package com.lkxiaojian.lkplayerlibrary.utlis

/**
 *create_time : 21-5-31 上午9:54
 *author: lk
 *description： PlayerUtils
 */
object PlayerUtils {

    fun getTimeByS(time: Int): String {
        return when {
            time < 60 -> {
                when {
                    time == 0 -> {
                        "00:00"
                    }
                    time < 9 -> {
                        "00:0$time"
                    }
                    else -> {
                        "00:$time"
                    }
                }
            }
            time in 61..3599 -> {
                val m = time / 60
                val s = time % 60
                if (s < 9) {
                    "$m:0$s"
                } else {
                    "$m:$s"
                }
            }
            else -> {
                val h = time / 3600
                val m = (time % 3600) / 60
                val s = (time % 3600) % 60
                var sm = "$m"
                var ss = "$s"
                if (m < 9) {
                    sm = "0$m"
                }
                if (s < 9) {
                    ss = "0$s"
                }
                "$h:$sm:$ss"
            }
        }

    }
}