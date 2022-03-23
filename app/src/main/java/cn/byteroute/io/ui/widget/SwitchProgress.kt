package cn.byteroute.io.ui.widget

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class SwitchProgress @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    val mPaint: Paint
    val outPaint: Paint
    val innerPaint: Paint
    val bitmapPaint: Paint
    val progressPaint: Paint
    var mBitmap: Bitmap? = null
    var padding = 0f
    var mWidth = 0
    var startColor = Color.argb(100, 0, 242, 123)
    var endColor = Color.argb(100, 86, 171, 228)
    var outRectF = RectF()
    var innerRectF = RectF()
    var rectFCenter = RectF()
    var mAngle = 0
    var canvas: Canvas? = null

    annotation class Status {
        companion object {
            var UNCONNETED = 1000
            var CONNECTTING = 2000
            var CONNETED = 3000
        }
    }

    var state = Status.UNCONNETED
    var valueAnimator: ValueAnimator? = null
    fun startViewAnim(time: Long): ValueAnimator? {
        valueAnimator = ValueAnimator.ofInt(0, 360)
        valueAnimator?.setDuration(time)
        valueAnimator?.setInterpolator(LinearInterpolator())
        valueAnimator?.setRepeatCount(-1)
        valueAnimator?.setRepeatMode(ValueAnimator.RESTART)
        valueAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
            mAngle = animation.animatedValue as Int
            invalidate()
        })
        if (!valueAnimator?.isRunning()!!) {
            valueAnimator?.start()
        }
        return valueAnimator
    }

    fun startAnim() {
        stopAnim()
        startViewAnim(2000)
    }

    fun stopAnim() {
        if (valueAnimator != null) {
            clearAnimation()
            valueAnimator!!.repeatCount = 0
            valueAnimator!!.cancel()
            valueAnimator!!.end()
        }
    }

    private fun createBitmap(): Bitmap? {
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(mWidth, mWidth, Bitmap.Config.ARGB_8888)
            canvas!!.setBitmap(mBitmap)
            canvas!!.drawLine(
                (mWidth / 2).toFloat(),
                mWidth / 3 - padding / 2,
                (mWidth / 2).toFloat(),
                mWidth / 2 - padding / 2,
                bitmapPaint
            ) //内开关
            canvas!!.drawArc(rectFCenter, -60f, 300f, false, bitmapPaint) //内开关
        }
        return mBitmap
    }

    private fun drawBg(canvas: Canvas) {
        canvas.drawBitmap(createBitmap()!!, 0f, 0f, mPaint)
    }

    private fun drawProgress(canvas: Canvas) {
        progressPaint.reset()
        progressPaint.strokeWidth = padding
        progressPaint.style = Paint.Style.STROKE
        val mShader: Shader = LinearGradient(
            outRectF.left, outRectF.top,
            outRectF.left, outRectF.bottom, intArrayOf(
                startColor,
                endColor
            ), floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        progressPaint.shader = mShader
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeJoin = Paint.Join.ROUND
        canvas.drawArc(outRectF, -90f, mAngle.toFloat(), false, progressPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        innerPaint.color =
            if (state == Status.UNCONNETED) Color.parseColor("#8098FB98") else Color.parseColor("#FFCC9933")
        canvas.drawOval(outRectF, outPaint) //最外层的圆
        canvas.drawOval(innerRectF, innerPaint) //内圆
        drawBg(canvas)
        drawProgress(canvas)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = if (width < height) width else height
        val measureSpec = if (width < height) widthMeasureSpec else heightMeasureSpec
        setMeasuredDimension(measureSpec, measureSpec)
        padding = (mWidth shr 4).toFloat() //dip2px(12);
        outRectF = RectF(padding, padding, mWidth - padding, mWidth - padding)
        innerRectF = RectF(2 * padding, 2 * padding, mWidth - 2 * padding, mWidth - 2 * padding)
        rectFCenter = RectF(
            (mWidth / 3).toFloat(), (mWidth / 3).toFloat(),
            (mWidth * 2 / 3).toFloat(), (mWidth * 2 / 3).toFloat()
        )
        outPaint.strokeWidth = padding //外圆宽度 1/16 宽度
        innerPaint.strokeWidth = padding //外圆宽度 1/16 宽度
        bitmapPaint.strokeWidth = padding / 2 //开关宽度 1/32 宽度
    }

    fun getStatus(): Int {
        return state
    }

    fun setStatus(@Status status: Int) {
        this.state = status
        if (this.state == Status.UNCONNETED) {
            stopAnim()
            invalidate()
        } else if (this.state == Status.CONNECTTING) {
            startAnim()
        } else if (this.state == Status.CONNETED) {
            stopAnim()
            invalidate()
        }
    }

    init {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        outPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        outPaint.color = Color.parseColor("#FFDEDEDE")
        outPaint.style = Paint.Style.STROKE
        innerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        innerPaint.style = Paint.Style.FILL_AND_STROKE
        bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bitmapPaint.color = Color.WHITE
        bitmapPaint.style = Paint.Style.STROKE
        bitmapPaint.strokeCap = Paint.Cap.ROUND
        bitmapPaint.strokeJoin = Paint.Join.ROUND
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas = Canvas()
    }
}