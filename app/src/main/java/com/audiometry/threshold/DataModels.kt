package com.audiometry.threshold

data class LogEntry(
    val trial: Int,
    val db: Int,
    val isi: Int,
    val window: Int,
    val track: String,
    val heard: Boolean,
    val stat: String,
    val note: String
)

data class SeriesPoint(
    val trial: Int,
    val db: Int,
    val heard: Boolean
)

data class ExamConfig(
    val frequencyHz: Int,
    val startDb: Int,
    val downStep: Int,
    val upStep: Int,
    val toneMs: Int,
    val respWindowMax: Int,
    val safetyMargin: Int,
    val volume: Float
)
