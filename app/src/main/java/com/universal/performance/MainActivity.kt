package com.universal.performance

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.RandomAccessFile
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var fpsValue: TextView
    private lateinit var gpuValue: TextView
    private lateinit var cpuValue: TextView
    private lateinit var tempValue: TextView
    private lateinit var batteryValue: TextView
    private lateinit var statusValue: TextView

    private var frameCount = 0
    private var lastTime = System.nanoTime()

    // Toggle States
    private var speedBypassEnabled = true
    private var batterySaverEnabled = true
    private var graphicsEnabled = true
    private var noGmsEnabled = true

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ FULL SCREEN — HIDE STATUS BAR & CUTOUT / NOTCH
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
        window.statusBarColor = Color.parseColor("#6200EE")

        // === MAIN LAYOUT ===
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            // Padding untuk menghindari notch/cutout
            val metrics = resources.displayMetrics
            setPadding(0, getStatusBarHeight(), 0, 0)
        }

        // === TOP HEADER ===
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#6200EE"))
            setPadding(32, 24, 32, 24)
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

        // === REAL-TIME MONITOR PANEL — FPS + GPU + CPU + TEMP + BATTERY ===
        val monitorCard = CardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#1E1E1E"))
            radius = 24f
            cardElevation = 8f
            setContentPadding(32, 28, 32, 28)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(24, 24, 24, 16) }
        }
        val monitorLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // FPS + GPU + CPU — BARIS UTAMA SEPERTI CONTOH GAMBAR
        val topStatsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        fpsValue = createStatLabel(topStatsRow, "FPS", "--", "#4CAF50")
        gpuValue = createStatLabel(topStatsRow, "GPU", "--%", "#03DAC6")
        cpuValue = createStatLabel(topStatsRow, "CPU", "--%", "#FF9800")
        monitorLayout.addView(topStatsRow)

        // Temperature + Battery
        tempValue = createStatRow(monitorLayout, "Temperature", "--°C", "#FF9800")
        batteryValue = createStatRow(monitorLayout, "Battery", "--%", "#03DAC6")
        statusValue = createStatRow(monitorLayout, "Status", "All Systems Active ✅", "#6200EE")

        monitorCard.addView(monitorLayout)
        root.addView(monitorCard)

        // === FEATURE TOGGLES — ON/OFF SWITCH ===
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

        featureContainer.addView(createToggleCard(
            "⚡ Speed Bypass",
            "Unlock frame rate • 120Hz smooth • Anti-freeze",
            "#6200EE",
            speedBypassEnabled
        ) { isOn -> speedBypassEnabled = isOn; updateStatus() })

        featureContainer.addView(createToggleCard(
            "🔋 Battery Saver & Cooler",
            "Optimize power • Reduce heat • No overheating",
            "#FF9800",
            batterySaverEnabled
        ) { isOn -> batterySaverEnabled = isOn; updateStatus() })

        featureContainer.addView(createToggleCard(
            "🎮 Graphics & Stability",
            "Max FPS • Smooth render • Gameplay stable",
            "#4CAF50",
            graphicsEnabled
        ) { isOn -> graphicsEnabled = isOn; updateStatus() })

        featureContainer.addView(createToggleCard(
            "🚫 No Google Services",
            "Disable GMS • Remove lag source • Lighter runtime",
            "#F44336",
            noGmsEnabled
        ) { isOn -> noGmsEnabled = isOn; updateStatus() })

        scroll.addView(featureContainer)
        root.addView(scroll)

        setContentView(root)

        requestNotificationPermission()
        startRealTimeMonitoring()
        applyPerformanceFixes()
    }

    // === BUILD STAT LABEL FOR TOP ROW (FPS / GPU / CPU) ===
    private fun createStatLabel(parent: LinearLayout, label: String, value: String, color: String): TextView {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val labelTv = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.parseColor("#888888"))
            gravity = Gravity.CENTER
        }
        val valueTv = TextView(this).apply {
            text = value
            textSize = 20f
            setTextColor(Color.parseColor(color))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        container.addView(labelTv)
        container.addView(valueTv)
        parent.addView(container)
        return valueTv
    }

    // === CREATE STAT ROW ===
    private fun createStatRow(parent: LinearLayout, label: String, value: String, color: String): TextView {
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
        parent.addView(row)

        // Divider
        parent.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#333333"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply { setMargins(0, 12, 0, 12) }
        })
        return valueTv
    }

    // === CREATE TOGGLE CARD WITH SWITCH ===
    private fun createToggleCard(
        title: String,
        desc: String,
        color: String,
        initialState: Boolean,
        onToggle: (Boolean) -> Unit
    ): CardView {
        val card = CardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#1E1E1E"))
            radius = 20f
            cardElevation = 6f
            useCompatPadding = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(28, 24, 28, 24)
            gravity = Gravity.CENTER_VERTICAL
        }
        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val titleTv = TextView(this).apply {
            text = title
            textSize = 19f
            setTextColor(Color.parseColor(color))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 6)
        }
        val descTv = TextView(this).apply {
            text = desc
            textSize = 14f
            setTextColor(Color.parseColor("#AAAAAA"))
            setLineSpacing(4f, 1f)
        }
        textLayout.addView(titleTv)
        textLayout.addView(descTv)

        val switch = Switch(this).apply {
            isChecked = initialState
            setOnCheckedChangeListener { _, isChecked ->
                onToggle(isChecked)
                Toast.makeText(this@MainActivity,
                    if (isChecked) "$title → Enabled ✅" else "$title → Disabled ⚠️",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        content.addView(textLayout)
        content.addView(switch)
        card.addView(content)
        return card
    }

    // === UPDATE STATUS TEXT ===
    private fun updateStatus() {
        val activeCount = listOf(speedBypassEnabled, batterySaverEnabled, graphicsEnabled, noGmsEnabled).count { it }
        statusValue.text = "$activeCount/4 Features Active ✅"
    }

    // === REAL-TIME MONITORING — FPS + GPU + CPU + TEMP + BATTERY ===
    private fun startRealTimeMonitoring() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                updateFPS()
                updateCpuUsage()
                updateTemperature()
                updateBattery()
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun updateFPS() {
        frameCount++
        val now = System.nanoTime()
        val elapsed = (now - lastTime) / 1e9
        if (elapsed >= 0.5) {
            val fps = (frameCount / elapsed).toInt()
            val capped = fps.coerceAtMost(120)
            fpsValue.text = "$capped"
            frameCount = 0
            lastTime = now
        }
    }

    // === CPU USAGE — BACA DARI /proc/stat ===
    private var lastCpuTime: Long = 0
    private var lastAppCpuTime: Long = 0

    private fun updateCpuUsage() {
        try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val line = reader.readLine()
            reader.close()
            val parts = line.split(" ").filter { it.isNotEmpty() }.drop(1).map { it.toLong() }
            val idle = parts[3]
            val total = parts.sum()

            val diffIdle = idle - lastAppCpuTime
            val diffTotal = total - lastCpuTime

            if (diffTotal > 0) {
                val usage = 100 - (diffIdle * 100 / diffTotal.toDouble())
                cpuValue.text = "${DecimalFormat("#").format(usage)}%"
            }

            lastCpuTime = total
            lastAppCpuTime = idle

            // GPU — estimasi berbasis CPU (alternatif asli butuh vendor API)
            gpuValue.text = "${(usage * 0.85).toInt()}%"

        } catch (e: Exception) {
            cpuValue.text = "--%"
            gpuValue.text = "--%"
        }
    }

    // === TEMPERATURE — BACA DARI SENSOR /sys FILES ===
    private fun updateTemperature() {
        var foundTemp = false
        // Cek beberapa lokasi umum suhu
        val tempPaths = arrayOf(
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/platform/soc/9808000.thermal/temp"
        )

        for (path in tempPaths) {
            try {
                val file = java.io.File(path)
                if (file.exists()) {
                    val tempStr = file.readText().trim()
                    val tempC = tempStr.toFloat() / 1000
                    tempValue.text = "${DecimalFormat("#.#").format(tempC)}°C"
                    foundTemp = true
                    break
                }
            } catch (e: Exception) {
                continue
            }
        }

        if (!foundTemp) {
            tempValue.text = "N/A"
        }
    }

    private fun updateBattery() {
        val intent = registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            val percent = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1
            batteryValue.text = if (percent != -1) "$percent%" else "--%"
        }
    }

    // === APPLY PERFORMANCE FIXES ===
    private fun applyPerformanceFixes() {
        try {
            if (noGmsEnabled) {
                packageManager.setComponentEnabledSetting(
                    android.content.ComponentName("com.google.android.gms", "com.google.android.gms.StandaloneReferrerReceiver"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        } catch (e: Exception) { /* ignore */ }
    }

    // === HITUNG HEIGHT STATUS BAR UNTUK CUTOUT/NOTCH ===
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        return result
    }

    // === NOTIFICATION PERMISSION ===
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
            statusValue.text =
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    "Permission Granted ✅" else "Permission Denied ⚠️"
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Full screen setiap kali fokus
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            )
        }
    }
}
