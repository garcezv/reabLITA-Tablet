package com.audiometry.threshold

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), ThresholdExam.ExamCallback {

    private lateinit var spinnerFrequency: Spinner
    private lateinit var editStartDb: EditText
    private lateinit var editWindowMax: EditText
    private lateinit var editSafetyMargin: EditText
    private lateinit var editToneDuration: EditText
    private lateinit var editDownStep: EditText
    private lateinit var editUpStep: EditText
    private lateinit var seekBarVolume: SeekBar
    private lateinit var tvVolumeValue: TextView

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnHeard: Button

    private lateinit var tvState: TextView
    private lateinit var tvTrial: TextView
    private lateinit var tvDb: TextView
    private lateinit var tvTrack: TextView
    private lateinit var tvWindow: TextView
    private lateinit var tvIsi: TextView
    private lateinit var tvSoa: TextView
    private lateinit var tvCandidate: TextView
    private lateinit var tvResult: TextView
    private lateinit var progressWindow: ProgressBar

    private lateinit var recyclerLog: RecyclerView
    private lateinit var chartView: ChartView

    private lateinit var logAdapter: LogAdapter
    private lateinit var audioGenerator: AudioToneGenerator
    private var exam: ThresholdExam? = null

    private val frequencies = arrayOf(1000, 500, 2000, 4000, 8000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinner()
        setupSeekBar()
        setupRecyclerView()
        setupButtons()

        audioGenerator = AudioToneGenerator()
    }

    private fun initViews() {
        spinnerFrequency = findViewById(R.id.spinnerFrequency)
        editStartDb = findViewById(R.id.editStartDb)
        editWindowMax = findViewById(R.id.editWindowMax)
        editSafetyMargin = findViewById(R.id.editSafetyMargin)
        editToneDuration = findViewById(R.id.editToneDuration)
        editDownStep = findViewById(R.id.editDownStep)
        editUpStep = findViewById(R.id.editUpStep)
        seekBarVolume = findViewById(R.id.seekBarVolume)
        tvVolumeValue = findViewById(R.id.tvVolumeValue)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnHeard = findViewById(R.id.btnHeard)

        tvState = findViewById(R.id.tvState)
        tvTrial = findViewById(R.id.tvTrial)
        tvDb = findViewById(R.id.tvDb)
        tvTrack = findViewById(R.id.tvTrack)
        tvWindow = findViewById(R.id.tvWindow)
        tvIsi = findViewById(R.id.tvIsi)
        tvSoa = findViewById(R.id.tvSoa)
        tvCandidate = findViewById(R.id.tvCandidate)
        tvResult = findViewById(R.id.tvResult)
        progressWindow = findViewById(R.id.progressWindow)

        recyclerLog = findViewById(R.id.recyclerLog)
        chartView = findViewById(R.id.chartView)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            frequencies.map { "$it Hz" }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrequency.adapter = adapter
        spinnerFrequency.setSelection(0)
    }

    private fun setupSeekBar() {
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvVolumeValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupRecyclerView() {
        logAdapter = LogAdapter()
        recyclerLog.adapter = logAdapter
        recyclerLog.layoutManager = LinearLayoutManager(this)
    }

    private fun setupButtons() {
        btnStart.setOnClickListener { startExam() }
        btnStop.setOnClickListener { exam?.stop() }
        btnHeard.setOnClickListener { exam?.onHeardButtonPressed() }
    }

    private fun startExam() {
        val config = ExamConfig(
            frequencyHz = frequencies[spinnerFrequency.selectedItemPosition],
            startDb = editStartDb.text.toString().toIntOrNull() ?: 40,
            downStep = editDownStep.text.toString().toIntOrNull() ?: 10,
            upStep = editUpStep.text.toString().toIntOrNull() ?: 5,
            toneMs = editToneDuration.text.toString().toIntOrNull() ?: 350,
            respWindowMax = editWindowMax.text.toString().toIntOrNull() ?: 1500,
            safetyMargin = editSafetyMargin.text.toString().toIntOrNull() ?: 200,
            volume = seekBarVolume.progress / 100f
        )

        logAdapter.clear()
        chartView.clear()
        tvState.text = getString(R.string.state_ready)
        tvTrial.text = "# 0"
        tvDb.text = "dB —"
        tvTrack.text = "Track —"
        tvWindow.text = "— ms"
        tvIsi.text = "ISI —"
        tvSoa.text = "SOA —"
        tvCandidate.text = "Cand. —"
        tvResult.text = "Resultado —"
        progressWindow.progress = 0

        btnStart.isEnabled = false
        btnStop.isEnabled = true
        btnHeard.isEnabled = false

        exam = ThresholdExam(config, audioGenerator, this)
        exam?.start()
    }

    override fun onStateChanged(state: String) {
        runOnUiThread {
            tvState.text = state
        }
    }

    override fun onTrialStarted(trial: Int, db: Int, track: String) {
        runOnUiThread {
            tvTrial.text = "# $trial"
            tvDb.text = "$db dB HL"
            tvTrack.text = "Track $track"
            progressWindow.progress = 0
            tvWindow.text = "— ms"
            btnHeard.isEnabled = true
        }
    }

    override fun onWindowUpdate(msLeft: Int, percentage: Float) {
        runOnUiThread {
            if (msLeft < 0) {
                tvWindow.text = "— ms"
                progressWindow.progress = 0
                btnHeard.isEnabled = false
            } else {
                tvWindow.text = "$msLeft ms"
                progressWindow.progress = (percentage * 10f).toInt().coerceIn(0, 1000)
            }
        }
    }

    override fun onTimingInfo(isi: Int, soa: Int, windowEff: Int) {
        runOnUiThread {
            tvIsi.text = "ISI ${isi}ms"
            tvSoa.text = "SOA ${soa}ms"
        }
    }

    override fun onLogEntry(entry: LogEntry) {
        runOnUiThread {
            logAdapter.addEntry(entry)
            recyclerLog.scrollToPosition(logAdapter.itemCount - 1)
        }
    }

    override fun onSeriesPoint(point: SeriesPoint) {
        runOnUiThread {
            chartView.addPoint(point)
        }
    }

    override fun onCandidateUpdate(db: Int?, heard: Int, present: Int) {
        runOnUiThread {
            if (db != null) {
                tvCandidate.text = "Cand. $db dB • $heard/$present"
            } else {
                tvCandidate.text = "Cand. —"
            }
        }
    }

    override fun onThresholdFound(db: Int) {
        runOnUiThread {
            tvResult.text = getString(R.string.threshold_result, db)
            chartView.setThreshold(db)
        }
    }

    override fun onExamComplete() {
        runOnUiThread {
            btnStart.isEnabled = true
            btnStop.isEnabled = false
            btnHeard.isEnabled = false
            progressWindow.progress = 0
            tvWindow.text = "— ms"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exam?.stop()
        audioGenerator.release()
    }
}
