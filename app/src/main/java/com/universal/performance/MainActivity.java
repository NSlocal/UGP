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
    private int fps = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        tvFps = findViewById(R.id.tv_fps);
        tvTemp = findViewById(R.id.tv_temp);
        btnToggle = findViewById(R.id.btn_toggle);
        handler = new Handler();

        btnToggle.setOnClickListener(v -> {
            isRunning = !isRunning;
            if (isRunning) {
                tvStatus.setText("Status: RUNNING");
                tvStatus.setTextColor(0xFF2E7D32);
                btnToggle.setText("STOP");
                startLoop();
            } else {
                tvStatus.setText("Status: STOPPED");
                tvStatus.setTextColor(0xFFE53935);
                btnToggle.setText("START");
                handler.removeCallbacksAndMessages(null);
            }
        });
    }

    private void startLoop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                fps = (int)(Math.random() * 30) + 50;
                tvFps.setText("FPS: " + fps);
                tvTemp.setText("Temp: 36.0 C");
                handler.postDelayed(this, 500);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
