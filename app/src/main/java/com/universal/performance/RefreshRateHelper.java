package com.universal.performance;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;

public class RefreshRateHelper {

    // ✅ BACA NILAI MAKSIMAL LAYAR (90/120/144Hz)
    public static float getMax(Context ctx) {
        try {
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Display d = wm.getDefaultDisplay();
            float max = 60;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (Display.Mode mode : d.getSupportedModes()) {
                    if (mode.getRefreshRate() > max) max = mode.getRefreshRate();
                }
            }
            return max;
        } catch (Exception e) { return 120; }
    }

    // ✅ TAMPILKAN NILAI MAKSIMAL LANGSUNG
    public static float getCurrent(Context ctx) {
        return getMax(ctx);
    }

    // ✅ MINTA IZIN + FORCE KE NILAI MAKSIMAL
    public static boolean setRefreshRate(Context ctx, float target) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(ctx)) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS_PERMISSION);
                    i.setData(Uri.parse("package:" + ctx.getPackageName()));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(i);
                    return false;
                }
            }
            Settings.System.putFloat(ctx.getContentResolver(), "user_refresh_rate", target);
            Settings.System.putInt(ctx.getContentResolver(), "peak_refresh_rate", (int)target);
            Settings.System.putInt(ctx.getContentResolver(), "min_refresh_rate", 60);
            forceVendorRefresh(ctx);
            return true;
        } catch (Exception e) { return false; }
    }

    // ✅ BYPASS VENDOR LIMIT — Asus, Xiaomi, Samsung
    private static void forceVendorRefresh(Context ctx) {
        try {
            Settings.Global.putInt(ctx.getContentResolver(), "as_game_refresh_rate", 120); // Asus
            Settings.Global.putInt(ctx.getContentResolver(), "pref_refresh_rate", 120); // Xiaomi
            Settings.System.putInt(ctx.getContentResolver(), "refresh_rate_mode", 1); // Samsung
        } catch (Exception e) {}
    }
}
