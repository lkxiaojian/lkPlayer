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



//错误代码
#define ERROR_CODE_OK 0
#define ERROR_CODE_FFMPEG_PREPARE -1000
#define ERROR_CODE_FFMPEG_PLAY -2000

//打不开媒体数据源
#define FFMPEG_CAN_NOT_OPEN_URL (ERROR_CODE_FFMPEG_PREPARE - 1)
//找不到媒体流信息
#define FFMPEG_CAN_NOT_FIND_STREAMS (ERROR_CODE_FFMPEG_PREPARE - 2)
//找不到解码器
#define FFMPEG_FIND_DECODER_FAIL (ERROR_CODE_FFMPEG_PREPARE - 3)
//无法根据解码器创建上下文
#define FFMPEG_ALLOC_CODEC_CONTEXT_FAIL (ERROR_CODE_FFMPEG_PREPARE - 4)
//根据流信息 配置上下文参数失败
#define FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL (ERROR_CODE_FFMPEG_PREPARE - 5)
//打开解码器失败
#define FFMPEG_OPEN_DECODER_FAIL (ERROR_CODE_FFMPEG_PREPARE - 6)
//没有音视频
#define FFMPEG_NOMEDIA (ERROR_CODE_FFMPEG_PREPARE - 7)

//读取媒体数据包失败
#define FFMPEG_READ_PACKETS_FAIL (ERROR_CODE_FFMPEG_PLAY - 1)


#endif //LKPLAYER_MACRO_H
