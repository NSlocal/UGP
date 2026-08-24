package com.universal.performance;

import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class RefreshRateHelper {
    public static float getCurrent(Context ctx) {
        try {
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            return wm.getDefaultDisplay().getRefreshRate();
        } catch (Exception e) { return 60; }
    }

    public static float getMax(Context ctx) {
        try {
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Display d = wm.getDefaultDisplay();
            float max = 60;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (Display.Mode m : d.getSupportedModes()) {
                    if (m.getRefreshRate() > max) max = m.getRefreshRate();
                }
            }
            return max;
        } catch (Exception e) { return 60; }
    }
}
