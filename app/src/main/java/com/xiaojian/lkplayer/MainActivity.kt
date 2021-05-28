package com.xiaojian.lkplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.lkxiaojian.lkplayerlibrary.LkPlayer
import com.lkxiaojian.lkplayerlibrary.`interface`.PlayListener
import com.lkxiaojian.lkplayerlibrary.`interface`.ProgressListener
import com.xiaojian.lkplayer.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val lkPlayer = LkPlayer()
    private var isTouch = false
    private var isSeek = false
    override fun onCreate(savedInstanceState: Bundle?) {
        val w: Window = this.window
        w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,0)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        lkPlayer.setSurfaceView(binding.surfaceView)
//        val p="https://v-cdn.zjol.com.cn/280443.mp4"
        lkPlayer.setDataSource(file.path)
        var duration = 0
        lkPlayer.setPlayListener(object : PlayListener {
            override fun onError(errorCode: Int) {
                Log.e("Tag", "tag-->errorCode $errorCode   ${Thread.currentThread()}")
            }

            override fun onPrepared() {
                Log.e("Tag", "tag-->准备播放${Thread.currentThread()}")
                Toast.makeText(this@MainActivity, "准备播放", Toast.LENGTH_SHORT).show()
                duration = lkPlayer.getDuration()

                lkPlayer.nativeStart()
            }

        })
        lkPlayer.setProgressListener(object : ProgressListener {
            override fun progress(progress: Int) {
                Log.e("tag-->", "progress-->$progress  duration-->$duration")
                if (duration != 0&&!isTouch) {
                    if (isSeek) {
                        isSeek = false
                        return
                    }
                    binding.asb.progress = progress*100/duration
                }

            }

        })
        binding.abtStart.setOnClickListener {
            lkPlayer.prepare()
        }

        binding.asb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //获取当前的seekBar的当前进度
                isTouch = false
                isSeek=true
                val progress = seekBar.progress * duration / 100
                lkPlayer.seekTo(progress)

            }

        })


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        lkPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        lkPlayer.release()
    }
}