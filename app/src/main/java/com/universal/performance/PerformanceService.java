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
    private static final String CHANNEL = "PerformanceService";
    private static final int NOTIF_ID = 9999;

    private Handler h;
    private String currentPkg = "";
    private boolean antiLag = false, gpuAccel = false, gpuAntiLag = false, dnd = false, ramBoost = false, refreshUnlock = false;

    private BroadcastReceiver recv = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            antiLag = i.getBooleanExtra("anti_lag", false);
            gpuAccel = i.getBooleanExtra("gpu_accel", false);
            gpuAntiLag = i.getBooleanExtra("gpu_anti_lag", false);
            dnd = i.getBooleanExtra("dnd_mode", false);
            ramBoost = i.getBooleanExtra("ram_boost", false);
            refreshUnlock = i.getBooleanExtra("refresh_rate_unlock", false);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        h = new Handler(Looper.getMainLooper());
        createChannel();
        registerReceiver(recv, new IntentFilter("com.universal.performance.UPDATE_CONFIG"));
        startForeground(NOTIF_ID, buildNotif());
        h.postDelayed(this::checkGame, 1000);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Performance Service", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    private Notification buildNotif() {
        return new Notification.Builder(this, CHANNEL)
            .setContentTitle("Universal Performance — ACTIVE")
            .setContentText("Monitoring: " + (currentPkg.isEmpty() ? "None" : currentPkg))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true).build();
    }

    private void checkGame() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();
        String top = "";
        if (apps != null) for (ActivityManager.RunningAppProcessInfo info : apps) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                top = info.processName; break;
            }
        }

        if (!top.equals(currentPkg)) {
            boolean wasGame = GameConfig.isSupportedGame(currentPkg);
            boolean isGame = GameConfig.isSupportedGame(top);
            currentPkg = top;

            if (isGame) {
                applyAll();
                startService(new Intent(this, FpsOverlayService.class));
                Log.d(TAG, "🎮 GAME DETECTED: " + top);
            } else if (wasGame) {
                resetAll();
                stopService(new Intent(this, FpsOverlayService.class));
                Log.d(TAG, "🎮 GAME CLOSED");
            }
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIF_ID, buildNotif());
        }

        if (ramBoost) cleanBackground();
        h.postDelayed(this::checkGame, 1000);
    }

    private void applyAll() {
        if (antiLag) android.os.Process.setThreadPriority(android.os.Process.myPid(), -10);
        if (dnd && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (nm.isNotificationPolicyAccessGranted()) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        }
        Log.d(TAG, "✅ ALL OPTIMIZATIONS APPLIED");
    }

    private void resetAll() {
        if (dnd && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (nm.isNotificationPolicyAccessGranted()) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        }
        Log.d(TAG, "🔄 ALL OPTIMIZATIONS RESET");
    }

    private void cleanBackground() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> ps = am.getRunningAppProcesses();
            String myPkg = getPackageName();
            for (ActivityManager.RunningAppProcessInfo p : ps) {
                if (!p.processName.equals(myPkg) && !p.processName.equals(currentPkg)) {
                    am.killBackgroundProcesses(p.processName);
                }
            }
        } catch (Exception e) {}
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public void onDestroy() {
        super.onDestroy();
        h.removeCallbacksAndMessages(null);
        unregisterReceiver(recv);
        stopService(new Intent(this, FpsOverlayService.class));
        resetAll();
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
