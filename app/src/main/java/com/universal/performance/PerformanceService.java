package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class PerformanceService extends Service {
    // ✅ ALL INSIDE the class!
    private static final String TAG = "PerfService";
    private static final String CHANNEL_ID = "PerformanceService";
    private static final int NOTIF_ID = 9999;
    
    private Handler handler;
    private final Runnable monitorRunnable = this::updatePerformance;
    private boolean antiLagActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Performance Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    private void updatePerformance() {
        // Your code here
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, new Notification.Builder(this, CHANNEL_ID).build());
        handler.postDelayed(monitorRunnable, 1000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(monitorRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
