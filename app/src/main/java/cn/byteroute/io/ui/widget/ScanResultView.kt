package cn.byteroute.io.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.huawei.hms.ml.scan.HmsScan
import java.util.*

class ScanResultView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val lock = Any()
    protected var widthScaleFactor = 1.0f
    protected var heightScaleFactor = 1.0f
    protected var previewWidth = 0f
    protected var previewHeight = 0f
    private val hmsScanGraphics: MutableList<HmsScanGraphic> = ArrayList()
    fun clear() {
        synchronized(lock) { hmsScanGraphics.clear() }
        postInvalidate()
    }

    fun add(graphic: HmsScanGraphic) {
        synchronized(lock) { hmsScanGraphics.add(graphic) }
    }

    fun setCameraInfo(previewWidth: Int, previewHeight: Int) {
        synchronized(lock) {
            this.previewWidth = previewWidth.toFloat()
            this.previewHeight = previewHeight.toFloat()
        }
        postInvalidate()
    }

    /**
     * Draw MultiCodes on screen.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            if (previewWidth != 0f && previewHeight != 0f) {
                widthScaleFactor = canvas.width.toFloat() / previewWidth
                heightScaleFactor = canvas.height.toFloat() / previewHeight
            }
            for (graphic in hmsScanGraphics) {
                graphic.drawGraphic(canvas)
            }
        }
    }

    class HmsScanGraphic @JvmOverloads constructor(
        private val scanResultView: ScanResultView,
        private val hmsScan: HmsScan?,
        color: Int = Color.WHITE
    ) {
        private val rectPaint: Paint
        private val hmsScanResult: Paint
        fun drawGraphic(canvas: Canvas) {
            if (hmsScan == null) {
                return
            }
            val rect = RectF(hmsScan.borderRect)
            val other = RectF()
            other.left = canvas.width - scaleX(rect.top)
            other.top = scaleY(rect.left)
            other.right = canvas.width - scaleX(rect.bottom)
            other.bottom = scaleY(rect.right)
            canvas.drawRect(other, rectPaint)
            canvas.drawText(hmsScan.getOriginalValue(), other.right, other.bottom, hmsScanResult)
        }

        fun scaleX(horizontal: Float): Float {
            return horizontal * scanResultView.widthScaleFactor
        }

        fun scaleY(vertical: Float): Float {
            return vertical * scanResultView.heightScaleFactor
        }

        companion object {
            private const val TEXT_COLOR = Color.WHITE
            private const val TEXT_SIZE = 35.0f
            private const val STROKE_WIDTH = 4.0f
        }

        init {
            rectPaint = Paint()
            rectPaint.color = color
            rectPaint.style = Paint.Style.STROKE
            rectPaint.strokeWidth = STROKE_WIDTH
            hmsScanResult = Paint()
            hmsScanResult.color = TEXT_COLOR
            hmsScanResult.textSize = TEXT_SIZE
        }
    }
}