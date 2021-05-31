package com.lkxiaojian.lkplayerlibrary.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 *create_time : 21-5-31 上午10:17
 *author: lk
 *description： LauViewModel
 */
class LauViewModel : ViewModel() {
    fun launchUI(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {

            }
        }
}



