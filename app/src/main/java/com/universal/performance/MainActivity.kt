package com.universal.performance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "UGP App Berhasil! 🎉"
        textView.textSize = 24f
        setContentView(textView)
    }
}
