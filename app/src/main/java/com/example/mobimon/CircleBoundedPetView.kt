package com.example.mobimon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

class CircleBoundedPetView(
    context: Context,
    initialColor: Int,
    private val petSizePx: Float
) : View(context) {

    var color: Int = initialColor
        set(value) {
            field = value
            petPaint.color = value
            invalidate()
        }

    var onHandleDrag: ((dx: Float, dy: Float) -> Unit)? = null

    private var petOffsetX = 0f
    private var petOffsetY = 0f
    private var isDraggingHandle = false
    private var lastRawX = 0f
    private var lastRawY = 0f
    private val handleRect = RectF()

    private val density = context.resources.displayMetrics.density
    private val strokeWidthPx = STROKE_WIDTH_DP * density
    private val handleTopMarginPx = HANDLE_TOP_MARGIN_DP * density
    private val handleHeightPx = HANDLE_HEIGHT_DP * density
    private val handleHalfWidthPx = HANDLE_HALF_WIDTH_DP * density
    private val handleDotRadiusPx = HANDLE_DOT_RADIUS_DP * density
    private val handleDotSpacingPx = HANDLE_DOT_SPACING_DP * density

    private val boundaryFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFF5F5F5.toInt()
    }
    private val boundaryStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        color = 0xFFBDBDBD.toInt()
    }
    private val handleDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF9E9E9E.toInt()
    }
    private val petPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = initialColor
        style = Paint.Style.FILL
    }
    private val petPath = Path().apply {
        moveTo(petSizePx / 2f, 0f)
        lineTo(petSizePx, petSizePx)
        lineTo(0f, petSizePx)
        close()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX = w / 2f
        handleRect.set(
            centerX - handleHalfWidthPx,
            handleTopMarginPx,
            centerX + handleHalfWidthPx,
            handleTopMarginPx + handleHeightPx
        )
    }

    fun setPetOffset(x: Float, y: Float) {
        petOffsetX = x
        petOffsetY = y
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDraggingHandle = handleRect.contains(event.x, event.y)
                if (isDraggingHandle) {
                    lastRawX = event.rawX
                    lastRawY = event.rawY
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDraggingHandle) {
                    val dx = event.rawX - lastRawX
                    val dy = event.rawY - lastRawY
                    lastRawX = event.rawX
                    lastRawY = event.rawY
                    onHandleDrag?.invoke(dx, dy)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDraggingHandle) {
                    isDraggingHandle = false
                    return true
                }
            }
        }
        return isDraggingHandle
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) - strokeWidthPx / 2f

        canvas.drawCircle(centerX, centerY, radius, boundaryFillPaint)
        canvas.drawCircle(centerX, centerY, radius, boundaryStrokePaint)

        canvas.save()
        canvas.translate(centerX + petOffsetX - petSizePx / 2f, centerY + petOffsetY - petSizePx / 2f)
        canvas.drawPath(petPath, petPaint)
        canvas.restore()

        val dotY = handleRect.centerY()
        val dotStartX = handleRect.centerX() - handleDotSpacingPx
        for (i in 0 until 3) {
            canvas.drawCircle(dotStartX + i * handleDotSpacingPx, dotY, handleDotRadiusPx, handleDotPaint)
        }
    }

    companion object {
        private const val STROKE_WIDTH_DP = 1.5f
        private const val HANDLE_TOP_MARGIN_DP = 4f
        private const val HANDLE_HEIGHT_DP = 12f
        private const val HANDLE_HALF_WIDTH_DP = 14f
        private const val HANDLE_DOT_RADIUS_DP = 1.8f
        private const val HANDLE_DOT_SPACING_DP = 6f
    }
}
