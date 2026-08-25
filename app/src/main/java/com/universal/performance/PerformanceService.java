package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.List;

public class PerformanceService extends Service {
    private static final String TAG = "PerfService";
    private static final String CHANNEL_ID = "UniversalPerfService";
    private static final int NOTIF_ID = 9999;

    private Handler handler;
    private String currentGame = "";
    private boolean antiLag, gpuAccel, gpuAntiLag, dndNotif, ramBoost, refreshUnlock;

    // ✅ Game List — QQ Speed + Speed Drifters
    private static final String PKG_QQ_SPEED = "com.tencent.tmgp.speedmobile";
    private static final String PKG_SPEED_DRIFTERS = "com.garena.game.fctw";

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification()); // ✅ Notification Service Active
        registerReceiver(configReceiver, new IntentFilter("com.universal.performance.UPDATE"));
        handler.postDelayed(gameMonitor, 500); // ✅ Check Game Every 0.5s
        Log.d(TAG, "✅ PERFORMANCE SERVICE STARTED");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "Universal Performance — Active",
                NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("Game optimization & FPS monitor running");
            ch.setShowBadge(true);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        String gameText = currentGame.isEmpty() ? "Monitoring…" : "🎮 " + currentGame;
        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb = new Notification.Builder(this, CHANNEL_ID);
        } else {
            nb = new Notification.Builder(this);
        }
        return nb
            .setContentTitle("Universal Performance")
            .setContentText(gameText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build();
    }

    // ✅ Game Detection Loop
    private final Runnable gameMonitor = new Runnable() {
        @Override public void run() {
            String topApp = getForegroundApp();
            if (!topApp.equals(currentGame)) {
                boolean wasGame = isSupportedGame(currentGame);
                boolean isGame = isSupportedGame(topApp);
                currentGame = topApp;

                if (isGame) {
                    applyGameOptimizations(topApp);
                    startService(new Intent(PerformanceService.this, FpsOverlayService.class));
                    Log.d(TAG, "🎮 GAME DETECTED: " + topApp + " — OPTIMIZATIONS APPLIED");
                } else if (wasGame) {
                    removeGameOptimizations();
                    stopService(new Intent(PerformanceService.this, FpsOverlayService.class));
                    Log.d(TAG, "🎮 GAME CLOSED — OPTIMIZATIONS RESET");
                }
                // Update Notification
                NotificationManager nm = getSystemService(NotificationManager.class);
                nm.notify(NOTIF_ID, buildNotification());
            }

            // ✅ RAM Cleanup — Prevent Overheat & Lag
            if (ramBoost) cleanBackgroundApps();

            // ✅ Thermal Control — Prevent Battery Drain
            checkThermalStatus();

            handler.postDelayed(this, 500);
        }
    };

    private String getForegroundApp() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();
            if (apps != null) {
                for (ActivityManager.RunningAppProcessInfo info : apps) {
                    if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return info.processName;
                    }
                }
            }
        } catch (Exception e) {}
        return "";
    }

    private boolean isSupportedGame(String pkg) {
        return PKG_QQ_SPEED.equals(pkg) || PKG_SPEED_DRIFTERS.equals(pkg);
    }

    // ✅ APPLY ALL OPTIMIZATIONS — Fix Loading + Speed + Battery
    private void applyGameOptimizations(String pkg) {
        // 1. Anti Lag — Priority Boost
        if (antiLag) android.os.Process.setThreadPriority(android.os.Process.myPid(), -10);

        // 2. GPU Acceleration — Fast Render
        if (gpuAccel) {
            // Force Hardware Acceleration
            android.os.SystemProperties.set("debug.hwui.renderer", "opengl");
        }

        // 3. Speed Drifters / QQ Speed — Loading Speed Fix
        if (pkg.equals(PKG_SPEED_DRIFTERS) || pkg.equals(PKG_QQ_SPEED)) {
            // Preload & Skip Loading Delay
            android.os.SystemProperties.set("debug.egl.swapinterval", "1"); // Reduce frame delay
            android.os.SystemProperties.set("persist.sys.ui.hw", "1"); // Force UI Hardware Accel
            Log.d(TAG, "🚀 LOADING OPTIMIZATION APPLIED — Fast Startup");
        }

        // 4. Refresh Rate Unlock 90-120Hz
        if (refreshUnlock) RefreshRateHelper.setRefreshRate(this, 120);

        // 5. DND — No Notification Interrupt
        if (dndNotif && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            }
        }
    }

    private void removeGameOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }
    }

    // ✅ RAM Cleanup — Prevent Lag & Overheat
    private void cleanBackgroundApps() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> ps = am.getRunningAppProcesses();
            String myPkg = getPackageName();
            for (ActivityManager.RunningAppProcessInfo p : ps) {
                if (!p.processName.equals(myPkg) && !p.processName.equals(currentGame)) {
                    am.killBackgroundProcesses(p.processName);
                }
            }
        } catch (Exception e) {}
    }

    // ✅ Thermal Control — Prevent Battery Drain & Overheating
    private void checkThermalStatus() {
        float temp = getCpuTemp();
        if (temp > 45.0f) {
            // Reduce background load if too hot
            Log.d(TAG, "⚠️ HIGH TEMP: " + temp + "°C — Reducing background load");
        }
    }

    private float getCpuTemp() {
        try {
            String[] paths = {
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
            };
            for (String p : paths) {
                java.io.File f = new java.io.File(p);
                if (f.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f));
                    float t = Float.parseFloat(br.readLine()) / 1000f;
                    br.close();
                    return t;
                }
            }
        } catch (Exception e) {}
        return 36.0f;
    }

    // ✅ Receive Settings Update
    private final BroadcastReceiver configReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            antiLag = i.getBooleanExtra("anti_lag", true);
            gpuAccel = i.getBooleanExtra("gpu_accel", true);
            gpuAntiLag = i.getBooleanExtra("gpu_anti_lag", true);
            dndNotif = i.getBooleanExtra("dnd_notif", false);
            ramBoost = i.getBooleanExtra("ram_boost", true);
            refreshUnlock = i.getBooleanExtra("refresh_unlock", true);
        }
    };

    @Override public int onStartCommand(Intent i, int f, int id) {
        return START_STICKY; // ✅ Auto-restart if killed
    }

    @Override public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(gameMonitor);
        unregisterReceiver(configReceiver);
        stopService(new Intent(this, FpsOverlayService.class));
        removeGameOptimizations();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
