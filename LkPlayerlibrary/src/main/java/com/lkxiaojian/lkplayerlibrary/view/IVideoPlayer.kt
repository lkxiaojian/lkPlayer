package com.lkxiaojian.lkplayerlibrary.view

/**
 *create_time : 2021/6/1 上午8:49
 *author: lk
 *description： IVideoPlayer
 */
interface IVideoPlayer {
    fun setTitle(title:String): VideoPlayer.Builder

    /**
     * TODO 开始播放
     *
     */
    fun start(): VideoPlayer.Builder

    /**
     * TODO 设置播放路径 可以是网络地址，拉流地址，文件路径地址
     * 如果是文件的，要动态申请sd读写权限 不然会返回 errorCode -13
     *
     * @param url
     * @return
     */
    fun setPath(url:String): VideoPlayer.Builder


    /**
     * TODO 设置全屏
     *
     * @param flag true 全屏  false 正常用户设置的大小
     * @return
     */
    fun setFullScreen(flag: Boolean):VideoPlayer.Builder

    /**
     * TODO 获取视频的时长（秒）
     *
     * @return 单位 s 秒
     */
    fun getDuration():Int


     fun dismissBaseControl()

    /**
     * TODO 结束播放，销毁
     *
     */
    fun stop(): VideoPlayer.Builder

    /**
     * TODO 设置 暂停 或者播放
     *
     * @param flag true 暂停 false 播放
     */
   fun setPauseOrResume(flag:Boolean): VideoPlayer.Builder

    fun destory()



}