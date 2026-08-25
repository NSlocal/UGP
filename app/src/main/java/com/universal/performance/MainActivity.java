package com.universal.performance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Switch swService, swAntiLag, swGpuAccel, swGpuAntiLag, swFps, swDnd, swRam, swRefresh;
    private TextView tvStatus, tvRefresh;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
        checkPermissions();
        initViews();
        loadSettings();
        setupListeners();
        updateRefreshDisplay();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }
            if (!Settings.System.canWrite(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS_PERMISSION);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
                Toast.makeText(this, "Izinkan Ubah Pengaturan Sistem → untuk 120Hz", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initViews() {
        swService = findViewById(R.id.sw_perf_service);
        swAntiLag = findViewById(R.id.sw_anti_lag);
        swGpuAccel = findViewById(R.id.sw_gpu_accel);
        swGpuAntiLag = findViewById(R.id.sw_gpu_anti_lag);
        swFps = findViewById(R.id.sw_fps_overlay);
        swDnd = findViewById(R.id.sw_dnd_notif);
        swRam = findViewById(R.id.sw_ram_boost);
        swRefresh = findViewById(R.id.sw_refresh_unlock);
        tvStatus = findViewById(R.id.tv_status);
        tvRefresh = findViewById(R.id.tv_refresh);
    }

    private void loadSettings() {
        swService.setChecked(prefs.getBoolean("service", false));
        swAntiLag.setChecked(prefs.getBoolean("anti_lag", true));
        swGpuAccel.setChecked(prefs.getBoolean("gpu_accel", true));
        swGpuAntiLag.setChecked(prefs.getBoolean("gpu_anti_lag", true));
        swFps.setChecked(prefs.getBoolean("fps", true));
        swDnd.setChecked(prefs.getBoolean("dnd", false));
        swRam.setChecked(prefs.getBoolean("ram", true));
        swRefresh.setChecked(prefs.getBoolean("refresh", true));
    }

    private void setupListeners() {
        swService.setOnCheckedChangeListener((v, isOn) -> {
            prefs.edit().putBoolean("service", isOn).apply();
            if (isOn) {
                startService(new Intent(this, PerformanceService.class));
                tvStatus.setText(R.string.status_running);
            } else {
                stopService(new Intent(this, PerformanceService.class));
                stopService(new Intent(this, FpsOverlayService.class));
                tvStatus.setText(R.string.status_stopped);
            }
        });

        swRefresh.setOnCheckedChangeListener((v, isOn) -> {
            prefs.edit().putBoolean("refresh", isOn).apply();
            if (isOn) {
                float max = RefreshRateHelper.getMax(this);
                RefreshRateHelper.setRefreshRate(this, max);
                Toast.makeText(this, "Refresh Rate: " + (int)max + "Hz ACTIVE ✅", Toast.LENGTH_SHORT).show();
            }
            updateRefreshDisplay();
        });

        swFps.setOnCheckedChangeListener((v, isOn) -> {
            prefs.edit().putBoolean("fps", isOn).apply();
            if (isOn && prefs.getBoolean("service", false)) {
                startService(new Intent(this, FpsOverlayService.class));
            } else stopService(new Intent(this, FpsOverlayService.class));
        });
    }

    private void updateRefreshDisplay() {
        float max = RefreshRateHelper.getMax(this);
        tvRefresh.setText("Refresh: " + (int)max + "Hz ✅");
    }
}
