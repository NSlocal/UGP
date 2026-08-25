package com.universal.performance;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FpsOverlayService extends Service {
    private WindowManager wm;
    private View view;
    private TextView fps, temp, net, refresh;
    private Handler h;
    private int frameCount = 0;
    private long lastTime = System.nanoTime();

    @Override
    public void onCreate() {
        super.onCreate();
        h = new Handler();
        createOverlay();
        startCounter();
    }

    private void createOverlay() {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(this).inflate(R.layout.overlay_fps, null);
        fps = view.findViewById(R.id.overlay_fps);
        temp = view.findViewById(R.id.overlay_temp);
        net = view.findViewById(R.id.overlay_network);
        refresh = view.findViewById(R.id.overlay_refresh);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = 20; p.y = 50;
        wm.addView(view, p);
    }

    private void startCounter() {
        h.post(new Runnable() {
            public void run() {
                updateAll();
                h.postDelayed(this, 500);
            }
        });
    }

    private void updateAll() {
        frameCount++;
        long now = System.nanoTime();
        if ((now - lastTime) / 1e9 >= 1.0) {
            fps.setText("FPS: " + frameCount);
            frameCount = 0;
            lastTime = now;
        }
        temp.setText(String.format("%.1f°C", getCpuTemp()));
        net.setText("WiFi: " + getWifiStatus());
        refresh.setText((int) RefreshRateHelper.getCurrent(this) + "Hz");
    }

    private float getCpuTemp() {
        try {
            for (String p : new String[]{"/sys/class/thermal/thermal_zone0/temp", "/sys/class/thermal/thermal_zone1/temp"}) {
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
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network n = cm.getActiveNetwork();
            NetworkCapabilities c = cm.getNetworkCapabilities(n);
            return c != null && c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "ON" : "OFF";
        }
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() ? "ON" : "OFF";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null) wm.removeView(view);
        h.removeCallbacksAndMessages(null);
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }
}
