//
// Created by root on 2021/5/17.
//




#include "LkFfmpage.h"


LkFfmpage::LkFfmpage(JavaCallHelper *javaCallHelper, char *dataSource) {
    this->javaCallHelper = javaCallHelper;
    this->dataSource = new char[strlen(dataSource) + 1];
    strcpy(this->dataSource, dataSource);

}

LkFfmpage::~LkFfmpage() {
    DELETE(dataSource)
    DELETE(javaCallHelper)
}

/**
 *  准备线程的真正执行的函数
 * @return
 */
void *task_prepare(void *args) {

    LkFfmpage *ffmpage = static_cast<LkFfmpage *>(args);
    ffmpage->_prepare();

    return 0;
}

/**
 * 播放准备
 */
void LkFfmpage::prepare() {
    //文件 io
    //直播 网络
    //创建主线程
    pthread_create(&pid_prepare, nullptr, task_prepare, this);
}

void LkFfmpage::_prepare() {
    //打开输入
//    avformat_network_init();
    avFormatContext = avformat_alloc_context();
    AVDictionary *options = nullptr;
    av_dict_set(&options, "timeout", "1000000", 0);

    int ret = avformat_open_input(&avFormatContext, dataSource, nullptr, &options);
    av_dict_free(&options);
    if (ret) {
        //失败
        LOGE("打开媒体失败: %s", av_err2str(ret));
        javaCallHelper->onError(THREAD_CHILD, av_err2str(ret), ret);
        return;
    }

    ret = avformat_find_stream_info(avFormatContext, nullptr);

    if (ret) {
        //失败
        LOGE("获取流失败: %s", av_err2str(ret));
        return;
    }

    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        //获取流媒体（音视频）
        AVStream *stream = avFormatContext->streams[i];
        //获取编解码这段流的参数
        AVCodecParameters *codecParameters = stream->codecpar;
        //通过参数ID(解码的方式)，来查找当前流的解码器
        const AVCodec *avCodec = avcodec_find_decoder(codecParameters->codec_id);
        if (!avCodec) {
            LOGE("获取解码器失败: %s", av_err2str(ret));
            return;
        }
        //获取解码器的上下文参数
        AVCodecContext *avCodecContext = avcodec_alloc_context3(avCodec);
        ret = avcodec_parameters_to_context(avCodecContext, codecParameters);
        if (ret) {
            LOGE("获取解码器上下文失败: %s", av_err2str(ret));
            return;
        }
        //打开解码器
        ret = avcodec_open2(avCodecContext, avCodec, nullptr);
        if (ret) {
            LOGE("打开解码器失败: %s", av_err2str(ret));
            return;
        }
        //判断流类型（音频还是视频）
        if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
            //视频
            videoChannel = new VideoChannel(i, avCodecContext);
            videoChannel->setRenderCallBack(renderCallBack);
        } else if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
            //音频
            audioChannel = new AudioChannel(i, avCodecContext);
        }
    }

    if (!videoChannel && !audioChannel) {
        LOGE("未获取到音视频流: %s", av_err2str(ret));
        javaCallHelper->onError(THREAD_CHILD, av_err2str(ret), ret);
        return;
    }
    //准备播放，通知java层
    javaCallHelper->onPrepared(THREAD_CHILD);
}

/**
 *  开始播放
 * @param args
 * @return
 */
void *task_start(void *args) {

    auto *ffmpage = static_cast<LkFfmpage *>(args);
    ffmpage->_start();

    return 0;
}

/**
 *  开启子线程播放
 */
void LkFfmpage::start() {
    isPlaying = true;
    videoChannel->start();
    pthread_create(&pid_start, nullptr, task_start, this);
}

/**
 * 真正播放的逻辑
 */
void LkFfmpage::_start() {
    while (isPlaying) {
        AVPacket *avPacket = av_packet_alloc();
        int ret = av_read_frame(avFormatContext, avPacket);
        if (!ret) {
            if (videoChannel && avPacket->stream_index == videoChannel->id) {
                videoChannel->packets.push(avPacket);
            } else if (audioChannel && avPacket->stream_index == audioChannel->id) {
//                audioChannel->frames.push()
            }
        } else if (ret == AVERROR_EOF) {
//表示读完了
//要考虑读完了，是否播放完了的情况
            LOGE("读取音视频 AVERROR_EOF ");
        } else {
            LOGE("读取音视频失败");
            break;
        }
    }

    isPlaying = false;
    //停止解码音频 和视频
    videoChannel->stop();
    audioChannel->stop();
}

void LkFfmpage::setRenderCallBack(RenderCallBack back) {
    this->renderCallBack = back;
}

