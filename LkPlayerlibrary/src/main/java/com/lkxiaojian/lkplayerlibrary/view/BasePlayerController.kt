package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.lkxiaojian.lkplayerlibrary.R

/**
 * @Description:     java类作用描述
 * @Author:         lk
 * @CreateDate:     2021/5/27 14:30
 */
abstract class BasePlayerController(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs), View.OnTouchListener, View.OnClickListener {
     var baseView: View? = null
    private var restartOrPause: AppCompatImageView? = null
    private var aivFullScreen: AppCompatImageView? = null
    private var atvPosition: AppCompatTextView? = null
    private var atvDuration: AppCompatTextView? = null

    private var seekBar: SeekBar? = null

    init {
        baseView = LayoutInflater.from(context).inflate(R.layout.controller_layout, null, false)
        findViewById()
        setViewListener()
    }

    /**
     *  获取控件
     */
    fun findViewById() {
        restartOrPause = baseView?.findViewById(R.id.restart_or_pause)
        atvPosition = baseView?.findViewById(R.id.atv_position)
        seekBar = baseView?.findViewById(R.id.seek)
        atvDuration = baseView?.findViewById(R.id.atv_duration)
        aivFullScreen = baseView?.findViewById(R.id.aiv_full_screen)

    }

    /**
     * 设置监听
     */
    private fun setViewListener() {
        restartOrPause?.setOnClickListener(this)
        aivFullScreen?.setOnClickListener(this)
    }


}