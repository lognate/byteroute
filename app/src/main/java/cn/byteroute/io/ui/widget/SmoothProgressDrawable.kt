package cn.byteroute.io.ui.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import cn.byteroute.io.R

class SmoothProgressDrawable private constructor(
    private var mInterpolator: Interpolator?,
    private var mSectionsCount: Int,
    private var mSeparatorLength: Int,
    private var mColors: IntArray,
    strokeWidth: Float,
    private var mSpeed: Float,
    private var mReversed: Boolean,
    private var mMirrorMode: Boolean
) :
    Drawable(), Animatable {
    private var mBounds: Rect? = null
    private val mPaint: Paint
    private var mColorsIndex = 0
    private var mRunning = false
    private var mCurrentOffset = 0f
    private var mNewTurn = false
    private var mMaxOffset: Float=0f

    fun setInterpolator(interpolator: Interpolator?) {
        requireNotNull(interpolator) { "Interpolator cannot be null" }
        mInterpolator = interpolator
        invalidateSelf()
    }

    fun setColors(colors: IntArray?) {
        require(!(colors == null || colors.size == 0)) { "Colors cannot be null or empty" }
        mColorsIndex = 0
        mColors = colors
        invalidateSelf()
    }

    fun setColor(color: Int) {
        setColors(intArrayOf(color))
    }

    fun setSpeed(speed: Float) {
        require(speed >= 0) { "Speed must be >= 0" }
        mSpeed = speed
        invalidateSelf()
    }

    fun setSectionsCount(sectionsCount: Int) {
        require(sectionsCount > 0) { "SectionsCount must be > 0" }
        mSectionsCount = sectionsCount
        mMaxOffset = 1f / mSectionsCount
        mCurrentOffset %= mMaxOffset
        invalidateSelf()
    }

    fun setSeparatorLength(separatorLength: Int) {
        require(separatorLength >= 0) { "SeparatorLength must be >= 0" }
        mSeparatorLength = separatorLength
        invalidateSelf()
    }

    fun setStrokeWidth(strokeWidth: Float) {
        require(strokeWidth >= 0) { "The strokeWidth must be >= 0" }
        mPaint.strokeWidth = strokeWidth
        invalidateSelf()
    }

    fun setReversed(reversed: Boolean) {
        if (mReversed == reversed) return
        mReversed = reversed
        invalidateSelf()
    }

    fun setMirrorMode(mirrorMode: Boolean) {
        if (mMirrorMode == mirrorMode) return
        mMirrorMode = mirrorMode
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        mBounds = bounds
        canvas.clipRect(mBounds!!)
        val boundsWidth = mBounds!!.width()
        if (mReversed) {
            canvas.translate(boundsWidth.toFloat(), 0f)
            canvas.scale(-1f, 1f)
        }
        drawStrokes(canvas)
    }

    private fun drawStrokes(canvas: Canvas) {
        var prevValue = 0f
        var boundsWidth = mBounds!!.width()
        if (mMirrorMode) boundsWidth /= 2
        val width = boundsWidth + mSeparatorLength + mSectionsCount
        val centerY = mBounds!!.centerY()
        val xSectionWidth = 1f / mSectionsCount

        //new turn
        if (mNewTurn) {
            mColorsIndex = decrementColor(mColorsIndex)
            mNewTurn = false
        }
        var prev: Float
        var end: Float
        var spaceLength: Float
        var xOffset: Float
        var ratioSectionWidth: Float
        var sectionWidth: Float
        var drawLength: Float
        var currentIndexColor = mColorsIndex
        for (i in 0..mSectionsCount) {
            xOffset = xSectionWidth * i + mCurrentOffset
            prev = Math.max(0f, xOffset - xSectionWidth)
            ratioSectionWidth = Math.abs(
                mInterpolator!!.getInterpolation(prev) -
                        mInterpolator!!.getInterpolation(Math.min(xOffset, 1f))
            )
            sectionWidth = (width * ratioSectionWidth).toFloat()
            spaceLength =
                if (sectionWidth + prev < width) Math.min(
                    sectionWidth,
                    mSeparatorLength.toFloat()
                ) else 0f
            drawLength = if (sectionWidth > spaceLength) sectionWidth - spaceLength else 0f
            end = prevValue + drawLength
            if (end > prevValue) {
                drawLine(
                    canvas,
                    boundsWidth,
                    Math.min(boundsWidth.toFloat(), prevValue),
                    centerY.toFloat(),
                    Math.min(boundsWidth.toFloat(), end),
                    centerY.toFloat(),
                    currentIndexColor
                )
            }
            prevValue = end + spaceLength
            currentIndexColor = incrementColor(currentIndexColor)
        }
    }

    private fun drawLine(
        canvas: Canvas,
        canvasWidth: Int,
        startX: Float,
        startY: Float,
        stopX: Float,
        stopY: Float,
        currentIndexColor: Int
    ) {
        mPaint.color = mColors[currentIndexColor]
        if (!mMirrorMode) {
            canvas.drawLine(startX, startY, stopX, stopY, mPaint)
        } else {
            if (mReversed) {
                canvas.drawLine(canvasWidth + startX, startY, canvasWidth + stopX, stopY, mPaint)
                canvas.drawLine(canvasWidth - startX, startY, canvasWidth - stopX, stopY, mPaint)
            } else {
                canvas.drawLine(startX, startY, stopX, stopY, mPaint)
                canvas.drawLine(
                    canvasWidth * 2 - startX,
                    startY,
                    canvasWidth * 2 - stopX,
                    stopY,
                    mPaint
                )
            }
        }
        canvas.save()
    }

    private fun incrementColor(colorIndex: Int): Int {
        var colorIndex = colorIndex
        ++colorIndex
        if (colorIndex >= mColors.size) colorIndex = 0
        return colorIndex
    }

    private fun decrementColor(colorIndex: Int): Int {
        var colorIndex = colorIndex
        --colorIndex
        if (colorIndex < 0) colorIndex = mColors.size - 1
        return colorIndex
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

       override fun start() {
        if (isRunning) return
        scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION)
        invalidateSelf()
    }

    override fun stop() {
        if (!isRunning) return
        mRunning = false
        unscheduleSelf(mUpdater)
    }

    override fun scheduleSelf(what: Runnable, `when`: Long) {
        mRunning = true
        super.scheduleSelf(what, `when`)
    }

    override fun isRunning(): Boolean {
        return mRunning
    }

    private val mUpdater: Runnable = object : Runnable {
        override fun run() {
            mCurrentOffset += OFFSET_PER_FRAME * mSpeed
            if (mCurrentOffset >= this@SmoothProgressDrawable.mMaxOffset) {
                mNewTurn = true
                mCurrentOffset -= this@SmoothProgressDrawable.mMaxOffset
            }
            scheduleSelf(this, SystemClock.uptimeMillis() + FRAME_DURATION)
            invalidateSelf()
        }
    }
    class Builder(context: Context) {
        private var mInterpolator: Interpolator? = null
        private var mSectionsCount = 0
        private lateinit var mColors: IntArray
        private var mSpeed = 0f
        private var mReversed = false
        private var mMirrorMode = false
        private var mStrokeSeparatorLength = 0
        private var mStrokeWidth = 0f
        fun build(): SmoothProgressDrawable {
            return SmoothProgressDrawable(
                mInterpolator,
                mSectionsCount,
                mStrokeSeparatorLength,
                mColors,
                mStrokeWidth,
                mSpeed,
                mReversed,
                mMirrorMode
            )
        }

        private fun initValues(context: Context) {
            val res = context.resources
            mInterpolator = AccelerateInterpolator()
            mSectionsCount = 4
            mColors = intArrayOf(Color.parseColor("#FF4092F5"))
            mSpeed = 1f
            mReversed = false
            mStrokeSeparatorLength =
                res.getDimensionPixelSize(R.dimen.spb_default_stroke_separator_length)
            mStrokeWidth = res.getDimensionPixelOffset(R.dimen.spb_default_stroke_width).toFloat()
        }

        fun interpolator(interpolator: Interpolator?): Builder {
            requireNotNull(interpolator) { "Interpolator can't be null" }
            mInterpolator = interpolator
            return this
        }

        fun sectionsCount(sectionsCount: Int): Builder {
            require(sectionsCount > 0) { "SectionsCount must be > 0" }
            mSectionsCount = sectionsCount
            return this
        }

        fun separatorLength(separatorLength: Int): Builder {
            require(separatorLength >= 0) { "SeparatorLength must be >= 0" }
            mStrokeSeparatorLength = separatorLength
            return this
        }

        fun color(color: Int): Builder {
            mColors = intArrayOf(color)
            return this
        }

        fun colors(colors: IntArray?): Builder {
            require(!(colors == null || colors.size == 0)) { "Your color array must not be empty" }
            mColors = colors
            return this
        }

        fun strokeWidth(width: Float): Builder {
            require(width >= 0) { "The width must be >= 0" }
            mStrokeWidth = width
            return this
        }

        fun speed(speed: Float): Builder {
            require(speed >= 0) { "Speed must be >= 0" }
            mSpeed = speed
            return this
        }

        fun reversed(reversed: Boolean): Builder {
            mReversed = reversed
            return this
        }

        fun mirrorMode(mirrorMode: Boolean): Builder {
            mMirrorMode = mirrorMode
            return this
        }

        init {
            initValues(context)
        }
    }

    companion object {
        private const val FRAME_DURATION = (1000 / 60).toLong()
        private const val OFFSET_PER_FRAME = 0.01f
    }

    init {
        mMaxOffset = 1f / mSectionsCount
        mPaint = Paint()
        mPaint.strokeWidth = strokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.isDither = false
        mPaint.isAntiAlias = false
    }
}