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

    // ✅ FITUR 1-8
    private Switch swPerfService, swAntiLag, swGpuAccel, swGpuAntiLag, 
                   swFpsOverlay, swDndNotif, swRamBoost, swRefreshUnlock;
    private TextView tvStatus, tvFps, tvTemp, tvNetwork, tvRefresh, tvGame;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

        // ✅ Minta Semua Permission
        checkAllPermissions();

        // ✅ Init Semua UI
        initViews();
        loadSavedSettings();
        setupListeners();
    }

    private void checkAllPermissions() {
        // Overlay Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
            && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(i);
            Toast.makeText(this, "Izinkan Tampil di Atas Aplikasi Lain", Toast.LENGTH_LONG).show();
        }
        // Write Settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
            && !Settings.System.canWrite(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS_PERMISSION);
            startActivity(i);
        }
        // Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                android.Manifest.permission.POST_NOTIFICATIONS
            }, 1001);
        }
    }

    private void initViews() {
        swPerfService   = findViewById(R.id.sw_perf_service);   // 1. Performance Service
        swAntiLag       = findViewById(R.id.sw_anti_lag);       // 2. Anti Lag Mode
        swGpuAccel      = findViewById(R.id.sw_gpu_accel);      // 3. GPU Acceleration
        swGpuAntiLag    = findViewById(R.id.sw_gpu_anti_lag);   // 4. GPU Anti Lag
        swFpsOverlay    = findViewById(R.id.sw_fps_overlay);     // 5. FPS & Network Real-time
        swDndNotif      = findViewById(R.id.sw_dnd_notif);      // 6. DND & Notif Killer
        swRamBoost      = findViewById(R.id.sw_ram_boost);       // 7. RAM Background Anti Lag
        swRefreshUnlock = findViewById(R.id.sw_refresh_unlock);  // 8. Refresh Rate Unlock

        tvStatus    = findViewById(R.id.tv_status);
        tvFps       = findViewById(R.id.tv_fps);
        tvTemp      = findViewById(R.id.tv_temp);
        tvNetwork   = findViewById(R.id.tv_network);
        tvRefresh   = findViewById(R.id.tv_refresh);
        tvGame      = findViewById(R.id.tv_game_info);
    }

    private void loadSavedSettings() {
        swPerfService.setChecked(prefs.getBoolean("perf_service", false));
        swAntiLag.setChecked(prefs.getBoolean("anti_lag", true));
        swGpuAccel.setChecked(prefs.getBoolean("gpu_accel", true));
        swGpuAntiLag.setChecked(prefs.getBoolean("gpu_anti_lag", true));
        swFpsOverlay.setChecked(prefs.getBoolean("fps_overlay", true));
        swDndNotif.setChecked(prefs.getBoolean("dnd_notif", false));
        swRamBoost.setChecked(prefs.getBoolean("ram_boost", true));
        swRefreshUnlock.setChecked(prefs.getBoolean("refresh_unlock", true));
    }

    private void setupListeners() {
        // ✅ 1. Performance Service — START / STOP
        swPerfService.setOnCheckedChangeListener((v, isOn) -> {
            prefs.edit().putBoolean("perf_service", isOn).apply();
            if (isOn) {
                startService(new Intent(this, PerformanceService.class));
                tvStatus.setText("✅ Status: RUNNING");
                Toast.makeText(this, "Service Started — Game Optimizations Active", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, PerformanceService.class));
                stopService(new Intent(this, FpsOverlayService.class));
                tvStatus.setText("⏹️ Status: STOPPED");
            }
        });

        // ✅ 2-8 — Simpan Settingan Langsung
        swAntiLag.setOnCheckedChangeListener((v, i) -> saveAndUpdate("anti_lag", i));
        swGpuAccel.setOnCheckedChangeListener((v, i) -> saveAndUpdate("gpu_accel", i));
        swGpuAntiLag.setOnCheckedChangeListener((v, i) -> saveAndUpdate("gpu_anti_lag", i));
        swFpsOverlay.setOnCheckedChangeListener((v, isOn) -> {
            prefs.edit().putBoolean("fps_overlay", isOn).apply();
            if (isOn && prefs.getBoolean("perf_service", false)) {
                startService(new Intent(this, FpsOverlayService.class));
            } else stopService(new Intent(this, FpsOverlayService.class));
        });
        swDndNotif.setOnCheckedChangeListener((v, i) -> saveAndUpdate("dnd_notif", i));
        swRamBoost.setOnCheckedChangeListener((v, i) -> saveAndUpdate("ram_boost", i));
        swRefreshUnlock.setOnCheckedChangeListener((v, i) -> saveAndUpdate("refresh_unlock", i));
    }

    private void saveAndUpdate(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        if (prefs.getBoolean("perf_service", false)) {
            Intent update = new Intent("com.universal.performance.UPDATE");
            update.putExtra(key, value);
            sendBroadcast(update);
        }
    }
}
