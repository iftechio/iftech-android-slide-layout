package io.iftech.android.library.slide

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.iftech.android.library.R
import io.iftech.android.library.useAttrs

class MinVerticalMarginFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private var minVerticalMargin = 0

    init {
        useAttrs(attrs, R.styleable.WidgetMinVerticalMarginFrameLayout) {
            minVerticalMargin = getDimensionPixelSize(R.styleable.WidgetMinVerticalMarginFrameLayout_widget_min_vertical_margin, 0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (minVerticalMargin > 0) {
            super.onMeasure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - minVerticalMargin,
                            MeasureSpec.getMode(heightMeasureSpec)))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setMinVerticalMargin(margin: Int) {
        minVerticalMargin = margin
        requestLayout()
    }
}
