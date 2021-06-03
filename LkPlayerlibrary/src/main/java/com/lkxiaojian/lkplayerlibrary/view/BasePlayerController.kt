package com.lkxiaojian.lkplayerlibrary.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
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
@SuppressLint("InflateParams")
abstract class BasePlayerController(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs), View.OnClickListener, View.OnTouchListener {
    private var atvPosition: AppCompatTextView? = null
    private var atvDuration: AppCompatTextView? = null
    var baseView: View? = null
    var resumeOrPause: AppCompatImageView? = null
    var clBaseControl: ConstraintLayout? = null
    var changeVolume: ConstraintLayout? = null//改变声音
    var changeBrightness: ConstraintLayout? = null//改变亮度


    var aivFullScreen: AppCompatImageView? = null
    var aivBack: AppCompatImageView? = null
    var title: AppCompatTextView? = null
    var isTouch = false//是否在触摸进度条
    var isSeek = false//是否在移动进度条
    var seekBar: SeekBar? = null
    var change_volume_progress: ProgressBar? = null
    var change_brightness_progress: ProgressBar? = null
    var lauViewModel: LauViewModel
    var duration = 0
    val THRESHOLD = 80
    var mGestureDownBrightness: Float? = 0f
    var mGestureDownVolume = 0
    var mGestureDownPosition=0
    var mNewPosition=-1
    var isMoveing=false//是否在滑动屏幕跳动进度条

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
        title = baseView?.findViewById(R.id.title)
        aivBack = baseView?.findViewById(R.id.aiv_back)

        changeVolume = baseView?.findViewById(R.id.change_volume)
        changeBrightness = baseView?.findViewById(R.id.change_brightness)

        change_brightness_progress = baseView?.findViewById(R.id.change_brightness_progress)
        change_volume_progress = baseView?.findViewById(R.id.change_volume_progress)
    }

    /**
     * 设置监听
     */
    private fun setViewListener() {
        resumeOrPause?.setOnClickListener(this)
        aivFullScreen?.setOnClickListener(this)
        aivBack?.setOnClickListener(this)
//        setOnTouchListener(this)
    }

    fun showChangePosition(currentTime: Int) {
        lauViewModel.launchUI {

            if (duration != 0) {
                val formatTime = PlayerUtils.formatTime(currentTime.toLong())
                atvPosition?.text = formatTime
                val i = currentTime * 100 / duration
                seekBar?.progress = i
            }
        }
    }

    fun setCurrentTimeTime(currentTime: Int) {
        lauViewModel.launchUI {
            if(!isMoveing) {
                val timeByS = PlayerUtils.formatTime(currentTime.toLong())
                atvPosition?.text = timeByS
            }
            if (duration != 0 && !isTouch&&!isMoveing) {
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
            val timeByS1 = PlayerUtils.formatTime(totalTime.toLong())
            atvDuration?.text = timeByS1
        }
    }

    protected fun showChangeVolume(newVolumeProgress: Int) {
        changeVolume?.visibility = View.VISIBLE
        changeBrightness?.visibility = View.GONE
        change_volume_progress?.progress = newVolumeProgress
    }

    protected fun showChangeBrightness(newVolumeProgress: Int) {
        changeBrightness?.visibility = View.VISIBLE
        changeVolume?.visibility = View.GONE
        change_brightness_progress?.progress = newVolumeProgress
    }

}