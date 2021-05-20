package com.xiaojian.lkplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.lkxiaojian.lkplayerlibrary.LkPlayer
import com.lkxiaojian.lkplayerlibrary.`interface`.PlayListener
import com.xiaojian.lkplayer.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val lkPlayer = LkPlayer()
        lkPlayer.setSurfaceView(binding.surfaceView)
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

        lkPlayer.setPlayListener(object : PlayListener {
            override fun onError(errorMessage:String,errorCode: Int) {
                Log.e("Tag", "tag-->errorCode $errorCode  $errorMessage  ${Thread.currentThread()}")
            }

            override fun onPrepared() {
                Log.e("Tag", "tag-->准备播放${Thread.currentThread()}")

                Toast.makeText(this@MainActivity, "准备播放", Toast.LENGTH_SHORT).show()
            }

        })
        binding.abtStart.setOnClickListener {
            val file = File(Environment.getExternalStorageDirectory(), "input.mp4")
            Log.e("tag", "file--" + file.exists())
            lkPlayer.start(file.path)
        }


    }


}