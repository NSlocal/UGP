package com.universal.performance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
            SharedPreferences p = c.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            if (p.getBoolean("service", false)) {
                Intent si = new Intent(c, PerformanceService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) c.startForegroundService(si);
                else c.startService(si);
            }
        }
    }
}
