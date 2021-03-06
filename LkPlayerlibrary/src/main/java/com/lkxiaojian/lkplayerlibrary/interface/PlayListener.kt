package com.lkxiaojian.lkplayerlibrary.`interface`

/**
 *create_time : 2021/5/20 上午10:48
 *author: lk
 *description： PlayListener
 */

interface PlayListener {
    fun onError(errorCode: Int)
    fun onPrepared()
    fun onComplete()
}

interface ProgressListener {
    fun progress(progress: Int)
}