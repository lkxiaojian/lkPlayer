package com.xiaojian.lkplayer

import android.app.Application
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

/**
 *create_time : 21-6-2 下午3:28
 *author: lk
 *description： MyAp
 */
class MyAp: Application() {
    override fun onCreate() {
        super.onCreate()
        UMConfigure.init(this, "60b73271dd01c71b57d11e4f", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "")
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
    }
}