package com.universal.performance;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FpsOverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private TextView fpsText;
    private TextView tempText;
    private TextView netText;
    private TextView refreshText;
    private Handler handler;
    private long lastTime = System.nanoTime();
    private int frameCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        createOverlay();
        startFpsCounter();
    }

    private void createOverlay() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        
        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.overlay_fps, null);
        
        fpsText = overlayView.findViewById(R.id.overlay_fps);
        tempText = overlayView.findViewById(R.id.overlay_temp);
        netText = overlayView.findViewById(R.id.overlay_network);
        refreshText = overlayView.findViewById(R.id.overlay_refresh);

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 20;
        params.y = 50;

        windowManager.addView(overlayView, params);
    }

    private void startFpsCounter() {
        Runnable fpsRunnable = new Runnable() {
            @Override
            public void run() {
                updateFps();
                updateTemp();
                updateNetwork();
                updateRefreshRate();
                handler.postDelayed(this, 500);
            }
        };
        handler.post(fpsRunnable);
    }

    private void updateFps() {
        frameCount++;
        long now = System.nanoTime();
        double elapsed = (now - lastTime) / 1_000_000_000.0;
        
        if (elapsed >= 1.0) {
            int fps = (int) Math.round(frameCount / elapsed);
            fpsText.setText("FPS: " + fps);
            frameCount = 0;
            lastTime = now;
        }
    }

    private void updateTemp() {
        float temp = getCpuTemp();
        tempText.setText(String.format("%.1f°C", temp));
    }

    private void updateNetwork() {
        netText.setText("WiFi: " + getWifiStatus());
    }

    private void updateRefreshRate() {
        float rate = RefreshRateHelper.getCurrentRefreshRate(this);
        refreshText.setText((int)rate + "Hz");
    }

    private float getCpuTemp() {
        try {
            String[] paths = {"/sys/class/thermal/thermal_zone0/temp", "/sys/class/thermal/thermal_zone1/temp"};
            for (String p : paths) {
                java.io.File f = new java.io.File(p);
                if (f.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f));
                    float t = Float.parseFloat(br.readLine()) / 1000f;
                    br.close();
                    return t;
                }
            }
        } catch (Exception e) {}
        return 36.0f;
    }

    private String getWifiStatus() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(android.net.ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected() ? "ON" : "OFF";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
