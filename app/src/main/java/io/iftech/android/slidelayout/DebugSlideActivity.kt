package io.iftech.android.slidelayout

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.iftech.android.library.dip
import io.iftech.android.library.slide.configSlideChildTypeHeader
import io.iftech.android.library.slide.configSlideChildTypeSlider
import kotlinx.android.synthetic.main.activity_debug_slide.*
import kotlinx.android.synthetic.main.layout_debug_slide_header.*
import kotlinx.android.synthetic.main.list_item_debug_slide_slider.view.*

class DebugSlideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_slide)
        setupHeader()
        fillSlider()
        layHeader.minimumHeight = dip(55)
        laySlider.setMinVerticalMargin(layHeader.minimumHeight)
        laySlide.setRefreshOffset(layHeader.minimumHeight)
        laySlide.expandHeader()
        layRefresh.refreshInterface = MyRefreshViewImpl(this)
        laySlide.apply {
            setOnRefreshListener { _, _ ->
                postDelayed({
                    finishRefresh()
                }, 1_000)
            }
        }
    }

    private fun setupHeader() {
        nestedScrollView.configSlideChildTypeHeader()
        val debugScroll = intent.getStringExtra(DEBUG_TYPE) == DEBUG_TYPE_SCROLL
        ivHeaderPic2.isVisible = debugScroll.not()
        tvHeaderContent2.isVisible = debugScroll.not()
    }

    private fun fillSlider() {
        val adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_debug_slide_slider, parent, false)
                return VH(v)
            }

            override fun getItemCount() = 20

            override fun onBindViewHolder(holder: VH, position: Int) {
                holder.bindView(position)
            }
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DebugSlideActivity)
            this.adapter = adapter
            configSlideChildTypeSlider()
            setBackgroundColor(Color.WHITE)
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bindView(pos: Int) {
            (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin =
                    if (pos == 0) 0 else itemView.context.dip(10)
            itemView.tvTitle.text = "This is ${pos + 1} title."
        }
    }

    companion object {
        const val DEBUG_TYPE = "debug_type"
        const val DEBUG_TYPE_SCROLL = "scroll"
        const val DEBUG_TYPE_SLIDE = "slide"
    }
}