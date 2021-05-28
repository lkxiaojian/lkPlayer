package com.xiaojian.lkplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.xiaojian.lkplayer.databinding.ActivityMainBinding
import com.xiaojian.lkplayer.databinding.ActivityVideoPlayBinding

class VideoPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
//        val w: Window = this.window
//        w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,0)
        super.onCreate(savedInstanceState)

        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.vPlayer.start()

    }
}