package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class PerformanceService extends Service {
    private static final String CHANNEL_ID = "UniversalPerfChannel";
    private static final int NOTIF_ID = 1001;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());
        startOptimizationLoop();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "Universal Performance",
                NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("Service Optimasi Berjalan");
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Universal Performance")
            .setContentText("Anti Lag - Anti Freeze - GPU Opt - Aktif")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }

    private void startOptimizationLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(
                        android.os.Process.myTid(),
                        android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY
                    );
                } catch (Exception e) { }
                handler.postDelayed(this, 2000);
            }
        }, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
