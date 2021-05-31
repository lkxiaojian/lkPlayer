package com.lkxiaojian.lkplayerlibrary

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.lkxiaojian.lkplayerlibrary.`interface`.PlayListener
import com.lkxiaojian.lkplayerlibrary.`interface`.ProgressListener


/**
 *create_time : 2021/5/13 下午5:40
 *author: lk
 *description： LkPlayer
 */
class LkPlayer : SurfaceHolder.Callback {
    companion object {
        init {
            System.loadLibrary("lkplayer")
        }

        fun getInstance(): LkPlayer {
            return LkPlayer()
        }
    }

    private var playListener: PlayListener? = null
    private var progressListener: ProgressListener? = null
    private var surfaceHolder: SurfaceHolder? = null
    private lateinit var surfaceView: SurfaceView
    private var dataSource = ""

    /**
     * TODO 支持 surfaceView
     *
     * @param surfaceView
     */
    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
        this.surfaceHolder?.removeCallback(this)
        this.surfaceHolder = surfaceView.holder
        this.surfaceHolder?.addCallback(this)
    }
    fun start() {
        nativeStart()
    }

    fun setDataSource(path: String) {
        this.dataSource = path
    }

    fun prepare() {
        nativePrepare(dataSource)
    }

    /**
     * TODO 停止
     *
     */
    fun stop() {
        nativeStop()
    }

    /**
     * TODO 释放资源
     *
     */
    fun release() {
        this.surfaceHolder?.removeCallback(this)
        nativeRelease()
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    /**
     * 画布刷新
     */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        setSurfaceNative(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }


    fun setPlayListener(listener: PlayListener) {
        this.playListener = listener
    }

    fun setProgressListener(listener: ProgressListener) {
        this.progressListener = listener
    }


    /**
     * TODO 获取视频的时长
     *
     * @return
     */
    fun getDuration(): Int {
        return getNativeDuration()
    }

    /**
     * TODO 设置播放的进度
     *
     * @param progress
     */
    fun seekTo(progress: Int) {
        setNativeSeekTo(progress)
    }

    /**
     * TODO 设置播放或者暂停
     *
     * @param flag true 暂停 false 继续播放
     */
    fun setPauseOrResume(flag: Boolean){
        nativePauseOrResume(flag)
    }


    //################ native 调用 ###############
    fun onPlayError(errorCode: Int) {
        playListener?.onError(errorCode)
    }

    fun onPrepared() {
        playListener?.onPrepared()
    }

    fun setProgress(progress: Int) {
        progressListener?.progress(progress)
    }


    external fun setSurfaceNative(surface: Surface): String
    external fun nativePrepare(url: String): String
    external fun nativeStart(): String
    external fun nativeRelease()
    external fun nativeStop()
    external fun getNativeDuration(): Int
    external fun setNativeSeekTo(progress: Int)
    external fun nativePauseOrResume(flag:Boolean)
}