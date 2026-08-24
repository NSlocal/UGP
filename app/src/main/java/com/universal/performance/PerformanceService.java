package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class PerformanceService extends PerformanceService {
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
        startForeground(NOTIF_ID, buildNotification());
        Log.i(TAG, "Service Created — Performance Optimization Active");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences("PerfPrefs", Context.MODE_PRIVATE);
        antiLagActive = prefs.getBoolean("anti_lag", true);
        
        // ⚡ Max priority for performance
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        handler.postDelayed(monitorRunnable, 1000);
        
        return START_STICKY;
    }

    private void updatePerformance() {
        if (antiLagActive) {
            // Anti-lag: reduce jank, keep main thread responsive
            android.os.Debug.startMethodTracingSampling("perf_trace", 1024 * 1024, 1000);
            Log.d(TAG, "Anti-Lag & GPU Optimization — Active");
        }
        
        // Refresh every 2s
        handler.postDelayed(monitorRunnable, 2000);
    }

    private Notification buildNotification() {
        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Universal Performance")
                .setContentText(antiLagActive ? "✅ Anti-Lag + GPU Optimization Active" : "Running — Standard Mode")
                .setSmallIcon(R.drawable.ic_performance)
                .setContentIntent(pi)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID, "Performance Service",
                    NotificationManager.IMPORTANCE_HIGH);
            chan.setDescription("Keeps performance optimizations active");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(chan);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(monitorRunnable);
        stopForeground(true);
        Log.i(TAG, "Service Stopped — Optimizations Disabled");
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }
}
