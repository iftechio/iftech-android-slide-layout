package io.iftech.android.library.slide

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.NestedScrollingChild2
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import io.iftech.android.library.dip
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

class SlideBarLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr),
    NestedScrollingChild2 {

    private val nestedHelper = NestedScrollingChildHelper(this)

    init {
        configSlideChildTypeBar()
        nestedHelper.isNestedScrollingEnabled = true
    }

    /* nested scroll start */

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return nestedHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        nestedHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return nestedHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return nestedHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return nestedHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return nestedHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return nestedHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    /* nested scroll start */

    private var isDragging = false
    private var lastY = 0
    private var activePointerId = -1
    private var velocityTracker: VelocityTracker? = null
    private var flingRunnable: Runnable? = null
    private var scroller: OverScroller? = null
    private val nestedOffsets = IntArray(2)
    private val scrollOffsets = IntArray(2)

    private val touchSlop: Int by lazy { ViewConfiguration.get(context).scaledTouchSlop }
    private var touchDownEv: MotionEvent? = null

    private var interceptFunc: (() -> Boolean)? = null

    fun setInterceptFunc(func: () -> Boolean) {
        this.interceptFunc = func
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (interceptFunc?.invoke() == true) {
            return true
        }
        val action = ev.action
        if (action == MotionEvent.ACTION_MOVE && isDragging) {
            return true
        } else {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    nestedOffsets.fill(0)
                    onTouchDown(ev)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onTouchCancel()
                MotionEvent.ACTION_MOVE -> {
                    if (activePointerId != -1) {
                        val activePointIndex = ev.findPointerIndex(activePointerId)
                        if (activePointIndex != -1) {
                            val y = ev.getY(activePointIndex).toInt()
                            if (abs(y - lastY) > touchSlop) {
                                onDragDetected()
                                lastY = y
                            }
                        }
                    }
                }
            }
            velocityTracker?.addMovement(ev)
            return isDragging
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val velocityEvent = MotionEvent.obtain(event)
        val clickCheckEv = MotionEvent.obtain(event)
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            nestedOffsets.fill(0)
        }
        velocityEvent.offsetLocation(nestedOffsets[0].toFloat(), nestedOffsets[1].toFloat())
        clickCheckEv.offsetLocation(nestedOffsets[0].toFloat(), nestedOffsets[1].toFloat())
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onTouchDown(event)
            MotionEvent.ACTION_UP -> {
                if (touchDownEv?.let { isClick(it, clickCheckEv) } == true) {
                    performClick()
                } else {
                    velocityTracker?.apply {
                        addMovement(velocityEvent)
                        computeCurrentVelocity(1000)
                        val yvel = getYVelocity(activePointerId)
                        val offset = context.dip(1000) // maybe screen height more sense
                        fling(-offset, offset, yvel)
                    }
                }
                onTouchCancel()
            }
            MotionEvent.ACTION_CANCEL -> {
                onTouchCancel()
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointIndex = event.findPointerIndex(activePointerId)
                if (activePointIndex == -1) {
                    return false
                }
                val y = event.getY(activePointIndex).toInt()
                var dy = lastY - y
                if (isDragging.not() && abs(dy) > touchSlop) {
                    onDragDetected()
                    if (dy > 0) {
                        dy -= touchSlop
                    } else {
                        dy += touchSlop
                    }
                }
                if (isDragging) {
                    scrollOffsets.fill(0)
                    dispatchNestedScroll(0, 0, 0, dy, scrollOffsets, ViewCompat.TYPE_TOUCH)
                    nestedOffsets[0] += scrollOffsets[0]
                    nestedOffsets[1] += scrollOffsets[1]
                    velocityEvent.offsetLocation(scrollOffsets[0].toFloat(), scrollOffsets[1].toFloat())
                    lastY = y - scrollOffsets[1]
                }
            }
        }
        velocityTracker?.addMovement(velocityEvent)
        velocityEvent.recycle()
        return true
    }

    private fun onTouchDown(event: MotionEvent) {
        onDragFinish()
        lastY = event.y.toInt()
        activePointerId = event.getPointerId(event.actionIndex)
        ensureVelocityTracker()
        touchDownEv = event
    }

    private fun isClick(down: MotionEvent, up: MotionEvent): Boolean {
        return abs(down.x - up.x).pow(2) + abs(down.y - up.y).pow(2) < touchSlop.toFloat().pow(2)
                && up.eventTime - down.eventTime < 200
    }

    private fun onDragFinish() {
        if (isDragging) {
            isDragging = false
            stopNestedScroll(ViewCompat.TYPE_TOUCH)
        }
    }

    private fun onTouchCancel() {
        onDragFinish()
        activePointerId = -1
        recycleVelocityTracker()
        touchDownEv = null
    }

    private fun ensureVelocityTracker() {
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.apply {
            recycle()
            velocityTracker = null
        }
    }

    private fun onDragDetected() {
        if (isDragging.not()) {
            isDragging = true
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
        }
    }

    private fun fling(minOffset: Int, maxOffset: Int, velocityY: Float): Boolean {
        flingRunnable?.apply {
            removeCallbacks(flingRunnable)
            flingRunnable = null
        }
        (scroller ?: OverScroller(context)).apply {
            fling(0, 0, 0, velocityY.roundToInt(), 0, 0, minOffset, maxOffset)
            return if (computeScrollOffset()) {
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
                if (dispatchNestedPreFling(0f, -velocityY)) {
                    onFlingFinished()
                } else {
                    flingRunnable = FlingRunnable(this@SlideBarLayout, this)
                    ViewCompat.postOnAnimation(this@SlideBarLayout, flingRunnable)
                }
                true
            } else {
                onFlingFinished()
                false
            }
        }
    }

    private fun scrollByFling(dy: Int) {
        dispatchNestedScroll(0, 0, 0, dy, null, ViewCompat.TYPE_NON_TOUCH)
    }

    private fun onFlingFinished() {
        stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    }

    class FlingRunnable(private val layout: SlideBarLayout, private val scroller: OverScroller) : Runnable {

        private var lastY = scroller.startY

        override fun run() {
            if (scroller.computeScrollOffset()) {
                val curY = scroller.currY
                val dy = lastY - curY
                lastY = curY
                layout.scrollByFling(dy)
                ViewCompat.postOnAnimation(layout, this)
            } else {
                layout.onFlingFinished()
            }
        }
    }
}