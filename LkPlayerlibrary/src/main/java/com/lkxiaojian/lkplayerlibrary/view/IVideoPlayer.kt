package com.lkxiaojian.lkplayerlibrary.view

/**
 *create_time : 2021/6/1 上午8:49
 *author: lk
 *description： IVideoPlayer
 */
interface IVideoPlayer {
    /**
     * TODO 开始播放
     *
     */
    fun start()


    fun setPath(url:String)

    /**
     * TODO 结束播放，销毁
     *
     */
    fun stop()

    /**
     * TODO 设置 暂停 或者播放
     *
     * @param flag true 暂停 false 播放
     */
   fun setPauseOrResume(flag:Boolean)



}