package com.universal.performance;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView tvStatus;
    private Button btnToggle;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        btnToggle = findViewById(R.id.btn_toggle);

        btnToggle.setOnClickListener(v -> {
            isRunning = !isRunning;
            if (isRunning) {
                tvStatus.setText("✅ Status: RUNNING");
                btnToggle.setText("STOP SERVICE");
            } else {
                tvStatus.setText("⏹️ Status: STOPPED");
                btnToggle.setText("START SERVICE");
            }
        });
    }
}
