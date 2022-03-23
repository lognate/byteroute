package cn.byteroute.io.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.animation.*
import android.widget.ProgressBar
import cn.byteroute.io.R

class SmoothProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.spbStyle
) :
    ProgressBar(context, attrs, defStyle) {
    fun applyStyle(styleResId: Int) {
        val a = context.obtainStyledAttributes(null, R.styleable.SmoothProgressBar, 0, styleResId)
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_color)) {
            setSmoothProgressDrawableColor(a.getColor(R.styleable.SmoothProgressBar_spb_color, 0))
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_colors)) {
            val colorsId = a.getResourceId(R.styleable.SmoothProgressBar_spb_colors, 0)
            if (colorsId != 0) {
                val colors = resources.getIntArray(colorsId)
                if (colors != null && colors.size > 0) setSmoothProgressDrawableColors(colors)
            }
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_sections_count)) {
            setSmoothProgressDrawableSectionsCount(
                a.getInteger(
                    R.styleable.SmoothProgressBar_spb_sections_count,
                    0
                )
            )
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_stroke_separator_length)) {
            setSmoothProgressDrawableSeparatorLength(
                a.getDimensionPixelSize(
                    R.styleable.SmoothProgressBar_spb_stroke_separator_length,
                    0
                )
            )
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_stroke_width)) {
            setSmoothProgressDrawableStrokeWidth(
                a.getDimension(
                    R.styleable.SmoothProgressBar_spb_stroke_width,
                    0f
                )
            )
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_speed)) {
            setSmoothProgressDrawableSpeed(a.getFloat(R.styleable.SmoothProgressBar_spb_speed, 0f))
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_reversed)) {
            setSmoothProgressDrawableReversed(
                a.getBoolean(
                    R.styleable.SmoothProgressBar_spb_reversed,
                    false
                )
            )
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_mirror_mode)) {
            setSmoothProgressDrawableMirrorMode(
                a.getBoolean(
                    R.styleable.SmoothProgressBar_spb_mirror_mode,
                    false
                )
            )
        }
        if (a.hasValue(R.styleable.SmoothProgressBar_spb_interpolator)) {
            val iInterpolator = a.getInteger(R.styleable.SmoothProgressBar_spb_interpolator, -1)
            val interpolator: Interpolator?
            interpolator =
                when (iInterpolator) {
                    INTERPOLATOR_ACCELERATEDECELERATE -> AccelerateDecelerateInterpolator()
                    INTERPOLATOR_DECELERATE -> DecelerateInterpolator()
                    INTERPOLATOR_LINEAR -> LinearInterpolator()
                    INTERPOLATOR_ACCELERATE -> AccelerateInterpolator()
                    else -> null
                }
            interpolator?.let { setInterpolator(it) }
        }
        a.recycle()
    }

    private fun checkIndeterminateDrawable(): SmoothProgressDrawable {
        val ret = indeterminateDrawable
        if (ret == null || ret !is SmoothProgressDrawable) throw RuntimeException("The drawable is not a SmoothProgressDrawable")
        return ret
    }

    override fun setInterpolator(interpolator: Interpolator) {
        super.setInterpolator(interpolator)
        val ret = indeterminateDrawable
        if (ret != null && ret is SmoothProgressDrawable) ret.setInterpolator(interpolator)
    }

    fun setSmoothProgressDrawableInterpolator(interpolator: Interpolator?) {
        checkIndeterminateDrawable().setInterpolator(interpolator)
    }

    fun setSmoothProgressDrawableColors(colors: IntArray?) {
        checkIndeterminateDrawable().setColors(colors)
    }

    fun setSmoothProgressDrawableColor(color: Int) {
        checkIndeterminateDrawable().setColor(color)
    }

    fun setSmoothProgressDrawableSpeed(speed: Float) {
        checkIndeterminateDrawable().setSpeed(speed)
    }

    fun setSmoothProgressDrawableSectionsCount(sectionsCount: Int) {
        checkIndeterminateDrawable().setSectionsCount(sectionsCount)
    }

    fun setSmoothProgressDrawableSeparatorLength(separatorLength: Int) {
        checkIndeterminateDrawable().setSeparatorLength(separatorLength)
    }

    fun setSmoothProgressDrawableStrokeWidth(strokeWidth: Float) {
        checkIndeterminateDrawable().setStrokeWidth(strokeWidth)
    }

    fun setSmoothProgressDrawableReversed(reversed: Boolean) {
        checkIndeterminateDrawable().setReversed(reversed)
    }

    fun setSmoothProgressDrawableMirrorMode(mirrorMode: Boolean) {
        checkIndeterminateDrawable().setMirrorMode(mirrorMode)
    }

    companion object {
        private const val INTERPOLATOR_ACCELERATE = 0
        private const val INTERPOLATOR_LINEAR = 1
        private const val INTERPOLATOR_ACCELERATEDECELERATE = 2
        private const val INTERPOLATOR_DECELERATE = 3
    }

    init {
        val res = context.resources
        val a = context.obtainStyledAttributes(attrs, R.styleable.SmoothProgressBar, defStyle, 0)
        val color =
            a.getColor(R.styleable.SmoothProgressBar_spb_color, Color.parseColor("#FF4092F5"))
        val sectionsCount = a.getInteger(R.styleable.SmoothProgressBar_spb_sections_count, 4)
        val separatorLength = a.getDimensionPixelSize(
            R.styleable.SmoothProgressBar_spb_stroke_separator_length,
            res.getDimensionPixelSize(R.dimen.spb_default_stroke_separator_length)
        )
        val strokeWidth = a.getDimension(
            R.styleable.SmoothProgressBar_spb_stroke_width,
            res.getDimension(R.dimen.spb_default_stroke_width)
        )
        val speed = a.getFloat(R.styleable.SmoothProgressBar_spb_speed, 1f)
        val iInterpolator = a.getInteger(R.styleable.SmoothProgressBar_spb_interpolator, 0)
        val reversed = a.getBoolean(R.styleable.SmoothProgressBar_spb_reversed, false)
        val mirrorMode = a.getBoolean(R.styleable.SmoothProgressBar_spb_mirror_mode, false)
        val colorsId = a.getResourceId(R.styleable.SmoothProgressBar_spb_colors, 0)
        a.recycle()

        //interpolator
        val interpolator: Interpolator
        interpolator =
            when (iInterpolator) {
                INTERPOLATOR_ACCELERATEDECELERATE -> AccelerateDecelerateInterpolator()
                INTERPOLATOR_DECELERATE -> DecelerateInterpolator()
                INTERPOLATOR_LINEAR -> LinearInterpolator()
                INTERPOLATOR_ACCELERATE -> AccelerateInterpolator()
                else -> AccelerateInterpolator()
            }
        var colors: IntArray? = null
        //colors
        if (colorsId != 0) {
            colors = res.getIntArray(colorsId)
        }
        val builder = SmoothProgressDrawable.Builder(context)
            .speed(speed)
            .interpolator(interpolator)
            .sectionsCount(sectionsCount)
            .separatorLength(separatorLength)
            .strokeWidth(strokeWidth)
            .reversed(reversed)
            .mirrorMode(mirrorMode)
        if (colors != null && colors.size > 0) builder.colors(colors) else builder.color(color)
        indeterminateDrawable = builder.build()
    }
}