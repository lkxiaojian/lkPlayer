package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.SeekBar

/**
 * @Description:     控制点击视频，出现的悬浮层
 * @Author:         lk
 * @CreateDate:     2021/5/27 14:11
 */
class SuspensionLayerView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    SeekBar.OnSeekBarChangeListener {
    private val mContext = context
    private val SEEK_MAX = 100
    lateinit var seekBar: SeekBar

    init {
        addCurtomView()
    }

    private fun addCurtomView() {
        seekBar = SeekBar(context)
        seekBar.max = SEEK_MAX
        seekBar.setOnSeekBarChangeListener(this)
        addView(seekBar)
    }


    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    /**
     *  设置当前进度条
     */
    fun setSeekToProgress(progress: Int) {
        seekBar.progress = progress * SEEK_MAX
    }
}