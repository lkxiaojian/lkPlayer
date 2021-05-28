package com.lkxiaojian.lkplayerlibrary.view

import android.content.Context
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import android.widget.FrameLayout

/**
 * @Description:     java类作用描述
 * @Author:         lk
 * @CreateDate:     2021/5/27 15:23
 */
class VideoPlayer(context: Context, attrs: AttributeSet?) : BasePlayerController(context, attrs),
    SurfaceTextureListener {
    private val mContext = context
    private var mContainer: FrameLayout? = null
    private var mSurface: Surface? = null
    private var mUrl: String? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mTextureView: CustomTextureView? = null

    init {
//        if (mTextureView == null) {
//            mTextureView = CustomTextureView(context)
//            mTextureView?.surfaceTextureListener = this
//        }
//        this.addView(mTextureView)
//        if (mSurface == null) {
//            mSurface = Surface(mSurfaceTexture)
//        }
        init()
    }


    private fun init() {
        mContainer = FrameLayout(mContext)
        mContainer?.setBackgroundColor(Color.BLACK)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.addView(mContainer, params)
        mContainer?.addView(baseView)
    }

    private fun initTextureView() {
        if (mTextureView == null) {
            mTextureView = CustomTextureView(mContext)
            mTextureView?.surfaceTextureListener = this
        }
    }

    private fun addTextureView() {
        mContainer?.removeView(mTextureView)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        mContainer?.addView(mTextureView, 0, params)
    }


    fun start() {
        initTextureView()
        addTextureView()
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return mSurfaceTexture == null
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