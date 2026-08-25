package com.universal.performance;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus, tvFps, tvTemp;
    private Button btnToggle;
    private Handler handler;
    private boolean isRunning = false;
    private int fpsCount = 0;
    private long fpsStartTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        tvFps = findViewById(R.id.tv_fps);
        tvTemp = findViewById(R.id.tv_temp);
        btnToggle = findViewById(R.id.btn_toggle);
        handler = new Handler();

        btnToggle.setOnClickListener(v -> toggleService());
        startFpsCounter();
    }

    private void toggleService() {
        isRunning = !isRunning;
        if (isRunning) {
            tvStatus.setText("Service: RUNNING");
            tvStatus.setTextColor(0xFF2E7D32);
            btnToggle.setText("STOP SERVICE");
        } else {
            tvStatus.setText("Service: STOPPED");
            tvStatus.setTextColor(0xFFE53935);
            btnToggle.setText("START SERVICE");
        }
    }

    private void startFpsCounter() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                fpsCount++;
                long now = System.currentTimeMillis();
                if (now - fpsStartTime >= 1000) {
                    tvFps.setText("FPS: " + fpsCount);
                    fpsCount = 0;
                    fpsStartTime = now;
                }
                handler.postDelayed(this, 100);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
