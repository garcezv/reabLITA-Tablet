package com.audiometry.threshold

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val series = mutableListOf<SeriesPoint>()
    private var thresholdDb: Int? = null

    private val density = context.resources.displayMetrics.density
    private val sp = context.resources.displayMetrics.scaledDensity

    private val paintGrid = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_line)
        strokeWidth = 1.5f * density
        style = Paint.Style.STROKE
    }

    private val paintLine = Paint().apply {
        color = ContextCompat.getColor(context, R.color.text_primary)
        strokeWidth = 3f * density
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paintPointOk = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_ok)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintPointBad = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_bad)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintPointStroke = Paint().apply {
        color = ContextCompat.getColor(context, R.color.bg_primary)
        strokeWidth = 3f * density
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = ContextCompat.getColor(context, R.color.text_muted)
        textSize = 12f * sp
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
    }

    private val paintAxisLabel = Paint().apply {
        color = ContextCompat.getColor(context, R.color.text_muted)
        textSize = 11f * sp
        isAntiAlias = true
    }

    private val paintThreshold = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_ok)
        strokeWidth = 2.5f * density
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(16f * density, 10f * density), 0f)
    }

    private val paintThresholdText = Paint().apply {
        color = ContextCompat.getColor(context, R.color.color_ok)
        textSize = 13f * sp
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val yMin = -10
    private val yMax = 90

    fun setSeries(newSeries: List<SeriesPoint>) {
        series.clear()
        series.addAll(newSeries)
        invalidate()
    }

    fun addPoint(point: SeriesPoint) {
        series.add(point)
        invalidate()
    }

    fun setThreshold(db: Int?) {
        thresholdDb = db
        invalidate()
    }

    fun clear() {
        series.clear()
        thresholdDb = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padL = 52f * density
        val padR = 18f * density
        val padT = 28f * density
        val padB = 40f * density

        val plotW = width - padL - padR
        val plotH = height - padT - padB

        if (plotW <= 0 || plotH <= 0) return

        // Grid horizontal (linhas de dB)
        for (db in yMin..yMax step 10) {
            val y = padT + (yMax - db) * plotH / (yMax - yMin)
            canvas.drawLine(padL, y, padL + plotW, y, paintGrid)
            canvas.drawText(db.toString(), 8f * density, y + paintText.textSize * 0.4f, paintText)
        }

        // Eixo X
        canvas.drawLine(padL, padT + plotH, padL + plotW, padT + plotH, paintGrid)

        // Rótulos dos eixos
        canvas.drawText("Intensidade (dB HL)", padL, padT - 8f * density, paintAxisLabel)
        canvas.drawText("Apresentação (#)", padL, height - 6f * density, paintAxisLabel)

        if (series.isEmpty()) return

        val n = series.size

        fun xOf(i: Int): Float =
            if (n == 1) padL + plotW / 2f
            else padL + i * plotW / (n - 1)

        fun yOf(db: Int): Float =
            padT + (yMax - db) * plotH / (yMax - yMin)

        // Linha conectando pontos
        if (series.size > 1) {
            val path = Path()
            series.forEachIndexed { i, point ->
                val x = xOf(i)
                val y = yOf(point.db)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, paintLine)
        }

        // Pontos (círculos)
        val radius = 7f * density
        series.forEachIndexed { i, point ->
            val x = xOf(i)
            val y = yOf(point.db)
            val fill = if (point.heard) paintPointOk else paintPointBad
            canvas.drawCircle(x, y, radius, fill)
            canvas.drawCircle(x, y, radius, paintPointStroke)
        }

        // Linha de limiar
        thresholdDb?.let { thresh ->
            val yT = yOf(thresh)
            canvas.drawLine(padL, yT, padL + plotW, yT, paintThreshold)
            canvas.drawText(
                "Limiar: $thresh dB HL",
                padL + 12f * density,
                yT - 6f * density,
                paintThresholdText
            )
        }
    }
}
