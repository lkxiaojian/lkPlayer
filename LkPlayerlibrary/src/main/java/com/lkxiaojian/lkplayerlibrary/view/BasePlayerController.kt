package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.lkxiaojian.lkplayerlibrary.R
import com.lkxiaojian.lkplayerlibrary.utlis.PlayerUtils

/**
 * @Description:   触摸后的操作
 * @Author:         lk
 * @CreateDate:     2021/5/27 14:30
 */
abstract class BasePlayerController(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs), View.OnTouchListener, View.OnClickListener {
    var baseView: View? = null
    var resumeOrPause: AppCompatImageView? = null
    var clBaseControl:ConstraintLayout?=null
    private var aivFullScreen: AppCompatImageView? = null
    private var atvPosition: AppCompatTextView? = null
    private var atvDuration: AppCompatTextView? = null
    var isTouch = false//是否在触摸进度条
    var isSeek = false//是否在移动进度条
    var seekBar: SeekBar? = null
    var lauViewModel: LauViewModel
    var duration = 0
    val TAG = "player"


    init {
        baseView = LayoutInflater.from(context).inflate(R.layout.controller_layout, null, false)
        lauViewModel = LauViewModel()
        findViewById()
        setViewListener()
    }

    /**
     *  获取控件
     */
    fun findViewById() {
        resumeOrPause = baseView?.findViewById(R.id.restart_or_pause)
        atvPosition = baseView?.findViewById(R.id.atv_position)
        seekBar = baseView?.findViewById(R.id.seek)
        atvDuration = baseView?.findViewById(R.id.atv_duration)
        aivFullScreen = baseView?.findViewById(R.id.aiv_full_screen)
        clBaseControl = baseView?.findViewById(R.id.cl_base_control)
    }

    /**
     * 设置监听
     */
    private fun setViewListener() {
        resumeOrPause?.setOnClickListener(this)
        aivFullScreen?.setOnClickListener(this)
    }

    fun setCurrentTimeTime(currentTime: Int) {
        lauViewModel.launchUI {
            val timeByS = PlayerUtils.getTimeByS(currentTime)
            atvPosition?.text = timeByS
            if (duration != 0 && !isTouch) {
                if (isSeek) {
                    isSeek = false
                    return@launchUI
                }
                val i = currentTime * 100 / duration
                seekBar?.progress = i
            }
        }
    }

    fun setTotalTime(totalTime: Int) {
        lauViewModel.launchUI {
            val timeByS1 = PlayerUtils.getTimeByS(totalTime)
            atvDuration?.text = timeByS1
        }
    }
}