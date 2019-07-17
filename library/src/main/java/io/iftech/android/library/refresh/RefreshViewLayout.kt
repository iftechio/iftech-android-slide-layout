package io.iftech.android.library.refresh

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.max

class RefreshViewLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var refreshInterface: RefreshView? = null
        set(value) {
            field = value?.also {
                removeAllViews()
                addView(it.view)
                it.updateVisibleHeight(visibleHeight)
                requestLayout()
            }
        }

    private var visibleHeight = 0

    var heightCanRefresh = 0

    /**
     * perhaps screen height
     */
    fun getDampFactor(range: Int): Float {
        if (visibleHeight > heightCanRefresh) {
            return visibleHeight * 6 / range + BASE_DAMP_FACTOR
        }
        return BASE_DAMP_FACTOR
    }

    fun updateVisibleHeight(height: Int) {
        if (visibleHeight != height) {
            this.visibleHeight = height
            refreshInterface?.apply {
                updateVisibleHeight(height)
                requestLayout()
                if (canDrag()) {
                    val fraction = (visibleHeight.toFloat() / heightCanRefresh.toFloat()).coerceIn(0f, 1f)
                    updateDragging(fraction)
                } else if (isLoading()) {
                    alpha = if (visibleHeight < heightCanRefresh) {
                        max(visibleHeight - heightCanRefresh / 4, 0).toFloat() / heightCanRefresh
                    } else {
                        1f
                    }
                }
            }
        }
    }

    fun canRefresh(): Boolean {
        return refreshInterface?.run {
            canRefresh().also {
                if (it) {
                    startLoading()
                }
            }
        } == true
    }

    fun onContainerHeightReady(height: Int) {
        heightCanRefresh = (REFRESH_HEIGHT_RATIO_OF_CONTAINER * height).toInt()
        layoutParams.height = height / 3
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        refreshInterface?.view?.measure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(visibleHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        refreshInterface?.view?.layout(0, measuredHeight - visibleHeight, measuredWidth, measuredHeight)
    }

    fun restore() {
        refreshInterface?.restore()
    }

    fun reset() {
        refreshInterface?.reset()
        alpha = 1f
    }

    fun isRestore(): Boolean {
        return refreshInterface?.isRestore() == true
    }

    companion object {
        private const val BASE_DAMP_FACTOR = 1.25f
        private const val REFRESH_HEIGHT_RATIO_OF_CONTAINER = 1 / 8f
    }
}