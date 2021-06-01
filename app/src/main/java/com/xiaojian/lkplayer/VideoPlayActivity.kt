package com.xiaojian.lkplayer

import android.Manifest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hjq.permissions.OnPermission
import com.hjq.permissions.XXPermissions
import com.lkxiaojian.lkplayerlibrary.view.VideoPlayer
import com.xiaojian.lkplayer.databinding.ActivityVideoPlayBinding
import java.io.File

class VideoPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayBinding
    private lateinit var builder: VideoPlayer.Builder
    val  TAG="VideoPlayActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url")
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        builder = binding.vPlayer.Builder()
        XXPermissions.with(this).permission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).request(object :
            OnPermission {
            override fun hasPermission(granted: MutableList<String>?, all: Boolean) {
                if (all) {
                    url?.let {
                        Log.e(TAG,"$TAG file ${File(url).exists()} ")
                        builder.setPath(it)
                            .setFullScreen(true)
                            .start()
                    }
                }
            }
            override fun noPermission(denied: MutableList<String>?, never: Boolean) {
                XXPermissions.startPermissionActivity(this@VideoPlayActivity, denied)
            }

        })

    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG,"$TAG onResume ")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"$TAG onStop ")
        builder.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG,"$TAG onDestroy ")
        builder.destory()
    }
}