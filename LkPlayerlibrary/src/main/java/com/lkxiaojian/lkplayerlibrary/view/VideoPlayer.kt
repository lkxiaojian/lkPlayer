package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.SeekBar
import com.lkxiaojian.lkplayerlibrary.LkPlayer
import com.lkxiaojian.lkplayerlibrary.R
import com.lkxiaojian.lkplayerlibrary.`interface`.PlayListener
import com.lkxiaojian.lkplayerlibrary.`interface`.ProgressListener
import com.lkxiaojian.lkplayerlibrary.status.PlayStatus


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
    private var mSurfaceView: SurfaceView? = null
    private lateinit var player: LkPlayer
    private var mCurrentState: Int = PlayStatus.STATE_IDLE
    private var path = ""

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

                if(duration==0){
                    duration= getTotalDuration()
                }
//                val progress = seekBar.progress
//                val i1 = progress * duration
//                Log.e(TAG,"i--ww>${progress} duration-->$duration  i1---$i1")
                val i = seekBar.progress*duration/100
                Log.e(TAG,"i-->$i")
                isTouch = false
                isSeek=true
                player.seekTo(i)
            }
        })
    }

    private fun init() {
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
        mCurrentState = PlayStatus.STATE_PREPARING
        addTextureView()
        play()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.restart_or_pause -> {
                //暂停或者播放
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
                //解析出错了
                mCurrentState = PlayStatus.STATE_ERROR
            }

            override fun onPrepared() {
                mCurrentState = PlayStatus.STATE_PREPARED
                setTotalTime(getTotalDuration())
                setCurrentTimeTime(0)
                player.start()

                player.setProgressListener(this@VideoPlayer)
            }
        })
    }

    override fun progress(progress: Int) {
        setCurrentTimeTime(progress)

    }

    //#########  提供外部使用方法  ################
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
}