package com.universal.performance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Switch switchService;
    private Switch switchAntiLag;
    private Switch switchGpuAccel;
    private Switch switchGpuAntiLag;
    private Switch switchDndMode;
    private Switch switchRamBoost;
    private Switch switchFpsOverlay;
    private Switch switchRefreshRate;
    private TextView textStatus;
    private TextView textDeviceInfo;
    private TextView textFps;
    private TextView textTemp;
    private TextView textNetwork;
    private TextView textRefreshRateInfo;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

        // Check Overlay Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        }

        // Init Views
        switchService = findViewById(R.id.switch_service);
        switchAntiLag = findViewById(R.id.switch_anti_lag);
        switchGpuAccel = findViewById(R.id.switch_gpu_accel);
        switchGpuAntiLag = findViewById(R.id.switch_gpu_anti_lag);
        switchDndMode = findViewById(R.id.switch_dnd_mode);
        switchRamBoost = findViewById(R.id.switch_ram_boost);
        switchFpsOverlay = findViewById(R.id.switch_fps_overlay);
        switchRefreshRate = findViewById(R.id.switch_refresh_rate);
        textStatus = findViewById(R.id.text_status);
        textDeviceInfo = findViewById(R.id.text_device_info);
        textFps = findViewById(R.id.text_fps);
        textTemp = findViewById(R.id.text_temp);
        textNetwork = findViewById(R.id.text_network);
        textRefreshRateInfo = findViewById(R.id.text_refresh_rate);

        // Load saved states
        switchService.setChecked(prefs.getBoolean("service_running", false));
        switchAntiLag.setChecked(prefs.getBoolean("anti_lag", false));
        switchGpuAccel.setChecked(prefs.getBoolean("gpu_accel", false));
        switchGpuAntiLag.setChecked(prefs.getBoolean("gpu_anti_lag", false));
        switchDndMode.setChecked(prefs.getBoolean("dnd_mode", false));
        switchRamBoost.setChecked(prefs.getBoolean("ram_boost", false));
        switchFpsOverlay.setChecked(prefs.getBoolean("fps_overlay", false));
        switchRefreshRate.setChecked(prefs.getBoolean("refresh_rate_unlock", false));

        // Device Info
        float maxRR = RefreshRateHelper.getMaxSupportedRefreshRate(this);
        textDeviceInfo.setText("Device: " + Build.MODEL + "\nSDK: " + Build.VERSION.SDK_INT + 
            "\nMax Refresh: " + (int)maxRR + "Hz");
        textRefreshRateInfo.setText("Current: " + (int)RefreshRateHelper.getCurrentRefreshRate(this) + "Hz");

        // Service Toggle
        switchService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent serviceIntent = new Intent(this, PerformanceService.class);
            if (isChecked) {
                startService(serviceIntent);
                textStatus.setText("Status: Running");
                prefs.edit().putBoolean("service_running", true).apply();
                Toast.makeText(this, "✅ Performance Service ACTIVE", Toast.LENGTH_SHORT).show();
            } else {
                stopService(serviceIntent);
                stopService(new Intent(this, FpsOverlayService.class));
                textStatus.setText("Status: Stopped");
                prefs.edit().putBoolean("service_running", false).apply();
                Toast.makeText(this, "❌ Performance Service STOPPED", Toast.LENGTH_SHORT).show();
            }
        });

        // Anti Lag
        switchAntiLag.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("anti_lag", isChecked).apply();
            sendConfigToService();
        });

        // GPU Acceleration
        switchGpuAccel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("gpu_accel", isChecked).apply();
            sendConfigToService();
        });

        // GPU Anti Lag
        switchGpuAntiLag.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("gpu_anti_lag", isChecked).apply();
            sendConfigToService();
        });

        // DND Mode + Notification Killer
        switchDndMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dnd_mode", isChecked).apply();
            sendConfigToService();
            Toast.makeText(this, isChecked ? "🔕 DND & Notif Killer ON" : "🔔 DND OFF", Toast.LENGTH_SHORT).show();
        });

        // RAM Background Anti Lag
        switchRamBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("ram_boost", isChecked).apply();
            sendConfigToService();
            Toast.makeText(this, isChecked ? "🚀 RAM Boost ON" : "RAM Boost OFF", Toast.LENGTH_SHORT).show();
        });

        // FPS Overlay
        switchFpsOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("fps_overlay", isChecked).apply();
            if (isChecked && prefs.getBoolean("service_running", false)) {
                startService(new Intent(this, FpsOverlayService.class));
                Toast.makeText(this, "📊 FPS Overlay ON", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, FpsOverlayService.class));
            }
        });

        // 📱 Refresh Rate Unlock
        switchRefreshRate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("refresh_rate_unlock", isChecked).apply();
            sendConfigToService();
            Toast.makeText(this, isChecked ? "📱 Refresh Rate Unlock ON (90-120Hz)" : "Refresh Rate Unlock OFF", Toast.LENGTH_SHORT).show();
            textRefreshRateInfo.setText("Current: " + (int)RefreshRateHelper.getCurrentRefreshRate(this) + "Hz");
        });

        // Start real-time update
        startRealtimeUpdate();
    }

    private void sendConfigToService() {
        Intent intent = new Intent("com.universal.performance.UPDATE_CONFIG");
        intent.putExtra("anti_lag", prefs.getBoolean("anti_lag", false));
        intent.putExtra("gpu_accel", prefs.getBoolean("gpu_accel", false));
        intent.putExtra("gpu_anti_lag", prefs.getBoolean("gpu_anti_lag", false));
        intent.putExtra("dnd_mode", prefs.getBoolean("dnd_mode", false));
        intent.putExtra("ram_boost", prefs.getBoolean("ram_boost", false));
        intent.putExtra("refresh_rate_unlock", prefs.getBoolean("refresh_rate_unlock", false));
        sendBroadcast(intent);
    }

    private void startRealtimeUpdate() {
        android.os.Handler handler = new android.os.Handler();
        Runnable update = new Runnable() {
            @Override
            public void run() {
                updateStats();
                handler.postDelayed(this, 500);
            }
        };
        handler.post(update);
    }

    private void updateStats() {
        textFps.setText("FPS: ~60");
        float temp = getCpuTemperature();
        textTemp.setText("Suhu: " + String.format("%.1f°C", temp));
        textNetwork.setText("WiFi: " + getWifiStatus() + " | Data: " + getNetworkType());
        textRefreshRateInfo.setText("Current: " + (int)RefreshRateHelper.getCurrentRefreshRate(this) + "Hz");
    }

    private float getCpuTemperature() {
        try {
            String[] paths = {
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp"
            };
            for (String path : paths) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
                    String line = br.readLine();
                    br.close();
                    return Float.parseFloat(line) / 1000f;
                }
            }
        } catch (Exception e) {}
        return 35.0f;
    }

    private String getWifiStatus() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(android.net.ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected() ? "ON" : "OFF";
    }

    private String getNetworkType() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        android.net.NetworkInfo mobile = cm.getNetworkInfo(android.net.ConnectivityManager.TYPE_MOBILE);
        return mobile != null && mobile.isConnected() ? "ON" : "OFF";
    }
}
