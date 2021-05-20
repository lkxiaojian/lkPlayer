//
// Created by root on 2021/5/20.
//定义宏函数

#ifndef LKPLAYER_MACRO_H
#define LKPLAYER_MACRO_H
#include <android/log.h>
//释放
#define   DELETE(object) if(object){ delete object;object=0;}
#define TAG "LKFFMPAEG"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
// 定义info信息
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
// 定义debug信息
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

//主线程
#define THREAD_MAIN 1
//子线程
#define THREAD_CHILD 2

#endif //LKPLAYER_MACRO_H
