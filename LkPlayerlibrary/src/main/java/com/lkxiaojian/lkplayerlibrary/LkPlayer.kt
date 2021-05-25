package com.lkxiaojian.lkplayerlibrary

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.lkxiaojian.lkplayerlibrary.`interface`.PlayListener

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
    }

    private var playListener: PlayListener? = null
    private var surfaceHolder: SurfaceHolder? = null
    private lateinit var surfaceView: SurfaceView
    private var dataSource = ""
    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
        this.surfaceHolder?.removeCallback(this)
        this.surfaceHolder = surfaceView.holder
        this.surfaceHolder?.addCallback(this)
    }

    fun start() {
        native_start()
    }

    fun setDataSource(path: String) {
        this.dataSource = path
    }

    fun prepare() {
        native_prepare(dataSource)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    /**
     * 画布刷新
     */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        this.surfaceHolder = holder
        setSurfaceNative(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }


    fun setPlayListener(listener: PlayListener) {
        this.playListener = listener
    }


    //################ native 调用 ##########
    fun onPlayError(errorCode: Int) {
        playListener?.onError(errorCode)
    }

    fun onPrepared() {
        playListener?.onPrepared()
    }


    external fun native_startPlay(url: String, surfaceView: Surface): String

    external fun native_prepare(url: String): String
    external fun native_start(): String
    external fun setSurfaceNative(surface: Surface): String
}