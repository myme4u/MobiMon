package com.example.mobimon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

class TriangleOverlayView(context: Context, initialColor: Int) : View(context) {

    var color: Int = initialColor
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = initialColor
        style = Paint.Style.FILL
    }
    private val path = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()
        path.moveTo(w / 2f, 0f)
        path.lineTo(w.toFloat(), h.toFloat())
        path.lineTo(0f, h.toFloat())
        path.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }
}
