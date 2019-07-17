package io.iftech.android.library.refresh

import android.view.View

interface RefreshView {
    val view: View
    fun canDrag(): Boolean
    fun canRefresh(): Boolean
    fun isLoading(): Boolean
    fun isRestore(): Boolean

    fun updateDragging(fraction: Float)
    fun startLoading()
    fun restore()
    fun reset()
    fun updateVisibleHeight(height: Int)
}