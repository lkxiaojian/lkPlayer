package com.lkxiaojian.lkplayerlibrary.status

/**
 *create_time : 21-5-31 上午9:16
 *author: lk
 *description： PlayStatus  视频播放状态
 */
object PlayStatus {
    /**
     * 播放错误
     **/
    const val STATE_ERROR = -1

    /**
     * 播放未开始
     **/
    const val STATE_IDLE = 0

    /**
     * 播放准备中
     **/
    const val STATE_PREPARING = 1

    /**
     * 播放准备就绪
     **/
    const val STATE_PREPARED = 2

    /**
     * 正在播放
     **/
    const val STATE_PLAYING = 3

    /**
     * 暂停播放
     **/
    const val STATE_PAUSED = 4

    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     **/
    const val STATE_BUFFERING_PLAYING = 5

    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
     */
    const val STATE_BUFFERING_PAUSED = 6

    /**
     * 播放完成
     */
    const val STATE_COMPLETED = 7

    /**
     * 普通模式
     */
    var MODE_NORMAL = 10

    /**
     * 全屏模式
     */
    const val MODE_FULL_SCREEN = 11

    /**
     * 小窗口模式
     */
    const val MODE_TINY_WINDOW = 12




}