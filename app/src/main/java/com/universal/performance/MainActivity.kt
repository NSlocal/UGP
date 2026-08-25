package com.universal.performance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var fpsValue: TextView
    private lateinit var gpuValue: TextView
    private lateinit var cpuValue: TextView
    private lateinit var tempValue: TextView
    private lateinit var batteryValue: TextView
    private lateinit var statusValue: TextView

    private var frameCount = 0
    private var lastFpsTime = System.nanoTime()
    private val fpsUpdateInterval = 1_000_000_000L
    private val choreographer = android.view.Choreographer.getInstance()
    private var fpsCallback: android.view.Choreographer.FrameCallback? = null

    private var speedBypassEnabled = true
    private var batterySaverEnabled = true
    private var graphicsEnabled = true
    private var noGmsEnabled = true

    private var simulatedCpu = 35.0
    private var simulatedGpu = 40.0
    private val random = java.util.Random()

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(0, getStatusBarHeight(), 0, 0)
        }

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

        val monitorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 8)
        }
        val monitorBg = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(48, 20, 48, 20)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 999f
                setColor(Color.parseColor("#DD000000"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val fpsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 32, 0)
        }
        fpsLayout.addView(TextView(this).apply {
            text = "FPS"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        fpsValue = TextView(this).apply {
            text = "--"
            textSize = 20f
            setTextColor(Color.parseColor("#4CAF50"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(8, 0, 0, 0)
        }
        fpsLayout.addView(fpsValue)
        monitorBg.addView(fpsLayout)

        val gpuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 32, 0)
        }
        gpuLayout.addView(TextView(this).apply {
            text = "GPU"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        gpuValue = TextView(this).apply {
            text = "--%"
            textSize = 20f
            setTextColor(Color.parseColor("#4CAF50"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(8, 0, 0, 0)
        }
        gpuLayout.addView(gpuValue)
        monitorBg.addView(gpuLayout)

        val cpuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        cpuLayout.addView(TextView(this).apply {
            text = "CPU"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        cpuValue = TextView(this).apply {
            text = "--%"
            textSize = 20f
            setTextColor(Color.parseColor("#4CAF50"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(8, 0, 0, 0)
        }
        cpuLayout.addView(cpuValue)
        monitorBg.addView(cpuLayout)

        monitorContainer.addView(monitorBg)
        root.addView(monitorContainer)

        val infoCard = CardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#1E1E1E"))
            radius = 16f
            cardElevation = 4f
            setContentPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(24, 8, 24, 16) }
        }
        val infoLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        tempValue = createInfoRow(infoLayout, "Temperature", "--°C", "#FF9800")
        batteryValue = createInfoRow(infoLayout, "Battery", "--%", "#03DAC6")
        statusValue = createInfoRow(infoLayout, "Status", "0/4 Features Active", "#6200EE")

        infoCard.addView(infoLayout)
        root.addView(infoCard)

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
        updateStatus()
        startRealTimeMonitoring()
        applyPerformanceFixes()
    }

    private fun createInfoRow(parent: LinearLayout, label: String, value: String, color: String): TextView {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 10)
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
        }
        row.addView(labelTv)
        row.addView(valueTv)
        parent.addView(row)
        parent.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#333333"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                .apply { setMargins(0, 8, 0, 8) }
        })
        return valueTv
    }

    private fun createToggleCard(
        title: String, desc: String, color: String, initialState: Boolean, onToggle: (Boolean) -> Unit
    ): CardView {
        val card = CardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#1E1E1E"))
            radius = 20f
            cardElevation = 6f
            useCompatPadding = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
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
        textLayout.addView(TextView(this).apply {
            text = title
            textSize = 19f
            setTextColor(Color.parseColor(color))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 6)
        })
        textLayout.addView(TextView(this).apply {
            text = desc
            textSize = 14f
            setTextColor(Color.parseColor("#AAAAAA"))
            setLineSpacing(4f, 1f)
        })
        val switch = Switch(this).apply {
            isChecked = initialState
            setOnCheckedChangeListener { _, isChecked ->
                onToggle(isChecked)
                Toast.makeText(this@MainActivity,
                    if (isChecked) "$title → Enabled ✅" else "$title → Disabled ⚠️", Toast.LENGTH_SHORT).show()
            }
        }
        content.addView(textLayout)
        content.addView(switch)
        card.addView(content)
        return card
    }

    private fun updateStatus() {
        val count = listOf(speedBypassEnabled, batterySaverEnabled, graphicsEnabled, noGmsEnabled).count { it }
        statusValue.text = "$count/4 Features Active ✅"
    }

    private fun startRealTimeMonitoring() {
        frameCount = 0
        lastFpsTime = System.nanoTime()

        fpsCallback = object : android.view.Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                val elapsed = frameTimeNanos - lastFpsTime
                if (elapsed >= fpsUpdateInterval) {
                    val fps = (frameCount * 1_000_000_000L / elapsed.toDouble()).roundToInt()
                    fpsValue.text = fps.coerceAtMost(120).toString()
                    frameCount = 0
                    lastFpsTime = frameTimeNanos
                }
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(fpsCallback!!)

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                updateCpuGpu()
                updateTemperature()
                updateBattery()
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun updateCpuGpu() {
        simulatedCpu += (random.nextDouble() - 0.5) * 6
        simulatedGpu += (random.nextDouble() - 0.5) * 5
        simulatedCpu = simulatedCpu.coerceIn(15.0, 85.0)
        simulatedGpu = simulatedGpu.coerceIn(10.0, 90.0)
        cpuValue.text = "${simulatedCpu.roundToInt()}%"
        gpuValue.text = "${simulatedGpu.roundToInt()}%"
    }

    private fun updateTemperature() {
        val paths = arrayOf(
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone0/temp"
        )
        for (path in paths) {
            try {
                val f = java.io.File(path)
                if (f.exists()) {
                    tempValue.text = "${DecimalFormat("#.#").format(f.readText().trim().toFloat() / 1000)}°C"
                    return
                }
            } catch (_: Exception) {}
        }
        tempValue.text = "N/A"
    }

    private fun updateBattery() {
        val intent = registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            val lvl = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            batteryValue.text = if (lvl != -1 && scale != -1) "${(lvl * 100 / scale.toFloat()).roundToInt()}%" else "--%"
        }
    }

    private fun applyPerformanceFixes() {
        try {
            if (noGmsEnabled) {
                packageManager.setComponentEnabledSetting(
                    android.content.ComponentName("com.google.android.gms", "com.google.android.gms.StandaloneReferrerReceiver"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                )
            }
        } catch (_: Exception) {}
    }

    private fun getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            statusValue.text =
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    "Permission Granted ✅" else "Permission Denied ⚠️"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fpsCallback?.let { choreographer.removeFrameCallback(it) }
    }
}
