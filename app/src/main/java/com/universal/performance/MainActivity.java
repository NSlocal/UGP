package com.universal.performance;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Switch switchService;
    private Switch switchAntiLag;
    private Switch switchGpuAccel;
    private TextView textStatus;
    private TextView textDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchService = findViewById(R.id.switch_service);
        switchAntiLag = findViewById(R.id.switch_anti_lag);
        switchGpuAccel = findViewById(R.id.switch_gpu_accel);
        textStatus = findViewById(R.id.text_status);
        textDeviceInfo = findViewById(R.id.text_device_info);

        // Show device info
        textDeviceInfo.setText("Device: " + Build.MODEL + "\nSDK: " + Build.VERSION.SDK_INT);

        // Service toggle
        switchService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startService(new Intent(this, PerformanceService.class));
                textStatus.setText("Status: Running");
            } else {
                stopService(new Intent(this, PerformanceService.class));
                textStatus.setText("Status: Stopped");
            }
        });
    }
}
