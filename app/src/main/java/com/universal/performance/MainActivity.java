package com.universal.performance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Switch sService, sAntiLag, sGpuAccel, sGpuAntiLag, sDnd, sRam, sFps, sRefresh;
    private TextView tStatus, tDevice, tFps, tTemp, tNet, tRefreshRate;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }

        sService = findViewById(R.id.switch_service);
        sAntiLag = findViewById(R.id.switch_anti_lag);
        sGpuAccel = findViewById(R.id.switch_gpu_accel);
        sGpuAntiLag = findViewById(R.id.switch_gpu_anti_lag);
        sDnd = findViewById(R.id.switch_dnd_mode);
        sRam = findViewById(R.id.switch_ram_boost);
        sFps = findViewById(R.id.switch_fps_overlay);
        sRefresh = findViewById(R.id.switch_refresh_rate);
        tStatus = findViewById(R.id.text_status);
        tDevice = findViewById(R.id.text_device_info);
        tFps = findViewById(R.id.text_fps);
        tTemp = findViewById(R.id.text_temp);
        tNet = findViewById(R.id.text_network);
        tRefreshRate = findViewById(R.id.text_refresh_rate);

        sService.setChecked(prefs.getBoolean("service_running", false));
        sAntiLag.setChecked(prefs.getBoolean("anti_lag", false));
        sGpuAccel.setChecked(prefs.getBoolean("gpu_accel", false));
        sGpuAntiLag.setChecked(prefs.getBoolean("gpu_anti_lag", false));
        sDnd.setChecked(prefs.getBoolean("dnd_mode", false));
        sRam.setChecked(prefs.getBoolean("ram_boost", false));
        sFps.setChecked(prefs.getBoolean("fps_overlay", false));
        sRefresh.setChecked(prefs.getBoolean("refresh_unlock", false));

        tDevice.setText("Device: " + Build.MODEL + "\nSDK: " + Build.VERSION.SDK_INT +
            "\nMax Refresh: " + (int)RefreshRateHelper.getMax(this) + "Hz");
        tRefreshRate.setText("Current: " + (int)RefreshRateHelper.getCurrent(this) + "Hz");

        sService.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                startService(new Intent(this, PerformanceService.class));
                tStatus.setText("Status: Running");
                prefs.edit().putBoolean("service_running", true).apply();
            } else {
                stopService(new Intent(this, PerformanceService.class));
                stopService(new Intent(this, FpsOverlayService.class));
                tStatus.setText("Status: Stopped");
                prefs.edit().putBoolean("service_running", false).apply();
            }
        });

        sAntiLag.setOnCheckedChangeListener((v, i) -> { prefs.edit().putBoolean("anti_lag", i).apply(); sendUpdate(); });
        sGpuAccel.setOnCheckedChangeListener((v, i) -> { prefs.edit().putBoolean("gpu_accel", i).apply(); sendUpdate(); });
        sGpuAntiLag.setOnCheckedChangeListener((v, i) -> { prefs.edit().putBoolean("gpu_anti_lag", i).apply(); sendUpdate(); });
        sDnd.setOnCheckedChangeListener((v, i) -> { prefs.edit().putBoolean("dnd_mode", i).apply(); sendUpdate(); });
        sRam.setOnCheckedChangeListener((v, i) -> { prefs.edit().putBoolean("ram_boost", i).apply(); sendUpdate(); });
        sRefresh.setOnCheckedChangeListener((v, i) -> { prefs.edit().putBoolean("refresh_unlock", i).apply(); sendUpdate(); });
        sFps.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked && prefs.getBoolean("service_running", false)) {
                startService(new Intent(this, FpsOverlayService.class));
            } else stopService(new Intent(this, FpsOverlayService.class));
        });

        startRealtime();
    }

    private void sendUpdate() {
        Intent i = new Intent("com.universal.performance.UPDATE_CONFIG");
        i.putExtra("anti_lag", prefs.getBoolean("anti_lag", false));
        i.putExtra("gpu_accel", prefs.getBoolean("gpu_accel", false));
        i.putExtra("gpu_anti_lag", prefs.getBoolean("gpu_anti_lag", false));
        i.putExtra("dnd_mode", prefs.getBoolean("dnd_mode", false));
        i.putExtra("ram_boost", prefs.getBoolean("ram_boost", false));
        i.putExtra("refresh_rate_unlock", prefs.getBoolean("refresh_unlock", false));
        sendBroadcast(i);
    }

    private void startRealtime() {
        android.os.Handler h = new android.os.Handler();
        h.post(new Runnable() { public void run() { updateStats(); h.postDelayed(this, 500); }});
    }

    private void updateStats() {
        tFps.setText("FPS: ~60");
        tTemp.setText("Suhu: " + String.format("%.1f°C", getCpuTemp()));
        tNet.setText("WiFi: " + getWifi() + " | Data: " + getData());
        tRefreshRate.setText("Current: " + (int)RefreshRateHelper.getCurrent(this) + "Hz");
    }

    private float getCpuTemp() {
        try {
            for (String p : new String[]{"/sys/class/thermal/thermal_zone0/temp", "/sys/class/thermal/thermal_zone1/temp"}) {
                java.io.File f = new java.io.File(p);
                if (f.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f));
                    float t = Float.parseFloat(br.readLine()) / 1000f; br.close(); return t;
                }
            }
        } catch (Exception e) {}
        return 35;
    }

    private String getWifi() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        android.net.NetworkInfo w = cm.getNetworkInfo(android.net.ConnectivityManager.TYPE_WIFI);
        return w != null && w.isConnected() ? "ON" : "OFF";
    }

    private String getData() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        android.net.NetworkInfo m = cm.getNetworkInfo(android.net.ConnectivityManager.TYPE_MOBILE);
        return m != null && m.isConnected() ? "ON" : "OFF";
    }
}
