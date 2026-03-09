package com.audiometry.threshold.models

data class TrialResult(
    val trial: Int,
    val db: Int,
    val isiMs: Int,
    val windowMs: Int,
    val track: Track,
    val heard: Boolean,
    val stats: String,
    val note: String
)

data class SeriesPoint(
    val trial: Int,
    val db: Int,
    val heard: Boolean
)

data class StatsEntry(
    var present: Int = 0,
    var heard: Int = 0
)

enum class Track {
    NONE, DOWN, UP;

    override fun toString(): String = when (this) {
        NONE -> "—"
        DOWN -> "downTrack"
        UP -> "upTrack"
    }
}

enum class ExamState {
    READY, RUNNING, PRESENTING, AWAITING, STOPPED, DONE;

    fun toDisplayString(): String = when (this) {
        READY -> "pronto"
        RUNNING -> "iniciado"
        PRESENTING -> "apresentando"
        AWAITING -> "aguardando"
        STOPPED -> "parado"
        DONE -> "limiar definido"
    }
}

data class ExamConfig(
    val frequencyHz: Int = 1000,
    val startDb: Int = 40,
    val responseWindowMax: Int = 1500,
    val safetyMargin: Int = 200,
    val toneDurationMs: Int = 350,
    val downStepDb: Int = 10,
    val upStepDb: Int = 5,
    val volume: Int = 70
)

data class TimingInfo(
    val isiMs: Int,
    val soaMs: Int,
    val windowEffective: Int
)
