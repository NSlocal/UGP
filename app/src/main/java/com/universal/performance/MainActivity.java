package com.universal.performance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION = 1001;
    private static final String PREFS = "PerfPrefs";
    
    private Switch switchService;
    private Switch switchAntiLag;
    private Switch switchGPUAccel;
    private TextView textStatus;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        
        initViews();
        checkPermissions();
        loadSettings();
        updateDeviceInfo();
    }

    private void initViews() {
        switchService = findViewById(R.id.switch_service);
        switchAntiLag = findViewById(R.id.switch_anti_lag);
        switchGPUAccel = findViewById(R.id.switch_gpu_accel);
        textStatus = findViewById(R.id.text_status);

        switchService.setOnCheckedChangeListener((btn, isOn) -> toggleService(isOn));
        switchAntiLag.setOnCheckedChangeListener((btn, isOn) -> {
            prefs.edit().putBoolean("anti_lag", isOn).apply();
            textStatus.setText(isOn ? "✅ Anti-Lag Active" : "⏸️ Anti-Lag Off");
        });
        switchGPUAccel.setOnCheckedChangeListener((btn, isOn) -> {
            prefs.edit().putBoolean("gpu_accel", isOn).apply();
            if (isOn) getWindow().setHardwareAccelerated(true);
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION);
        }
    }

    private void loadSettings() {
        switchAntiLag.setChecked(prefs.getBoolean("anti_lag", true));
        switchGPUAccel.setChecked(prefs.getBoolean("gpu_accel", true));
    }

    private void toggleService(boolean enable) {
        Intent service = new Intent(this, PerformanceService.class);
        if (enable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
            textStatus.setText("✅ Service Running");
        } else {
            stopService(service);
            textStatus.setText("⏹️ Service Stopped");
        }
        prefs.edit().putBoolean("service_running", enable).apply();
    }

    private void updateDeviceInfo() {
        TextView info = findViewById(R.id.text_device_info);
        info.setText(
            "Chipset: " + (Build.HARDWARE.contains("qcom") ? "Qualcomm" :
                          Build.HARDWARE.contains("mt") ? "MediaTek" : "Apple A18 Pro / Other") + "\n" +
            "Arch: " + Build.SUPPORTED_ABIS[0] + "\n" +
            "Android: " + Build.VERSION.RELEASE + "\n" +
            "Performance Mode: Active"
        );
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(reqCode, perms, results);
        if (reqCode == REQUEST_NOTIFICATION && results.length > 0
            && results[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
        }
    }
}
