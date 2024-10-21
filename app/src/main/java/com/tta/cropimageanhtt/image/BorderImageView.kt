package com.tohsoft.periodtracker.ui.custom.image

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.tta.cropimageanhtt.R

class BorderImageView : AppCompatImageView {
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BorderImageView)
        mBorderRadius = a.getDimensionPixelSize(R.styleable.BorderImageView_civ_border_radius, DEFAULT_BORDER_RADIUS).toFloat()
        a.recycle()
        init()
    }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BorderImageView, defStyle, 0)
        mBorderRadius = a.getDimensionPixelSize(R.styleable.BorderImageView_civ_border_radius, DEFAULT_BORDER_RADIUS).toFloat()
        a.recycle()
        init()
    }

    private val roundRect = RectF()
    private var mBorderRadius = 0f
    private val maskPaint = Paint()
    private val zonePaint = Paint()
    private fun init() {
        maskPaint.isAntiAlias = true
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        zonePaint.isAntiAlias = true
        zonePaint.color = Color.WHITE
//        val density = resources.displayMetrics.density
        mBorderRadius = mBorderRadius / 1
    }

    fun setRectAdius(radius: Float) {
        mBorderRadius = radius
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val w = width
        val h = height
        roundRect[0f, 0f, w.toFloat()] = h.toFloat()
    }

    override fun draw(canvas: Canvas) {
        canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG)
        canvas.drawRoundRect(roundRect, mBorderRadius, mBorderRadius, zonePaint)
        canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG)
        super.draw(canvas)
        canvas.restore()
    }

    companion object {
        private const val DEFAULT_BORDER_RADIUS = 10
    }
}