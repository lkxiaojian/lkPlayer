package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View

/**
 * @Description:     java类作用描述
 * @Author:         lk
 * @CreateDate:     2021/5/27 15:23
 */
class VideoPlayer(context: Context, attrs: AttributeSet?) : BasePlayerController(context, attrs),
    SurfaceTextureListener {

    private var mSurface: Surface? = null
    private var mUrl: String? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mTextureView: CustomTextureView? = null

    init {

        if (mTextureView == null) {
            mTextureView = CustomTextureView(context)
            mTextureView?.surfaceTextureListener = this
        }
        addView(mTextureView)
        if (mSurface == null) {
            mSurface = Surface(mSurfaceTexture)
        }





    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.e("","")

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        Log.e("","")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return mSurfaceTexture==null
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface
        } else {
            mSurfaceTexture?.let {
                mTextureView?.setSurfaceTexture(it)
            }
        }
    }
}