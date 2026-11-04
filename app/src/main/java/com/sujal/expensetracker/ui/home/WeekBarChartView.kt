package com.sujal.expensetracker.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.max

/**
 * Small bar chart drawn for use on a dark/gradient background: translucent white bars,
 * with the last bar (today) solid white and labelled with its amount.
 */
class WeekBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var labels: List<String> = emptyList()
    private var values: List<Double> = emptyList()

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = sp(11f)
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = sp(11f)
        isFakeBoldText = true
        color = Color.WHITE
    }
    private val rect = RectF()

    fun setData(labels: List<String>, values: List<Double>) {
        this.labels = labels
        this.values = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val count = values.size
        if (count == 0) return

        val labelArea = dp(22f)
        val valueArea = dp(18f)
        val left = paddingLeft.toFloat()
        val right = (width - paddingRight).toFloat()
        val chartTop = paddingTop + valueArea
        val chartBottom = height - paddingBottom - labelArea
        val slot = (right - left) / count
        val barWidth = slot * 0.46f
        val radius = barWidth / 2f
        val minBar = dp(6f)
        val maxValue = (values.maxOrNull() ?: 0.0).takeIf { it > 0 } ?: 1.0

        for (i in 0 until count) {
            val isToday = i == count - 1
            val cx = left + slot * i + slot / 2f
            val v = values[i]
            val barHeight = if (v > 0) {
                max(minBar, ((v / maxValue) * (chartBottom - chartTop)).toFloat())
            } else {
                minBar
            }

            barPaint.color = if (isToday) Color.WHITE else 0x59FFFFFF
            rect.set(cx - barWidth / 2f, chartBottom - barHeight, cx + barWidth / 2f, chartBottom)
            canvas.drawRoundRect(rect, radius, radius, barPaint)

            if (isToday && v > 0) {
                canvas.drawText(compact(v), cx, chartBottom - barHeight - dp(6f), valuePaint)
            }

            labelPaint.color = if (isToday) Color.WHITE else 0xB3FFFFFF.toInt()
            labelPaint.isFakeBoldText = isToday
            val label = labels.getOrNull(i) ?: ""
            canvas.drawText(label, cx, height - paddingBottom - dp(5f), labelPaint)
        }
    }

    private fun compact(v: Double): String = when {
        v >= 100_000 -> "₹%.1fL".format(v / 100_000)
        v >= 1_000 -> "₹%.1fk".format(v / 1_000)
        else -> "₹%d".format(v.toLong())
    }

    private fun dp(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

    private fun sp(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
}
