package com.lkxiaojian.lkplayerlibrary

/**
 * @Description:     视频播放错误码
 * @Author:         lk
 * @CreateDate:     2021/5/24 14:56
 */
object ErrorCode {
    //准备过程错误码
    const val ERROR_CODE_FFMPEG_PREPARE = 1000

    //播放过程错误码
    const val ERROR_CODE_FFMPEG_PLAY = 2000

    //打不开视频
    const val FFMPEG_CAN_NOT_OPEN_URL = ERROR_CODE_FFMPEG_PREPARE - 1

    //找不到媒体流信息
    const val FFMPEG_CAN_NOT_FIND_STREAMS = ERROR_CODE_FFMPEG_PREPARE - 2

    //找不到解码器
    const val FFMPEG_FIND_DECODER_FAIL = ERROR_CODE_FFMPEG_PREPARE - 3

    //无法根据解码器创建上下文
    const val FFMPEG_ALLOC_CODEC_CONTEXT_FAIL = ERROR_CODE_FFMPEG_PREPARE - 4

    //根据流信息 配置上下文参数失败
    const val FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL = ERROR_CODE_FFMPEG_PREPARE - 5

    //打开解码器失败
    const val FFMPEG_OPEN_DECODER_FAIL = ERROR_CODE_FFMPEG_PREPARE - 6

    //没有音视频
    const val FFMPEG_NOMEDIA = ERROR_CODE_FFMPEG_PREPARE - 7

    //读取媒体数据包失败
    const val FFMPEG_READ_PACKETS_FAIL = ERROR_CODE_FFMPEG_PLAY - 1

}