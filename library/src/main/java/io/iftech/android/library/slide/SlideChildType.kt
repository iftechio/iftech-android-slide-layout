package io.iftech.android.library.slide

import android.view.View

private const val HEADER = "HEADER"
private const val BAR = "BAR"
private const val SLIDER = "SLIDER"

private fun View.setTypeTag(type: String) {
    if (tag == null) {
        tag = type
    }
}

fun View.configSlideChildTypeHeader() = setTypeTag(HEADER)
fun View.configSlideChildTypeBar() = setTypeTag(BAR)
fun View.configSlideChildTypeSlider() = setTypeTag(SLIDER)

private fun View.checkType(type: String): Boolean = tag == type

fun View.isSlideChildTypeHeader() = checkType(HEADER)
fun View.isSlideChildTypeBar() = checkType(BAR)
fun View.isSlideChildTypeSlider() = checkType(SLIDER)

fun View.findChildTypeHeader(): View = findViewWithTag<View>(HEADER)