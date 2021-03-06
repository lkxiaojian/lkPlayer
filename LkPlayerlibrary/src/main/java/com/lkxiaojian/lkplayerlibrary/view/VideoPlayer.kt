package com.lkxiaojian.lkplayerlibrary.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.AudioManager
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
import kotlin.math.abs


/**
 * @Description:     java类作用描述
 * @Author:         lk
 * @CreateDate:     2021/5/27 15:23
 */
class VideoPlayer(context: Context, attrs: AttributeSet?) : BasePlayerController(context, attrs),
    ProgressListener, View.OnTouchListener {
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
    private var mDownX = 0f
    private var mDownY = 0f
    private var mAudioManager: AudioManager? = null

    init {
        init()
        setViewListen()
    }

    private fun setViewListen() {
        setOnTouchListener(this)
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (duration == 0) {
                    duration = builder.getDuration()
                }
                val i = seekBar.progress * duration / 100
                isTouch = false
                isSeek = true
//                mCurrentState.value = PlayStatus.STATE_PLAYING
                builder.seekTo(i)
            }
        })
        mCurrentState.observeForever {
            when (it) {
                PlayStatus.STATE_PLAYING -> {
                    //播放
                    resumeOrPause?.setImageResource(R.drawable.ic_player_pause)
                }
                PlayStatus.STATE_PAUSED -> {
                    //暂停
//                    builder.setPauseOrResume(PlayStatus.STATE_PLAYING == mCurrentState.value)
                    resumeOrPause?.setImageResource(R.drawable.ic_player_start)
                }
            }

        }

    }

    @SuppressLint("ClickableViewAccessibility")
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
        mContainer?.setOnTouchListener(this)
        mContainer?.addView(baseView)
        initTextureView()
        initAudioManager()
    }

    private fun initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mAudioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
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

    private fun start() {
        mCurrentState.value = PlayStatus.STATE_PREPARING
        addTextureView()
        if (mCurrentMode == MODE_FULL_SCREEN) {
            enterFullScreen()
        }
        play()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = x
                mDownY = y
                showBaseControl = true
                clBaseControl?.visibility = View.VISIBLE
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mNewPosition != -1 && duration != 0) {
                    builder.seekTo(mNewPosition)
                }
                lauViewModel.launchUI {
                    delay(100)
                    isMoveing = false
                    showBaseControl = false
                    builder.dismissBaseControl()
                    mNewPosition = -1
                }


            }
            MotionEvent.ACTION_MOVE -> {
                isMoveing = true
                showBaseControl = true
                if (mCurrentState.value == PlayStatus.STATE_PLAYING ||
                    mCurrentState.value == PlayStatus.STATE_PAUSED ||
                    mCurrentState.value == PlayStatus.STATE_BUFFERING_PLAYING ||
                    mCurrentState.value == PlayStatus.STATE_BUFFERING_PAUSED
                ) {
                    var deltaX: Float = x - mDownX
                    var deltaY: Float = y - mDownY
                    val absDeltaX = abs(deltaX)
                    val absDeltaY = abs(deltaY)


                    // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                    if (absDeltaX >= THRESHOLD) {
                        //改变进度
                        lauViewModel.launchUI {
                            mGestureDownPosition = builder.getCurrentTime()
                            duration = builder.getDuration()
                            val toPosition =
                                (mGestureDownPosition + duration * deltaX / 2 / width).toInt()
                            mNewPosition =
                                0.coerceAtLeast(duration.coerceAtMost(toPosition))
                            Log.e(TAG, "mNewPosition-->$mNewPosition")
//                            val newPositionProgress = (100f * mNewPosition / duration).toInt()
                            showChangePosition(mNewPosition)
                        }


                    } else if (absDeltaY >= THRESHOLD) {
                        if (mDownX < width * 0.5f) {
                            // 左侧改变亮度
                            lauViewModel.launchUI {
                                mGestureDownBrightness =
                                    PlayerUtils.scanForActivity(mContext)?.window?.attributes?.screenBrightness
                                deltaY = -deltaY
                                val deltaBrightness = deltaY / 20 / height
                                var newBrightness =
                                    mGestureDownBrightness!! + deltaBrightness
                                newBrightness =
                                    0f.coerceAtLeast(newBrightness.coerceAtMost(1f))
                                val newBrightnessPercentage = newBrightness
                                val params: WindowManager.LayoutParams? =
                                    PlayerUtils.scanForActivity(mContext)
                                        ?.window?.attributes
                                params?.screenBrightness = newBrightnessPercentage
                                PlayerUtils.scanForActivity(mContext)?.window?.attributes = params
                                val newBrightnessProgress = (100f * newBrightnessPercentage).toInt()
                                Log.e(TAG, "newBrightnessProgress-->$newBrightnessProgress")
                                showChangeBrightness(newBrightnessProgress)
                            }


                        } else {
                            // 右侧改变声音
                            lauViewModel.launchUI {
                                mGestureDownVolume = builder.getVolume()
                                deltaY = -deltaY
                                val maxVolume: Int = builder.getMaxVolume()
                                val deltaVolume = (maxVolume * deltaY / 20 / height).toInt()
                                var newVolume = mGestureDownVolume + deltaVolume
                                newVolume =
                                    0.coerceAtLeast(maxVolume.coerceAtMost(newVolume))
                                builder.setVolume(newVolume)
                                val newVolumeProgress = (100f * newVolume / maxVolume).toInt()
                                showChangeVolume(newVolumeProgress)
                            }
                        }
                    }

                }


            }


        }

        return true
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
                builder.setPauseOrResume(flag)

            }
            R.id.aiv_full_screen -> {

                if (mCurrentMode != MODE_FULL_SCREEN) {
                    enterFullScreen()
                } else {
                    exitFullScreen()
                }
                builder.dismissBaseControl()

            }
            R.id.aiv_back -> {
                //按返回键 如果是全屏，就退出全屏
                if (mCurrentMode == MODE_FULL_SCREEN) {
                    exitFullScreen()
                }
//                else{
//                    //退出
////                    exitTinyWindow
//                }

            }
        }
    }

    /**
     * TODO 退出全屏
     *
     */
    private fun exitFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            aivFullScreen?.setImageResource(R.drawable.ic_player_enlarge)
            PlayerUtils.showActionBar(mContext)
            PlayerUtils.scanForActivity(mContext)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val contentView = PlayerUtils.scanForActivity(mContext)
                ?.findViewById(android.R.id.content) as ViewGroup
            mContainer
            contentView.removeView(mContainer)
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(mContainer, params)
            mCurrentMode = MODE_NORMAL
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
                setTotalTime(builder.getDuration())
                val savedPlayPosition = PlayerUtils.getSavedPlayPosition(mContext, path)
                setCurrentTimeTime(savedPlayPosition)
                player.start()
                player.seekTo(savedPlayPosition)
                player.setProgressListener(this@VideoPlayer)
                lauViewModel.launchUI {
                    mCurrentState.value = PlayStatus.STATE_PLAYING
                    // 设置屏幕常亮
                    mContainer?.keepScreenOn = true

                }
                builder.dismissBaseControl()
            }

            override fun onComplete() {

            }
        })
    }

    override fun progress(progress: Int) {
        setCurrentTimeTime(progress)
    }

    private fun enterFullScreen() {
        lauViewModel.launchUI {
            aivFullScreen?.setImageResource(R.drawable.ic_player_shrink)
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

    inner class Builder : IVideoPlayer {
        override fun setTitle(m: String): Builder {
            title?.text = m
            return this
        }

        override fun start(): Builder {
            this@VideoPlayer.start()
            return this
        }

        override fun setPath(url: String): Builder {
            this@VideoPlayer.path = url
            return this
        }

        override fun setFullScreen(flag: Boolean): Builder {
            mCurrentMode = if (flag) {
                MODE_FULL_SCREEN
            } else {
                MODE_NORMAL
            }
            return this
        }

        override fun getDuration(): Int {
            duration = player.getDuration()
            return duration
        }

        override fun getCurrentTime(): Int {
            return player.getCurrentTime()
        }

        override fun getMaxVolume(): Int {
            return if (mAudioManager != null) {
                mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            } else 0
        }

        override fun getVolume(): Int {
            return if (mAudioManager != null) {
                mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            } else 0
        }

        override fun setVolume(volume: Int) {
            mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        }

        override fun seekTo(time: Int) {
            player.seekTo(time)
        }

        override fun dismissBaseControl() {
            if (!showBaseControl) {
                lauViewModel.launchUI {
                    delay(30000)
                    clBaseControl?.visibility = View.GONE
                    changeVolume?.visibility = View.GONE
                    changeBrightness?.visibility = View.GONE
                }
            }

        }

        override fun stop(): Builder {
            PlayerUtils.savePlayPosition(mContext, path, getCurrentTime())
            player.stop()
            return this
        }

        override fun setPauseOrResume(flag: Boolean): Builder {
            player.setPauseOrResume(flag)
            return this
        }

        override fun destory() {
            player.release()
        }
    }
}