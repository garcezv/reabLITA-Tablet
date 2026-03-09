package com.audiometry.threshold

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SpeedometerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var maxValue = 90f
    private var currentValue = 0f
    private var isActive = false

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#212121")
        strokeCap = Paint.Cap.ROUND
    }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#212121")
    }
    private val bgArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E0E0E0")
        strokeCap = Paint.Cap.BUTT
    }

    fun setValue(v: Float, active: Boolean = true) {
        currentValue = v.coerceIn(0f, maxValue)
        isActive = active
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height.toFloat()
        val radius = min(width.toFloat(), height * 2f) / 2f * 0.82f
        val strokeW = radius * 0.20f

        arcPaint.strokeWidth = strokeW
        bgArcPaint.strokeWidth = strokeW

        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        if (!isActive) {
            bgArcPaint.color = Color.parseColor("#E0E0E0")
            canvas.drawArc(rect, 180f, 180f, false, bgArcPaint)
        } else {
            // Green zone: 180° → 240° (0–33% of max = 0–30 dB)
            arcPaint.color = Color.parseColor("#4CAF50")
            canvas.drawArc(rect, 180f, 60f, false, arcPaint)

            // Yellow zone: 240° → 300° (33–66% = 30–60 dB)
            arcPaint.color = Color.parseColor("#FFC107")
            canvas.drawArc(rect, 240f, 60f, false, arcPaint)

            // Red zone: 300° → 360° (66–100% = 60–90 dB)
            arcPaint.color = Color.parseColor("#F44336")
            canvas.drawArc(rect, 300f, 60f, false, arcPaint)
        }

        // Needle
        val angleD = 180.0 + (currentValue / maxValue) * 180.0
        val rad = Math.toRadians(angleD)
        val needleLen = radius * 0.76f
        val tipX = (cx + needleLen * cos(rad)).toFloat()
        val tipY = (cy + needleLen * sin(rad)).toFloat()

        needlePaint.strokeWidth = radius * 0.045f
        canvas.drawLine(cx, cy, tipX, tipY, needlePaint)

        // Center circle
        canvas.drawCircle(cx, cy, radius * 0.08f, centerPaint)
    }
}
