package com.universal.performance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Switch sService, sAntiLag, sGpuAccel, sGpuAntiLag, sFps, sDnd, sRam, sRefresh;
    private TextView tStatus, tFps, tTemp, tNet, tRefresh, tGames;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS_PERMISSION));
        }

        initViews();
        loadPrefs();
        setupListeners();
        startMonitor();
    }

    private void initViews() {
        sService = findViewById(R.id.switch_service);
        sAntiLag = findViewById(R.id.switch_anti_lag);
        sGpuAccel = findViewById(R.id.switch_gpu_accel);
        sGpuAntiLag = findViewById(R.id.switch_gpu_anti_lag);
        sFps = findViewById(R.id.switch_fps_overlay);
        sDnd = findViewById(R.id.switch_dnd_mode);
        sRam = findViewById(R.id.switch_ram_boost);
        sRefresh = findViewById(R.id.switch_refresh);
        tStatus = findViewById(R.id.text_status);
        tFps = findViewById(R.id.text_fps);
        tTemp = findViewById(R.id.text_temp);
        tNet = findViewById(R.id.text_network);
        tRefresh = findViewById(R.id.text_refresh_rate);
        tGames = findViewById(R.id.text_games);
    }

    private void loadPrefs() {
        sService.setChecked(prefs.getBoolean("service_running", false));
        sAntiLag.setChecked(prefs.getBoolean("anti_lag", false));
        sGpuAccel.setChecked(prefs.getBoolean("gpu_accel", false));
        sGpuAntiLag.setChecked(prefs.getBoolean("gpu_anti_lag", false));
        sFps.setChecked(prefs.getBoolean("fps_overlay", false));
        sDnd.setChecked(prefs.getBoolean("dnd_mode", false));
        sRam.setChecked(prefs.getBoolean("ram_boost", false));
        sRefresh.setChecked(prefs.getBoolean("refresh_unlock", false));
    }

    private void setupListeners() {
        sService.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                startService(new Intent(this, PerformanceService.class));
                tStatus.setText("✅ Status: Running");
                prefs.edit().putBoolean("service_running", true).apply();
            } else {
                stopService(new Intent(this, PerformanceService.class));
                stopService(new Intent(this, FpsOverlayService.class));
                tStatus.setText("⏹️ Status: Stopped");
                prefs.edit().putBoolean("service_running", false).apply();
            }
        });

        sAntiLag.setOnCheckedChangeListener((v, i) -> {
            prefs.edit().putBoolean("anti_lag", i).apply(); sendUpdate();
        });
        sGpuAccel.setOnCheckedChangeListener((v, i) -> {
            prefs.edit().putBoolean("gpu_accel", i).apply(); sendUpdate();
        });
        sGpuAntiLag.setOnCheckedChangeListener((v, i) -> {
            prefs.edit().putBoolean("gpu_anti_lag", i).apply(); sendUpdate();
        });
        sDnd.setOnCheckedChangeListener((v, i) -> {
            prefs.edit().putBoolean("dnd_mode", i).apply(); sendUpdate();
        });
        sRam.setOnCheckedChangeListener((v, i) -> {
            prefs.edit().putBoolean("ram_boost", i).apply(); sendUpdate();
        });
        sRefresh.setOnCheckedChangeListener((v, i) -> {
            prefs.edit().putBoolean("refresh_unlock", i).apply(); sendUpdate();
        });
        sFps.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked && prefs.getBoolean("service_running", false)) {
                startService(new Intent(this, FpsOverlayService.class));
            } else stopService(new Intent(this, FpsOverlayService.class));
        });
    }

    private void sendUpdate() {
        Intent i = new Intent("com.universal.performance.UPDATE_CONFIG");
        i.putExtra("anti_lag", prefs.getBoolean("anti_lag", false));
        i.putExtra("gpu_accel", prefs.getBoolean("gpu_accel", false));
        i.putExtra("gpu_anti_lag", prefs.getBoolean("gpu_anti_lag", false));
        i.putExtra("dnd_mode", prefs.getBoolean("dnd_mode", false));
        i.putExtra("ram_boost", prefs.getBoolean("ram_boost", false));
        i.putExtra("refresh_unlock", prefs.getBoolean("refresh_unlock", false));
        sendBroadcast(i);
    }

    private void startMonitor() {
        android.os.Handler h = new android.os.Handler();
        h.post(new Runnable() { public void run() { updateUI(); h.postDelayed(this, 500); }});
    }

    private void updateUI() {
        tRefresh.setText("Refresh Rate: " + (int)RefreshRateHelper.getCurrent(this) + "Hz");
        tNet.setText("WiFi: " + isWifiConnected() + " | Data: " + isDataConnected());
    }

    private String isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network n = cm.getActiveNetwork();
            NetworkCapabilities c = cm.getNetworkCapabilities(n);
            return c != null && c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "ON" : "OFF";
        }
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() ? "ON" : "OFF";
    }

    private String isDataConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network n = cm.getActiveNetwork();
            NetworkCapabilities c = cm.getNetworkCapabilities(n);
            return c != null && c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "ON" : "OFF";
        }
        return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected() ? "ON" : "OFF";
    }
}
