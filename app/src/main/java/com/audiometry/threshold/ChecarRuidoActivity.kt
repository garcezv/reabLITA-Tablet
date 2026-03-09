package com.audiometry.threshold

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.random.Random

class ChecarRuidoActivity : BaseActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var isMeasuring = false

    private lateinit var speedometerView: SpeedometerView
    private lateinit var tvNoiseLevel: TextView
    private lateinit var tvNoiseStatus: TextView
    private lateinit var btnToggle: Button

    private val noiseRunnable = object : Runnable {
        override fun run() {
            if (!isMeasuring) return
            val db = 15f + Random.nextFloat() * 60f
            val dbStr = String.format("%.1f dB", db)
            tvNoiseLevel.text = dbStr
            speedometerView.setValue(db, active = true)
            if (db <= 30f) {
                tvNoiseStatus.text = getString(R.string.noise_acceptable)
                tvNoiseStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            } else {
                tvNoiseStatus.text = getString(R.string.noise_too_high)
                tvNoiseStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            }
            handler.postDelayed(this, 600)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checar_ruido)

        setupLanguageSelector(
            findViewById<LinearLayout>(R.id.llLanguageSelector),
            findViewById<TextView>(R.id.tvLanguageLabel)
        )

        speedometerView = findViewById(R.id.speedometerView)
        tvNoiseLevel = findViewById(R.id.tvNoiseLevel)
        tvNoiseStatus = findViewById(R.id.tvNoiseStatus)
        btnToggle = findViewById(R.id.btnToggleCheck)

        speedometerView.maxValue = 90f

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            openSideMenu()
        }

        btnToggle.setOnClickListener {
            if (isMeasuring) stopMeasuring() else startMeasuring()
        }
    }

    private fun startMeasuring() {
        isMeasuring = true
        btnToggle.text = getString(R.string.btn_stop_check)
        tvNoiseStatus.text = getString(R.string.noise_measuring)
        tvNoiseStatus.setTextColor(getColor(android.R.color.darker_gray))
        handler.post(noiseRunnable)
    }

    private fun stopMeasuring() {
        isMeasuring = false
        handler.removeCallbacks(noiseRunnable)
        btnToggle.text = getString(R.string.btn_start_check)
        tvNoiseLevel.text = "-- dB"
        tvNoiseStatus.text = ""
        speedometerView.setValue(0f, active = false)
    }

    override fun onStop() {
        super.onStop()
        stopMeasuring()
    }
}
