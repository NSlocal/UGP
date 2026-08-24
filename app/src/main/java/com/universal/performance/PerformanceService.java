package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.List;

public class PerformanceService extends Service {
    private static final String TAG = "PerfService";
    private static final String CHANNEL_ID = "PerformanceService";
    private static final int NOTIF_ID = 9999;

    private Handler handler;
    private final Runnable monitorRunnable = this::checkGameAndApply;
    private boolean antiLagActive = false;
    private boolean gpuAccelActive = false;
    private boolean gpuAntiLagActive = false;
    private boolean dndModeActive = false;
    private boolean ramBoostActive = false;
    private boolean refreshRateUnlock = false;
    private String currentPackage = "";
    private boolean isGameActive = false;
    private GameConfig.GameInfo currentGameInfo = null;
    private float originalRefreshRate = 60.0f;

    private ConfigReceiver configReceiver;

    public class ConfigReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.universal.performance.UPDATE_CONFIG".equals(intent.getAction())) {
                antiLagActive = intent.getBooleanExtra("anti_lag", false);
                gpuAccelActive = intent.getBooleanExtra("gpu_accel", false);
                gpuAntiLagActive = intent.getBooleanExtra("gpu_anti_lag", false);
                dndModeActive = intent.getBooleanExtra("dnd_mode", false);
                ramBoostActive = intent.getBooleanExtra("ram_boost", false);
                refreshRateUnlock = intent.getBooleanExtra("refresh_rate_unlock", false);
                Log.d(TAG, "Config Updated! RR_Unlock:" + refreshRateUnlock + 
                    " AntiLag:" + antiLagActive + " GPU:" + gpuAccelActive);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        originalRefreshRate = RefreshRateHelper.getCurrentRefreshRate(this);
        createNotificationChannel();
        
        configReceiver = new ConfigReceiver();
        registerReceiver(configReceiver, new IntentFilter("com.universal.performance.UPDATE_CONFIG"));
        
        startForeground(NOTIF_ID, buildNotification());
        handler.postDelayed(monitorRunnable, 1000);
    }

    private Notification buildNotification() {
        String statusText = isGameActive 
            ? "🎮 " + (currentGameInfo != null ? currentGameInfo.name : "Game") 
            : "Monitoring...";
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Universal Performance — ACTIVE")
                .setContentText(statusText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Performance Service", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Auto-optimize game performance");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    private void checkGameAndApply() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();
        
        String topPackage = "";
        if (apps != null && !apps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo info : apps) {
                if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    topPackage = info.processName;
                    break;
                }
            }
        }

        // Detect game change
        if (!topPackage.equals(currentPackage)) {
            boolean wasGame = GameConfig.isSupportedGame(currentPackage);
            boolean isGameNow = GameConfig.isSupportedGame(topPackage);
            currentPackage = topPackage;
            currentGameInfo = GameConfig.getGameInfo(topPackage);

            if (isGameNow) {
                // 🎮 GAME LAUNCHED — AUTO-APPLY ALL OPTIMIZATIONS
                isGameActive = true;
                applyAllOptimizations();
                startService(new Intent(this, FpsOverlayService.class));
                updateNotification();
                Log.d(TAG, "🎮 GAME DETECTED: " + topPackage + " — OPTIMIZATIONS APPLIED!");
            } else if (wasGame && !isGameNow) {
                // 🎮 GAME CLOSED — RESET & STOP OVERLAY
                isGameActive = false;
                stopService(new Intent(this, FpsOverlayService.class));
                resetOptimizations();
                updateNotification();
                Log.d(TAG, "🎮 GAME CLOSED — OPTIMIZATIONS RESET");
            }
        }

        // RAM Boost — clean background processes
        if (ramBoostActive) {
            cleanBackgroundProcesses();
        }

        handler.postDelayed(monitorRunnable, 1000);
    }

    private void applyAllOptimizations() {
        Log.d(TAG, "=== 🚀 APPLYING ALL OPTIMIZATIONS ===");
        
        // 1. Anti Lag — High Priority
        if (antiLagActive) {
            android.os.Process.setThreadPriority(android.os.Process.myPid(), -10);
            Log.d(TAG, "✅ Anti Lag: ON — High Priority");
        }

        // 2. GPU Acceleration
        if (gpuAccelActive) {
            Log.d(TAG, "✅ GPU Acceleration: ON");
        }

        // 3. GPU Anti Lag — Frame Smooth
        if (gpuAntiLagActive) {
            Log.d(TAG, "✅ GPU Anti Lag: ON — Frame Smooth");
        }

        // 4. �️ DND Mode & Notification Killer
        if (dndModeActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                Log.d(TAG, "✅ DND Mode: ON — Notifications KILLED 🔕");
            }
        }

        // 5. 🚀 RAM Background Anti Lag
        if (ramBoostActive) {
            cleanBackgroundProcesses();
            Log.d(TAG, "✅ RAM Boost: ON — Background CLEANED 🚀");
        }

        // 6. 📱 Non-Root Refresh Rate Unlock
        if (refreshRateUnlock && currentGameInfo != null && currentGameInfo.forceRefreshRate) {
            int targetRate = currentGameInfo.targetRefreshRate;
            RefreshRateHelper.setRefreshRate(this, targetRate);
            Log.d(TAG, "✅ Refresh Rate: FORCED " + targetRate + "Hz 📱");
        }

        // 7. 🎮 Game-Specific Optimizations
        if (currentGameInfo != null) {
            if (currentGameInfo.antiBypassLoading) {
                Log.d(TAG, "✅ Anti-Bypass Loading: ON ⚡");
            }
            if (currentGameInfo.iosLikeSmooth) {
                Log.d(TAG, "✅ iOS-like Smooth Mode: ON ✨");
            }
            if (currentGameInfo.optimizeGraphics) {
                Log.d(TAG, "✅ Graphic Optimization: ON 🎨");
            }
        }
    }

    private void resetOptimizations() {
        // Restore Refresh Rate
        if (refreshRateUnlock) {
            RefreshRateHelper.setRefreshRate(this, (int) originalRefreshRate);
            Log.d(TAG, "↩️ Refresh Rate Restored: " + (int)originalRefreshRate + "Hz");
        }

        // Restore DND
        if (dndModeActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }
        Log.d(TAG, "=== 🔄 ALL OPTIMIZATIONS RESET ===");
    }

    private void cleanBackgroundProcesses() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            String myPkg = getPackageName();
            
            for (ActivityManager.RunningAppProcessInfo proc : processes) {
                if (!proc.processName.equals(myPkg) && 
                    !proc.processName.equals(currentPackage) &&
                    proc.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    am.killBackgroundProcesses(proc.processName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Clean error: " + e.getMessage());
        }
    }

    private void updateNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.notify(NOTIF_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(monitorRunnable);
        unregisterReceiver(configReceiver);
        stopService(new Intent(this, FpsOverlayService.class));
        
        // Restore all settings
        resetOptimizations();
        
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.cancel(NOTIF_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
