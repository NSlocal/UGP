package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.List;

public class PerformanceService extends Service {
    private static final String CHANNEL = "UniversalPerf";
    private static final int NOTIF_ID = 9999;
    private Handler h;
    private String currentGame = "";
    private boolean antiLag, gpuAccel, gpuAntiLag, dndNotif, ramBoost, refreshUnlock;

    @Override public void onCreate() {
        super.onCreate();
        h = new Handler();
        createNotifChannel();
        startForeground(NOTIF_ID, buildNotif());
        h.postDelayed(gameMonitor, 500);
    }

    private void createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Performance Active", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    private Notification buildNotif() {
        String text = currentGame.isEmpty() ? "Monitoring…" : "🎮 " + currentGame + " | 120Hz Active";
        Notification.Builder nb = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        return nb.setContentTitle("Universal Performance")
            .setContentText(text).setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).build();
    }

    private final Runnable gameMonitor = new Runnable() {
        @Override public void run() {
            String topApp = getForegroundApp();
            if (!topApp.equals(currentGame)) {
                boolean wasGame = GameConfig.isSupported(currentGame);
                boolean isGame = GameConfig.isSupported(topApp);
                currentGame = topApp;
                if (isGame) {
                    applyOptimizations(topApp);
                    startService(new Intent(PerformanceService.this, FpsOverlayService.class));
                } else if (wasGame) {
                    resetOptimizations();
                    stopService(new Intent(PerformanceService.this, FpsOverlayService.class));
                }
                ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIF_ID, buildNotif());
            }
            if (ramBoost) cleanBackground();
            h.postDelayed(this, 500);
        }
    };

    private String getForegroundApp() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();
            if (apps != null) for (ActivityManager.RunningAppProcessInfo info : apps) {
                if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                    return info.processName;
            }
        } catch (Exception e) {}
        return "";
    }

    private void applyOptimizations(String pkg) {
        if (antiLag) android.os.Process.setThreadPriority(android.os.Process.myPid(), -10);
        if (gpuAccel) android.os.SystemProperties.set("debug.hwui.renderer", "opengl");
        if (refreshUnlock) {
            float max = RefreshRateHelper.getMax(this);
            RefreshRateHelper.setRefreshRate(this, max);
            Log.d("PerfService", "✅ REFRESH RATE: " + (int)max + "Hz");
        }
        if (dndNotif && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (nm.isNotificationPolicyAccessGranted()) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        }
    }

    private void resetOptimizations() {
        if (dndNotif && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (nm.isNotificationPolicyAccessGranted()) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        }
    }

    private void cleanBackground() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> ps = am.getRunningAppProcesses();
            String myPkg = getPackageName();
            for (ActivityManager.RunningAppProcessInfo p : ps)
                if (!p.processName.equals(myPkg) && !p.processName.equals(currentGame))
                    am.killBackgroundProcesses(p.processName);
        } catch (Exception e) {}
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }
    @Override public void onDestroy() { super.onDestroy(); h.removeCallbacks(gameMonitor); resetOptimizations(); }
    @Override public IBinder onBind(Intent i) { return null; }
}
