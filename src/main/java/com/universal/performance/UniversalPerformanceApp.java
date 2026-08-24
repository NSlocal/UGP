package com.universal.performance;

import android.app.Application;
import android.os.Build;
import android.os.Process;
import android.util.Log;

public class UniversalPerformanceApp extends Application {
    private static final String TAG = "UniversalPerf";
    private static UniversalPerformanceApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // ⚡ Lower background priority for smoother UI
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        
        // Log device info
        Log.i(TAG, "=== Device Info ===");
        Log.i(TAG, "Manufacturer: " + Build.MANUFACTURER);
        Log.i(TAG, "Model: " + Build.MODEL);
        Log.i(TAG, "CPU: " + Build.SUPPORTED_ABIS[0]);
        Log.i(TAG, "Android SDK: " + Build.VERSION.SDK_INT);
    }

    public static UniversalPerformanceApp getInstance() {
        return instance;
    }
}
