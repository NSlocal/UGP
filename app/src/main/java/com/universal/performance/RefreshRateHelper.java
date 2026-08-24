package com.universal.performance;

import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class RefreshRateHelper {
    public static float getCurrentRefreshRate(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            return display.getRefreshRate();
        } catch (Exception e) { return 60.0f; }
    }

    public static boolean setRefreshRate(Context context, int targetRate) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Display.Mode[] modes = display.getSupportedModes();
                for (Display.Mode mode : modes) {
                    if (Math.abs(mode.getRefreshRate() - targetRate) < 5.0f) {
                        display.getClass().getMethod("setDisplayMode", Display.Mode.class).invoke(display, mode);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) { return false; }
    }

    public static float getMaxSupportedRefreshRate(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            float max = 60;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (Display.Mode m : display.getSupportedModes()) {
                    if (m.getRefreshRate() > max) max = m.getRefreshRate();
                }
            }
            return max;
        } catch (Exception e) { return 60; }
    }
}
