package io.iftech.android.library

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StyleRes
import kotlin.math.roundToInt

fun Animator.cancel(removeListener: Boolean) {
    if (removeListener) {
        removeAllListeners()
        (this as? ValueAnimator)?.removeAllUpdateListeners()
    }
    if (isRunning) {
        cancel()
    }
}

fun View.useAttrs(attrs: AttributeSet?, styleableRes: IntArray, block: TypedArray.() -> Unit) {
    attrs?.also {
        val ta = context.obtainStyledAttributes(it, styleableRes)
        ta.block()
        ta.recycle()
    }
}

inline fun <reified T> ValueAnimator.doOnUpdate(crossinline action: (Animator, T) -> Unit): ValueAnimator.AnimatorUpdateListener {
    val listener = ValueAnimator.AnimatorUpdateListener {
        action(it, it.animatedValue as T)
    }
    addUpdateListener(listener)
    return listener
}

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
fun View.dip(value: Int): Int = context.dip(value)
