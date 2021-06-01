package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.widget.ContentFrameLayout
import androidx.lifecycle.MutableLiveData
import com.lkxiaojian.lkplayerlibrary.LkPlayer
import com.lkxiaojian.lkplayerlibrary.R
import com.lkxiaojian.lkplayerlibrary.`interface`.PlayListener
import com.lkxiaojian.lkplayerlibrary.`interface`.ProgressListener
import com.lkxiaojian.lkplayerlibrary.status.PlayStatus
import com.lkxiaojian.lkplayerlibrary.status.PlayStatus.MODE_FULL_SCREEN
import com.lkxiaojian.lkplayerlibrary.status.PlayStatus.MODE_NORMAL
import com.lkxiaojian.lkplayerlibrary.status.PlayStatus.MODE_TINY_WINDOW
import com.lkxiaojian.lkplayerlibrary.utlis.PlayerUtils
import kotlinx.coroutines.delay


/**
 * @Description:     java类作用描述
 * @Author:         lk
 * @CreateDate:     2021/5/27 15:23
 */
class VideoPlayer(context: Context, attrs: AttributeSet?) : BasePlayerController(context, attrs),
    ProgressListener {
    private val mContext = context
    private var mContainer: FrameLayout? = null
    private var mSurface: Surface? = null
    private var mSurfaceView: CustomSurfaceView? = null
    private lateinit var player: LkPlayer
    private var mCurrentState = MutableLiveData<Int>()
    private var path = ""
    private var mCurrentMode: Int = MODE_NORMAL
    private var showBaseControl = false
    private var builder = Builder()

    init {
        init()
        setViewListen()
    }

    private fun setViewListen() {
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (duration == 0) {
                    duration = getTotalDuration()
                }
                val i = seekBar.progress * duration / 100
                isTouch = false
                isSeek = true
//                mCurrentState.value = PlayStatus.STATE_PLAYING
                player.seekTo(i)
            }
        })
        mCurrentState.observeForever {
            when (it) {
                PlayStatus.STATE_PLAYING -> {
                    //播放
                    resumeOrPause?.setImageResource(R.drawable.ic_player_pause)
                    builder.setPauseOrResume(PlayStatus.STATE_PLAYING == mCurrentState.value)
                }
                PlayStatus.STATE_PAUSED -> {
                    //暂停
                    builder.setPauseOrResume(PlayStatus.STATE_PLAYING == mCurrentState.value)
                    resumeOrPause?.setImageResource(R.drawable.ic_player_start)
                }
            }

        }

    }

    private fun init() {

        mCurrentState.value = PlayStatus.STATE_IDLE
        player = LkPlayer.getInstance()
        mContainer = FrameLayout(mContext)
        mContainer?.setBackgroundColor(Color.BLACK)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.addView(mContainer, params)
        mContainer?.addView(baseView)
        initTextureView()

    }

    private fun initTextureView() {
        if (mSurfaceView == null) {
            mSurfaceView = CustomSurfaceView(mContext)
        }
    }

    private fun addTextureView() {
        mContainer?.removeView(mSurfaceView)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        mContainer?.addView(mSurfaceView, 0, params)
    }

    fun start() {
        mCurrentState.value = PlayStatus.STATE_PREPARING
        addTextureView()
        if (MODE_NORMAL == MODE_FULL_SCREEN) {
            enterFullScreen()
        }
        play()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                showBaseControl = true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                showBaseControl = false
            }
        }

        return false
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.restart_or_pause -> {
                //暂停或者播放
                val flag = PlayStatus.STATE_PLAYING == mCurrentState.value
                mCurrentState.value = if (flag) {
                    PlayStatus.STATE_PAUSED
                } else {
                    PlayStatus.STATE_PLAYING
                }
            }

            R.id.aiv_full_screen -> {
                enterFullScreen()
            }
        }
    }

    private fun play() {
        mSurfaceView?.let {
            player.setSurfaceView(it)
            mSurface = it.holder?.surface
        }
        player.setDataSource(path)
        player.prepare()
        player.setPlayListener(object : PlayListener {
            override fun onError(errorCode: Int) {
                lauViewModel.launchUI {
                    //视频播放出错了
                    mCurrentState.value = PlayStatus.STATE_ERROR
                    Log.e(TAG, "errorCode-->$errorCode")
                }

            }

            override fun onPrepared() {

                setTotalTime(getTotalDuration())
                setCurrentTimeTime(0)
                player.start()
                player.setProgressListener(this@VideoPlayer)
                lauViewModel.launchUI {
                    mCurrentState.value = PlayStatus.STATE_PREPARED
                    // 设置屏幕常亮
                    mContainer?.keepScreenOn = true
                    delay(1500)

                }
            }
        })
    }

    override fun progress(progress: Int) {
        setCurrentTimeTime(progress)

    }

    private fun enterFullScreen() {
        lauViewModel.launchUI {
            if (mCurrentMode == MODE_FULL_SCREEN) return@launchUI
            // 隐藏ActionBar、状态栏，并横屏
            PlayerUtils.hideActionBar(mContext)
            PlayerUtils.scanForActivity(mContext)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val contentView = PlayerUtils.scanForActivity(mContext)
                ?.findViewById<ContentFrameLayout>(android.R.id.content)
            if (mCurrentMode == MODE_TINY_WINDOW) {
                contentView?.removeView(mContainer)
            } else {
                removeView(mContainer)
            }
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            contentView?.addView(mContainer, params)

            mCurrentMode = MODE_FULL_SCREEN
        }
    }

    //#########  提供外部使用方法  ################
    /**
     * TODO 地址 url
     *
     * @param path
     */
    fun setPath(path: String) {
        this.path = path
    }

    /**
     * TODO 获取视频的总时长
     *
     * @return
     */
    fun getTotalDuration(): Int {
        duration = player.getDuration()
        return duration
    }

    fun setFullScreen(flag: Boolean) {
        MODE_NORMAL = if (flag) {
            MODE_FULL_SCREEN
        } else {
            MODE_NORMAL
        }

    }

    fun stop() {
        builder.stop()
    }

    inner class Builder : IVideoPlayer {
        override fun start() {
            player.start()
        }

        override fun setPath(url: String) {
            player.setDataSource(url)
        }

        override fun stop() {
            player.stop()
        }

        override fun setPauseOrResume(flag: Boolean) {
            player.setPauseOrResume(flag)
        }
    }
}