package io.iftech.android.library.slide

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import io.iftech.android.library.cancel
import io.iftech.android.library.refresh.RefreshViewLayout
import io.iftech.android.library.doOnUpdate
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RefreshPresenter(private val refreshView: () -> RefreshViewLayout?, private val updateCallback: () -> Unit) {

    var height = 0
        private set

    private var onRefreshListener: ((Boolean) -> Unit)? = null
    private var refreshing = false
    private var refreshByPull = true

    private var hideRefreshAnimator: Animator? = null

    private var containerHeight = 0
    private var offset = 0

    fun isVisible() = height > 0

    fun onLayout() {
        changeHeight(height)
    }

    fun onDetach() {
        cancelHideRefreshAnimation()
    }

    private fun changeHeight(h: Int) {
        this.height = h
        refreshView()?.also { refresh ->
            refresh.updateVisibleHeight(h)
            val top = offset + h - refresh.height
            refresh.offsetTopAndBottom(top - refresh.top)
        }
        updateCallback()
    }

    fun setOffset(offset: Int) {
        this.offset = offset
        changeHeight(height)
    }

    fun onContainerHeightReady(height: Int) {
        containerHeight = height
        refreshView()?.onContainerHeightReady(height)
    }

    fun onStartScroll() {
        cancelHideRefreshAnimation()
    }

    fun onScroll(dy: Int, type: Int): Int {
        var consumedY = 0
        refreshView()?.also { refresh ->
            val totalHeight = refresh.height
            if (dy < 0) {
                // pull down
                if (type == ViewCompat.TYPE_TOUCH) {
                    if (height < totalHeight) {
                        consumedY = max(dy, -(totalHeight - height))
                        changeHeight((height - consumedY / refresh.getDampFactor(containerHeight)).toInt())
                    }
                }
            } else if (dy > 0) {
                // pull up
                consumedY = min(dy, height)
                changeHeight(height - consumedY)
            }
        }
        return consumedY
    }

    fun canStopNonTouch(dy: Int): Boolean {
        if (dy < 0) {
            // pull down
            return true
        }
        return false
    }

    fun onStopScroll() {
        tryStartRefresh()
        hideRefreshIfNeed()
    }

    private fun tryStartRefresh() {
        refreshView()?.apply {
            if (canRefresh()) {
                startRefresh()
            }
        }
    }

    private fun startRefresh() {
        if (refreshing) {
            return
        }
        refreshing = true
        onRefreshListener?.invoke(refreshByPull)
        refreshByPull = true
    }

    private fun hideRefreshIfNeed(force: Boolean = false) {
        if (isVisible()) {
            refreshView()?.also { refresh ->
                var listener: Animator.AnimatorListener? = null
                val targetHeight: Int
                if (height < refresh.heightCanRefresh || refresh.isRestore() || force) {
                    listener = object : Animator.AnimatorListener {

                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator) {
                            refresh.restore()
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            refresh.reset()
                        }
                    }
                    targetHeight = 0
                } else {
                    targetHeight = refresh.heightCanRefresh
                }
                animateToHeight(
                    targetHeight,
                    (HIDE_REFRESH_DURATION * (abs(targetHeight - height)) / refresh.height + HIDE_REFRESH_DURATION).toLong(),
                    listener
                )
            }
        } else {
            refreshView()?.reset()
        }
    }

    private fun animateToHeight(targetHeight: Int, duration: Long, listener: Animator.AnimatorListener? = null) {
        if (refreshView() != null && targetHeight != height) {
            cancelHideRefreshAnimation()
            hideRefreshAnimator = ValueAnimator.ofInt(height, targetHeight).apply {
                doOnUpdate { _: Animator, h: Int ->
                    changeHeight(h)
                }
                interpolator = DECELERATE
                this.duration = duration
                listener?.also { addListener(it) }
                start()
            }
        }
    }

    private fun cancelHideRefreshAnimation() {
        hideRefreshAnimator?.apply {
            cancel(true)
            hideRefreshAnimator = null
        }
    }

    fun setOnRefreshListener(listener: (Boolean) -> Unit) {
        onRefreshListener = listener
    }

    fun refresh() {
        refreshView()?.heightCanRefresh?.takeIf { it > 0 }?.also {
            animateToHeight(it, ANIM_DURATION, object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    tryStartRefresh()
                }
            })
            refreshByPull = false
        }
    }

    fun finishRefresh() {
        hideRefreshIfNeed(true)
        refreshing = false
    }

    companion object {
        private val DECELERATE = DecelerateInterpolator()
        private const val HIDE_REFRESH_DURATION = 150
        private const val ANIM_DURATION = 200L
    }
}