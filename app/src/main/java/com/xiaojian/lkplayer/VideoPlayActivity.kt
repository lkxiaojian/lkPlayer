package com.xiaojian.lkplayer

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.xiaojian.lkplayer.databinding.ActivityMainBinding
import com.xiaojian.lkplayer.databinding.ActivityVideoPlayBinding
import java.io.File

class VideoPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
//        val w: Window = this.window
//        w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,0)
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 说明没有该权限，就需要申请权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            );
        }

        val file = File(Environment.getExternalStorageDirectory(), "input.mp4")
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.vPlayer.setPath(file.path)
        binding.vPlayer.start()

    }

    override fun onPause() {
        super.onPause()
    }
}