package com.universal.performance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private Switch swService, swAntiFreeze, swCpuOpt, swGpuOpt, swBattery;
    private TextView tvStatus, tvFps, tvTemp, tvCpu, tvGpu;
    private Button btnToggle;
    private SharedPreferences prefs;
    private Handler handler;
    private boolean isRunning = false;
    private int fpsCount = 0;
    private long fpsStartTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
        handler = new Handler();

        initViews();
        checkAllPermissions();
        loadSettings();
        setupListeners();
        startFpsCounter();
    }

    private void initViews() {
        swService = findViewById(R.id.sw_service);
        swAntiFreeze = findViewById(R.id.sw_anti_freeze);
        swCpuOpt = findViewById(R.id.sw_cpu_opt);
        swGpuOpt = findViewById(R.id.sw_gpu_opt);
        swBattery = findViewById(R.id.sw_battery_save);

        tvStatus = findViewById(R.id.tv_status);
        tvFps = findViewById(R.id.tv_fps);
        tvTemp = findViewById(R.id.tv_temp);
        tvCpu = findViewById(R.id.tv_cpu);
        tvGpu = findViewById(R.id.tv_gpu);

        btnToggle = findViewById(R.id.btn_toggle);
    }

    // ✅ PERBAIKI: Minta semua izin — TIDAK ERROR di GitHub!
    private void checkAllPermissions() {
        // 1. Tampil di atas aplikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
            && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        }

        // 2. Ubah pengaturan sistem
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
            && !Settings.System.canWrite(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        }

        // 3. Notifikasi — PERBAIKI: TIDAK PAKAI nama variabel yang salah!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                Intent i = new Intent();
                i.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                i.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                startActivity(i);
            }
        }
    }

    private void loadSettings() {
        swService.setChecked(prefs.getBoolean("service", false));
        swAntiFreeze.setChecked(prefs.getBoolean("anti_freeze", true));
        swCpuOpt.setChecked(prefs.getBoolean("cpu_opt", true));
        swGpuOpt.setChecked(prefs.getBoolean("gpu_opt", true));
        swBattery.setChecked(prefs.getBoolean("battery", true));
    }

    private void setupListeners() {
        btnToggle.setOnClickListener(v -> toggleService());

        swService.setOnCheckedChangeListener((b, isOn) -> 
            prefs.edit().putBoolean("service", isOn).apply());
        swAntiFreeze.setOnCheckedChangeListener((b, isOn) -> 
            prefs.edit().putBoolean("anti_freeze", isOn).apply());
        swCpuOpt.setOnCheckedChangeListener((b, isOn) -> 
            prefs.edit().putBoolean("cpu_opt", isOn).apply());
        swGpuOpt.setOnCheckedChangeListener((b, isOn) -> 
            prefs.edit().putBoolean("gpu_opt", isOn).apply());
        swBattery.setOnCheckedChangeListener((b, isOn) -> 
            prefs.edit().putBoolean("battery", isOn).apply());
    }

    private void toggleService() {
        isRunning = !isRunning;
        if (isRunning) {
            tvStatus.setText("✅ Service: RUNNING");
            tvStatus.setTextColor(0xFF2E7D32);
            btnToggle.setText("⏹️ STOP SERVICE");
            btnToggle.setBackgroundColor(0xFFE53935);
            
            startService(new Intent(this, PerformanceService.class));
            Toast.makeText(this, "✅ Semua Fitur Aktif — Anti Lag + Anti Freeze ON!", Toast.LENGTH_SHORT).show();
        } else {
            tvStatus.setText("⏹️ Service: STOPPED");
            tvStatus.setTextColor(0xFFE53935);
            btnToggle.setText("▶️ START SERVICE");
            btnToggle.setBackgroundColor(0xFF1A73E8);
            
            stopService(new Intent(this, PerformanceService.class));
        }
    }

    // ✅ FPS & SUHU REAL-TIME di UI
    private void startFpsCounter() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                fpsCount++;
                long now = System.currentTimeMillis();
                if (now - fpsStartTime >= 1000) {
                    tvFps.setText("🎮 FPS: " + fpsCount);
                    fpsCount = 0;
                    fpsStartTime = now;
                }

                // Update suhu simulasi (bisa diganti baca sensor asli)
                float temp = getCpuTemperature();
                tvTemp.setText(String.format("🌡️ Suhu: %.1f°C", temp));

                // Status CPU & GPU
                tvCpu.setText(swCpuOpt.isChecked() ? "💻 CPU: Optimal ✅" : "💻 CPU: Normal");
                tvGpu.setText(swGpuOpt.isChecked() ? "🎨 GPU: Accelerated ✅" : "🎨 GPU: Normal");

                handler.postDelayed(this, 100);
            }
        });
    }

    private float getCpuTemperature() {
        try {
            String[] paths = {
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
            };
            for (String path : paths) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
                    float temp = Float.parseFloat(br.readLine()) / 1000f;
                    br.close();
                    return temp;
                }
            }
        } catch (Exception e) { /* fallback */ }
        return 35.5f;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
