package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
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
    private var view: View? = null
    private var restartOrPause: AppCompatImageView? = null
    private var aivFullScreen: AppCompatImageView? = null
    private var atvPosition: AppCompatTextView? = null
    private var atvDuration: AppCompatTextView? = null

    private var seekBar: SeekBar? = null

    init {
        view = LayoutInflater.from(context).inflate(R.layout.controller_layout, null, false)
        addView(view)
        findViewById()
        setViewListener()
    }

    /**
     *  获取控件
     */
    fun findViewById() {
        restartOrPause = view?.findViewById(R.id.restart_or_pause)
        atvPosition = view?.findViewById(R.id.atv_position)
        seekBar = view?.findViewById(R.id.seek)
        atvDuration = view?.findViewById(R.id.atv_duration)
        aivFullScreen = view?.findViewById(R.id.aiv_full_screen)

    }

    /**
     * 设置监听
     */
    private fun setViewListener() {
        restartOrPause?.setOnClickListener(this)
        aivFullScreen?.setOnClickListener(this)
    }


}