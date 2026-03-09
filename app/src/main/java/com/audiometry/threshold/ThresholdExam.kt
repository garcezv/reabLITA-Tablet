package com.audiometry.threshold

import android.os.Handler
import android.os.Looper
import kotlin.random.Random

class ThresholdExam(
    private val config: ExamConfig,
    private val audioGenerator: AudioToneGenerator,
    private val callback: ExamCallback
) {
    interface ExamCallback {
        fun onStateChanged(state: String)
        fun onTrialStarted(trial: Int, db: Int, track: String)
        fun onWindowUpdate(msLeft: Int, percentage: Float)
        fun onTimingInfo(isi: Int, soa: Int, windowEff: Int)
        fun onLogEntry(entry: LogEntry)
        fun onSeriesPoint(point: SeriesPoint)
        fun onCandidateUpdate(db: Int?, heard: Int, present: Int)
        fun onThresholdFound(db: Int)
        fun onExamComplete()
    }
    
    private val handler = Handler(Looper.getMainLooper())
    
    private var running = false
    private var awaiting = false
    private var trial = 0
    private var currentDb = config.startDb
    private var track = "—"
    private var lastDb: Int? = null
    
    private var lastISI: Int? = null
    private var lastSOA: Int? = null
    private var lastWindowEff: Int? = null
    
    private val stats = mutableMapOf<Int, Pair<Int, Int>>() // db -> (present, heard)
    private var thresholdDb: Int? = null
    
    private val series = mutableListOf<SeriesPoint>()
    
    private var windowRunnable: Runnable? = null
    private var tickRunnable: Runnable? = null
    private var nextRunnable: Runnable? = null
    
    private var windowStartTime = 0L
    
    fun start() {
        if (running) return
        
        running = true
        trial = 0
        currentDb = config.startDb.coerceIn(-10, 90)
        track = "—"
        lastDb = null
        lastISI = null
        lastSOA = null
        lastWindowEff = null
        stats.clear()
        thresholdDb = null
        series.clear()
        awaiting = false
        
        audioGenerator.setMasterVolume(config.volume)
        callback.onStateChanged("iniciado")
        
        present()
    }
    
    fun stop() {
        if (!running) return
        
        running = false
        awaiting = false
        clearTimers()
        callback.onStateChanged("parado")
        callback.onExamComplete()
    }
    
    fun onHeardButtonPressed() {
        if (!running || !awaiting) return
        handleResponse(true)
    }
    
    private fun clearTimers() {
        windowRunnable?.let { handler.removeCallbacks(it) }
        tickRunnable?.let { handler.removeCallbacks(it) }
        nextRunnable?.let { handler.removeCallbacks(it) }
        windowRunnable = null
        tickRunnable = null
        nextRunnable = null
    }
    
    private fun clampDb(db: Int) = db.coerceIn(-10, 90)
    
    private fun computeTimingForThisTrial() {
        val isi = Random.nextInt(1000, 3001) // 1000-3000 ms
        val soa = config.toneMs + isi
        val margin = config.safetyMargin.coerceAtLeast(0)
        
        var winEff = (config.respWindowMax).coerceAtMost(soa - margin)
        winEff = winEff.coerceAtLeast(250)
        winEff = winEff.coerceAtMost((soa - margin).coerceAtLeast(250))
        
        lastISI = isi
        lastSOA = soa
        lastWindowEff = winEff
        
        callback.onTimingInfo(isi, soa, winEff)
    }
    
    private fun present() {
        if (!running) return
        
        trial++
        lastDb = clampDb(currentDb)
        
        computeTimingForThisTrial()
        
        callback.onStateChanged("apresentando")
        callback.onTrialStarted(trial, lastDb!!, track)
        
        audioGenerator.playTone(config.frequencyHz, lastDb!!, config.toneMs)
        openResponseWindow()
    }
    
    private fun openResponseWindow() {
        awaiting = true
        val totalMs = lastWindowEff!!
        windowStartTime = System.currentTimeMillis()
        
        // Tick para atualizar UI
        fun tick() {
            if (!awaiting) return
            
            val elapsed = System.currentTimeMillis() - windowStartTime
            val left = (totalMs - elapsed).coerceAtLeast(0)
            val percentage = (elapsed.toFloat() / totalMs * 100f).coerceAtMost(100f)
            
            callback.onWindowUpdate(left.toInt(), percentage)
            
            if (left > 0) {
                tickRunnable = Runnable { tick() }
                handler.postDelayed(tickRunnable!!, 50)
            }
        }
        tick()
        
        // Timer para timeout
        windowRunnable = Runnable {
            if (!awaiting) return@Runnable
            awaiting = false
            callback.onWindowUpdate(0, 100f)
            handleResponse(false)
        }
        handler.postDelayed(windowRunnable!!, totalMs.toLong())
    }
    
    private fun handleResponse(heard: Boolean) {
        if (!running) return
        
        // Fecha janela
        if (awaiting) {
            awaiting = false
            clearTimers()
            callback.onWindowUpdate(-1, 0f)
        }
        
        val db = lastDb!!
        
        // Registra série
        val point = SeriesPoint(trial, db, heard)
        series.add(point)
        callback.onSeriesPoint(point)
        
        // Stats no upTrack
        val statTxt = updateStatsIfUpTrack(db, heard)
        
        // Log
        val note = if (heard) "Ouviu: desce -${config.downStep} dB" else "Não ouviu: sobe +${config.upStep} dB"
        val entry = LogEntry(
            trial = trial,
            db = db,
            isi = lastISI!!,
            window = lastWindowEff!!,
            track = track,
            heard = heard,
            stat = if (track == "upTrack") statTxt else "—",
            note = note
        )
        callback.onLogEntry(entry)
        
        // Checa parada
        if (thresholdDb != null) {
            callback.onStateChanged("limiar definido")
            callback.onThresholdFound(thresholdDb!!)
            stop()
            return
        }
        
        // Aplica regra 10↓ / 5↑
        if (heard) {
            currentDb = clampDb(db - config.downStep)
            track = "downTrack"
        } else {
            currentDb = clampDb(db + config.upStep)
            track = "upTrack"
        }
        
        callback.onStateChanged("aguardando")
        scheduleNext()
    }
    
    private fun updateStatsIfUpTrack(db: Int, heard: Boolean): String {
        if (track != "upTrack") return "—"
        
        val cur = stats[db] ?: (0 to 0)
        val newPresent = cur.first + 1
        val newHeard = cur.second + if (heard) 1 else 0
        stats[db] = newPresent to newHeard
        
        // Atualiza candidato
        val candDb = stats.keys.minOrNull()
        if (candDb != null) {
            val cand = stats[candDb]!!
            callback.onCandidateUpdate(candDb, cand.second, cand.first)
        }
        
        // Parada: 2/3
        if (newPresent >= 3 && newHeard >= 2) {
            thresholdDb = db
        }
        
        return "$newHeard/$newPresent @ $db dB"
    }
    
    private fun scheduleNext() {
        if (!running) return
        
        val delayToNextOnset = lastSOA!!.toLong()
        nextRunnable = Runnable { present() }
        handler.postDelayed(nextRunnable!!, delayToNextOnset)
    }
    
    fun getSeries() = series.toList()
    fun getThreshold() = thresholdDb
}
