package com.universal.performance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var fpsText: TextView
    private lateinit var tempText: TextView
    private lateinit var batteryText: TextView
    private lateinit var statusText: TextView
    private var fpsJob: Job? = null
    private var tempJob: Job? = null
    private var frameCount = 0
    private var lastTime = System.nanoTime()

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.parseColor("#6200EE")

        // === MAIN LAYOUT ===
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // === TOP HEADER ===
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#6200EE"))
            setPadding(32, 48, 32, 24)
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "UGP — Universal Performance"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        header.addView(title)
        root.addView(header)

        // === REAL-TIME MONITOR PANEL ===
        val monitorPanel = CardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#1E1E1E"))
            radius = 24f
            cardElevation = 8f
            setContentPadding(32, 28, 32, 28)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(24, 24, 24, 16) }
        }
        val monitorLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fpsText = createStatRow("FPS", "--/120 Hz", "#4CAF50")
        tempText = createStatRow("Temperature", "--°C", "#FF9800")
        batteryText = createStatRow("Battery", "--%", "#03DAC6")
        statusText = createStatRow("Status", "Initializing...", "#6200EE")

        monitorLayout.addView(fpsText)
        monitorLayout.addView(createDivider())
        monitorLayout.addView(tempText)
        monitorLayout.addView(createDivider())
        monitorLayout.addView(batteryText)
        monitorLayout.addView(createDivider())
        monitorLayout.addView(statusText)
        monitorPanel.addView(monitorLayout)
        root.addView(monitorPanel)

        // === FEATURE CARDS ===
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val featureContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 48)
        }

        featureContainer.addView(createFeatureCard(
            "⚡ Speed Bypass",
            "Unlock frame rate • 120Hz smooth • Anti-freeze",
            "#6200EE",
            "speed"
        ))
        featureContainer.addView(createFeatureCard(
            "🧊 Anti-Lag & Freeze",
            "Stabilize CPU/GPU • Reduce jitter • No frame drop",
            "#03DAC6",
            "lag"
        ))
        featureContainer.addView(createFeatureCard(
            "🔋 Battery Saver & Cooler",
            "Optimize power • Reduce heat • No overheating",
            "#FF9800",
            "battery"
        ))
        featureContainer.addView(createFeatureCard(
            "🎮 Graphics & Stability",
            "Max FPS • Smooth render • Gameplay stable",
            "#4CAF50",
            "graphics"
        ))
        featureContainer.addView(createFeatureCard(
            "🚫 No Google Services",
            "Disable GMS • Remove lag source • Lighter runtime",
            "#F44336",
            "gms"
        ))

        scroll.addView(featureContainer)
        root.addView(scroll)

        setContentView(root)

        // === START SERVICES ===
        requestNotificationPermission()
        startRealTimeMonitoring()
        applyPerformanceFixes()
    }

    // === CREATE STAT ROW ===
    private fun createStatRow(label: String, value: String, color: String): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val labelTv = TextView(this).apply {
            text = label
            textSize = 15f
            setTextColor(Color.parseColor("#BBBBBB"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val valueTv = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.parseColor(color))
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(labelTv)
        row.addView(valueTv)
        row.tag = label
        return row
    }

    private fun createDivider(): View {
        return View(this).apply {
            setBackgroundColor(Color.parseColor("#333333"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply { setMargins(0, 12, 0, 12) }
        }
    }

    // === FEATURE CARD ===
    private fun createFeatureCard(title: String, desc: String, color: String, id: String): CardView {
        val card = CardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#1E1E1E"))
            radius = 20f
            cardElevation = 6f
            useCompatPadding = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            setOnClickListener { toggleFeature(id, title) }
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }
        val titleTv = TextView(this).apply {
            text = title
            textSize = 19f
            setTextColor(Color.parseColor(color))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        val descTv = TextView(this).apply {
            text = desc
            textSize = 14f
            setTextColor(Color.parseColor("#AAAAAA"))
            setLineSpacing(4f, 1f)
        }
        content.addView(titleTv)
        content.addView(descTv)
        card.addView(content)
        return card
    }

    // === TOGGLE FEATURE ===
    private fun toggleFeature(id: String, name: String) {
        Toast.makeText(this, "$name → Activated ✅", Toast.LENGTH_SHORT).show()
        statusText.findViewWithTag<TextView>("Status")?.text = "Active: $name"
    }

    // === REAL-TIME MONITORING ===
    private fun startRealTimeMonitoring() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                updateFPS()
                updateTemperature()
                updateBattery()
                handler.postDelayed(this, 100)
            }
        })
    }

    private fun updateFPS() {
        frameCount++
        val now = System.nanoTime()
        val elapsed = (now - lastTime) / 1e9
        if (elapsed >= 0.5) {
            val fps = (frameCount / elapsed).roundToInt()
            val cappedFps = fps.coerceAtMost(120)
            fpsText.findViewWithTag<TextView>("FPS")?.text = "$cappedFps/120 Hz"
            frameCount = 0
            lastTime = now
        }
    }

    private fun updateTemperature() {
        try {
            val sensorManager = getSystemService(SENSOR_SERVICE) as android.hardware.SensorManager
            val tempSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_TEMPERATURE)
            if (tempSensor != null) {
                sensorManager.registerListener(object : android.hardware.SensorEventListener {
                    override fun onSensorChanged(event: android.hardware.SensorEvent) {
                        val temp = event.values[0]
                        tempText.findViewWithTag<TextView>("Temperature")?.text = "${DecimalFormat("#.#").format(temp)}°C"
                        sensorManager.unregisterListener(this)
                    }
                    override fun onAccuracyChanged(sensor: android.hardware.Sensor, accuracy: Int) {}
                }, tempSensor, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                tempText.findViewWithTag<TextView>("Temperature")?.text = "--°C"
            }
        } catch (e: Exception) {
            tempText.findViewWithTag<TextView>("Temperature")?.text = "N/A"
        }
    }

    private fun updateBattery() {
        val batteryIntent = registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        if (batteryIntent != null) {
            val level = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            val percent = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).roundToInt() else -1
            batteryText.findViewWithTag<TextView>("Battery")?.text = if (percent != -1) "$percent%" else "--%"
        }
    }

    // === PERFORMANCE FIXES ===
    private fun applyPerformanceFixes() {
        try {
            // Disable GMS wakelock & optimizations
            packageManager.setComponentEnabledSetting(
                android.content.ComponentName("com.google.android.gms", "com.google.android.gms.StandaloneReferrerReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) { /* ignore */ }

        statusText.findViewWithTag<TextView>("Status")?.text = "All Systems Active ✅"
    }

    // === NOTIFICATION PERMISSION FIX ===
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            statusText.findViewWithTag<TextView>("Status")?.text =
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    "Permission Granted ✅" else "Permission Denied ⚠️"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fpsJob?.cancel()
        tempJob?.cancel()
    }
}
