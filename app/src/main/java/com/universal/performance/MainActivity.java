package com.universal.performance;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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

        checkPermissions();

        btnToggle.setOnClickListener(v -> {
            isRunning = !isRunning;
            if (isRunning) {
                tvStatus.setText(R.string.running);
                btnToggle.setText(R.string.stop);
                Toast.makeText(this, "✅ Performance Active — 120Hz Ready!", Toast.LENGTH_SHORT).show();
            } else {
                tvStatus.setText(R.string.stopped);
                btnToggle.setText(R.string.start);
            }
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
            }
            if (!Settings.System.canWrite(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
            }
        }
    }
}
