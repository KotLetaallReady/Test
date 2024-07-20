package com.example.Test.UI.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class Swiper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val lines = mutableListOf<Line>()
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
    }

    var onPointerDown: ((x: Float, y: Float,  size: Float, pressure: Float) -> Unit)? = null
    var onDrag: ((x: Float, y: Float, size: Float, pressure: Float) -> Unit)? = null
    var onPointerUp: ((x: Float, y: Float,  size: Float, pressure: Float) -> Unit)? = null

    data class Line(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val size: Float,
        val pressure: Float
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (line in lines.toMutableList()) {
            paint.strokeWidth = line.size
            paint.alpha = pressureToAlpha(line.pressure)
            canvas.drawLine(line.startX, line.startY, line.endX, line.endY, paint)
        }
    }

    fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, size: Float, pressure: Float) {
        lines.add(Line(startX, startY, endX, endY, size, pressure))
        invalidate()
    }

    fun clearLines() {
        lines.clear()
        invalidate()
    }

    private fun pressureToAlpha(pressure: Float): Int {
        val minPressure = 0.1f
        val maxPressure = 0.8f
        val normalizedPressure = (pressure - minPressure) / (maxPressure - minPressure)
        val alpha = (255 * (normalizedPressure)).toInt()
        return alpha.coerceIn(0, 255)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var size = 15.0f
        Log.e("drawLinesSw", event.size.toString())

        if(event.size>1)
            size = event.size
        var pressure = 100.0f
        if(event.pressure>0)
            pressure = event.pressure
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onPointerDown?.invoke(event.x, event.y, size, pressure)
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.historySize > 0) {
                    val historicalX = event.getHistoricalX(0)
                    val historicalY = event.getHistoricalY(0)
                    drawLine(historicalX, historicalY, event.x, event.y, event.size, event.pressure)
                }
                onDrag?.invoke(event.x, event.y, size, pressure)
            }
            MotionEvent.ACTION_UP -> {
                onPointerUp?.invoke(event.x, event.y, size, pressure)
            }
        }
        return true
    }
}
