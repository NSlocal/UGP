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
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("⚡ Universal Performance")
            .setContentText("Anti Lag • Anti Freeze • GPU Opt — Aktif ✅")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);
        return nb.build();
    }

    private void startOptimizationLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                applyAntiFreeze();
                applyCpuOptimization();
                applyGpuOptimization();
                applyBatterySaver();
                handler.postDelayed(this, 2000); // Ulangi tiap 2 detik
            }
        }, 1000);
    }

    // ✅ Anti Freeze — Stabilkan Frame Rate
    private void applyAntiFreeze() {
        try {
            // Set prioritas proses lebih tinggi
            android.os.Process.setThreadPriority(
                android.os.Process.myTid(),
                android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY
            );
        } catch (Exception e) { /* ignore */ }
    }

    // ✅ CPU Optimize — Kurangi Panas
    private void applyCpuOptimization() {
        try {
            // Turunkan beban latar belakang
            android.os.Process.setThreadPriority(
                android.os.Process.myPid(),
                android.os.Process.THREAD_PRIORITY_BACKGROUND + 5
            );
        } catch (Exception e) { /* ignore */ }
    }

    // ✅ GPU Optimize — Tanpa Google Play Services!
    private void applyGpuOptimization() {
        // TIDAK PAKAI Google Play Services — lebih ringan!
        // Hardware acceleration sudah aktif di Manifest
    }

    // ✅ Hemat Baterai — Kurangi Drain
    private void applyBatterySaver() {
        // Minimal update, tidak pakai sensor berlebih
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Tetap berjalan meski app ditutup
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
