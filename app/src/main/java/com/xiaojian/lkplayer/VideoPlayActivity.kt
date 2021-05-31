package com.xiaojian.lkplayer

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.hjq.permissions.OnPermission
import com.hjq.permissions.XXPermissions

import com.xiaojian.lkplayer.databinding.ActivityVideoPlayBinding
import java.io.File

class VideoPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url")
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        XXPermissions.with(this).permission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).request(object :
            OnPermission {
            override fun hasPermission(granted: MutableList<String>?, all: Boolean) {
                if (all) {
                    url?.let {
                        Log.e("tag", "tag--${File(it).exists()}")
                        binding.vPlayer.setPath(it)
                        binding.vPlayer.setFullScreen(true)
                        binding.vPlayer.start()
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
        Log.e("tag", "tag-->onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.e("tag", "tag-->onStop")
        binding.vPlayer.stop()
    }
}