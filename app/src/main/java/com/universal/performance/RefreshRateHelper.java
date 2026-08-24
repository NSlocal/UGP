package com.universal.performance;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class RefreshRateHelper {
    private static final String TAG = "RefreshRateHelper";

    // Get current display refresh rate
    public static float getCurrentRefreshRate(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return display.getRefreshRate();
            }
            return 60.0f;
        } catch (Exception e) {
            Log.e(TAG, "Get refresh rate failed: " + e.getMessage());
            return 60.0f;
        }
    }

    // Non-Root Refresh Rate Bypass — try set to target rate
    public static boolean setRefreshRate(Context context, int targetRate) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();

            // Method 1: Reflection — setRefreshRate
            try {
                Method setRefreshRate = display.getClass().getMethod("setRefreshRate", int.class);
                setRefreshRate.invoke(display, targetRate);
                Log.d(TAG, "✅ Refresh Rate set to " + targetRate + "Hz (Method 1)");
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Method 1 failed: " + e.getMessage());
            }

            // Method 2: Set via Display.Mode (API 23+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Display.Mode[] modes = display.getSupportedModes();
                Display.Mode bestMode = null;
                
                for (Display.Mode mode : modes) {
                    float rate = mode.getRefreshRate();
                    if (Math.abs(rate - targetRate) < 1.0f) {
                        bestMode = mode;
                        break;
                    }
                    // Fallback to highest available
                    if (bestMode == null || rate > bestMode.getRefreshRate()) {
                        bestMode = mode;
                    }
                }

                if (bestMode != null) {
                    try {
                        Method setMode = display.getClass().getMethod("setDisplayMode", Display.Mode.class);
                        setMode.invoke(display, bestMode);
                        Log.d(TAG, "✅ Refresh Rate set to " + bestMode.getRefreshRate() + "Hz (Method 2)");
                        return true;
                    } catch (Exception e) {
                        Log.w(TAG, "Method 2 failed: " + e.getMessage());
                    }
                }
            }

            Log.w(TAG, "⚠️ Could not set refresh rate — using system default");
            return false;

        } catch (Exception e) {
            Log.e(TAG, "❌ Set refresh rate failed: " + e.getMessage());
            return false;
        }
    }

    // Get highest supported refresh rate
    public static float getMaxSupportedRefreshRate(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Display.Mode[] modes = display.getSupportedModes();
                float maxRate = 60.0f;
                for (Display.Mode mode : modes) {
                    if (mode.getRefreshRate() > maxRate) {
                        maxRate = mode.getRefreshRate();
                    }
                }
                return maxRate;
            }
            return 60.0f;
        } catch (Exception e) {
            return 60.0f;
        }
    }

    // Check if device supports high refresh rate
    public static boolean supportsHighRefreshRate(Context context) {
        return getMaxSupportedRefreshRate(context) >= 90.0f;
    }
}
