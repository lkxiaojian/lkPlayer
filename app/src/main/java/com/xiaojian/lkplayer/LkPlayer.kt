package com.xiaojian.lkplayer

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView

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

    private var surfaceHolder: SurfaceHolder? = null
    private lateinit var surfaceView: SurfaceView;
    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView;
        this.surfaceHolder?.removeCallback(this)
        this.surfaceHolder = surfaceView.holder
        this.surfaceHolder?.addCallback(this)
    }

    fun start(path:String){
        this.surfaceHolder?.let {
            native_startPlay(path, it.surface)
        }

    }


    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        this.surfaceHolder = holder
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    external fun native_startPlay(url: String, surfaceView: Surface): String
}