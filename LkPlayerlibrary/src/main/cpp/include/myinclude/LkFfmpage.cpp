#include <alloca.h>
//
// Created by root on 2021/5/17.
//




#include "LkFfmpage.h"


LkFfmpage::LkFfmpage(JavaCallHelper *javaCallHelper, char *dataSource) {
    this->javaCallHelper = javaCallHelper;
    this->dataSource = new char[strlen(dataSource) + 1];
    strcpy(this->dataSource, dataSource);
//    pthread_mutex_init(&seekMutex, nullptr);


}

LkFfmpage::~LkFfmpage() {
    DELETE(dataSource)
    DELETE(javaCallHelper)
//    pthread_mutex_destroy(&seekMutex);
}

/**
 *  准备线程的真正执行的函数
 * @return
 */
void *task_prepare(void *args) {

    auto *ffmpage = static_cast<LkFfmpage *>(args);
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
    avformat_network_init();
//    av_register_all();

    avFormatContext = avformat_alloc_context();
    AVDictionary *options = nullptr;
    av_dict_set(&options, "timeout", "3000000", 0);

    int ret = avformat_open_input(&avFormatContext, dataSource, nullptr, &options);
    av_dict_free(&options);
    if (ret) {
        //失败
        LOGE("打开媒体失败: %s", av_err2str(ret));
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, ret);
        }
        return;
    }
//查找媒体中的流信息
    ret = avformat_find_stream_info(avFormatContext, nullptr);

    if (ret) {
        //失败
        LOGE("获取流失败: %s", av_err2str(ret));
        return;
    }
    duration = avFormatContext->duration / AV_TIME_BASE;
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
        if (!avCodecContext) {
            LOGE("创建解码器上下文失败");
            if (javaCallHelper) {
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_ALLOC_CODEC_CONTEXT_FAIL);
            }

        }
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
        AVRational time_base = stream->time_base;
        if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
            //视频
            //采样率
            AVRational avRational = stream->r_frame_rate;
            int fps = av_q2d(avRational);
            avVideoCodecContext = avCodecContext;
            videoChannel = new VideoChannel(i, avCodecContext, fps, time_base, javaCallHelper);
            videoChannel->setRenderCallBack(renderCallBack);
        } else if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
            //音频
            avAudioCodecContext = avCodecContext;
            audioChannel = new AudioChannel(i, avCodecContext, time_base, javaCallHelper);
        }
    }

    if (!videoChannel && !audioChannel) {
        LOGE("未获取到音视频流: %s", av_err2str(ret));
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, ret);
        }
        return;
    }
    //准备播放，通知java层
    if (javaCallHelper) {
        javaCallHelper->onPrepared(THREAD_CHILD);
    }
}

/**
 *  开始播放
 * @param args
 * @return
 */
void *task_start(void *args) {
    auto *ffmpage = static_cast<LkFfmpage *>(args);
    ffmpage->_start();
    return nullptr;
}

/**
 *  停止播放
 * @param args
 * @return
 */
void *task_stop(void *args) {
    auto *ffmpage = static_cast<LkFfmpage *>(args);
    ffmpage->_stop(ffmpage);
    return nullptr;
}

/**
 *  开启子线程播放
 */
void LkFfmpage::start() {

    isPlaying = true;
    if (videoChannel) {
        videoChannel->setAudioChannel(audioChannel);
        videoChannel->start();
    }
    if (audioChannel) {
        audioChannel->start();
    }
    pthread_create(&pid_start, nullptr, task_start, this);

}


/**
 * 真正播放的逻辑
 */
void LkFfmpage::_start() {
    while (isPlaying) {

        if (videoChannel != nullptr && videoChannel->frames.size() > 256) {
            av_usleep(10 * 1000);
            continue;
        }
        if (audioChannel != nullptr && audioChannel->frames.size() > 256) {
            av_usleep(10 * 1000);
            continue;
        }

        AVPacket *avPacket = av_packet_alloc();
        int ret = av_read_frame(avFormatContext, avPacket);
        if (!ret) {
            if (videoChannel && avPacket->stream_index == videoChannel->id) {
                start_decode(avPacket, avVideoCodecContext, 2);

            } else if (audioChannel && avPacket->stream_index == audioChannel->id) {
                start_decode(avPacket, avAudioCodecContext, 1);
            }
        } else if (ret == AVERROR_EOF) {
            //表示读完了
            //要考虑读完了，是否播放完了的情况
            if ( videoChannel->frames.empty() &&audioChannel->frames.empty()) {
                //播放完成
                av_packet_free(&avPacket);
                if (javaCallHelper) {
                    javaCallHelper->onComplete(THREAD_CHILD);
                }
                break;
            }
        } else {
            if (javaCallHelper) {
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_READ_PACKETS_FAIL);
            }
            LOGE("读取音视频失败");
            av_packet_free(&avPacket);
            break;
        }
    }

    isPlaying = false;
    //停止解码音频 和视频
    if (videoChannel) {
        videoChannel->stop();
    }
    if (audioChannel) {
        audioChannel->stop();
    }

}

void LkFfmpage::setRenderCallBack(RenderCallBack back) {
    this->renderCallBack = back;
}

/**
 * 停止播放
 */
void LkFfmpage::stop() {
    //开启子线程 防止ANR
    pthread_create(&pid_sop, nullptr, task_stop, this);
}

int LkFfmpage::_stop(LkFfmpage *lkFfmpage) {
    isPlaying = false;

    //保证prepare 中的子线程执行完成了再执行后续的炒作  阻塞(sè)后面的方法
    pthread_join(pid_prepare, 0);
    if (avFormatContext) {
        avformat_close_input(&avFormatContext);
        avformat_free_context(avFormatContext);
        avFormatContext = nullptr;
    }

    DELETE(videoChannel)
    DELETE(audioChannel)
    DELETE(lkFfmpage)
    return 0;
}

int LkFfmpage::getDuration() const {
    return duration;
}

jint LkFfmpage::setSeekToProgress(int progress) {
    if (progress < 0 || progress > duration) {
        return -1;
    }
    if (!avFormatContext) {
        return -1;
    }
//    pthread_mutex_lock(&seekMutex);
    int ret = av_seek_frame(avFormatContext, -1, progress * AV_TIME_BASE, AVSEEK_FLAG_BACKWARD);
//    int ret = avformat_seek_file(avFormatContext, -1, 0, progress * AV_TIME_BASE, duration,
//                                 AVSEEK_FLAG_BACKWARD);
    if (ret < 0) {
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, ret);
        }
    }

    if (audioChannel) {
        audioChannel->frames.setWork(0);
        audioChannel->frames.clear();
        audioChannel->frames.setWork(1);
    }

    if (videoChannel) {
        videoChannel->frames.setWork(0);
        videoChannel->frames.clear();
        videoChannel->frames.setWork(1);
    }
//    pthread_mutex_unlock(&seekMutex);
    return 0;
}

/**
 * TODO 设置播放或者暂停
 *
 * @param flag true 暂停 false 继续播放
 */
void LkFfmpage::setPauseOrResume(bool flag) {
    if (videoChannel) {
        videoChannel->setPauseOrResume(flag);
    }
    if (audioChannel) {
        audioChannel->setPauseOrResume(flag);
    }


}

int LkFfmpage::getCurrentTime() {
    if (audioChannel) {
        return audioChannel->audio_time;
    }
    if (videoChannel) {
        return videoChannel->audio_time;
    }

    return 0;
}

void LkFfmpage::start_decode(AVPacket *packet, AVCodecContext *avCodecContext, int type) {
    if (!isPlaying) {
        //如果停止播放了，跳出循环 释放packet
        return;
    }

    //拿到了视频数据包（编码压缩了的），需要把数据包给解码器进行解码
    int ret = avcodec_send_packet(avCodecContext, packet);
    if (ret) {
        //往解码器发送数据包失败
        return;
    }
    av_packet_free(&packet);
    packet = nullptr;
    AVFrame *avFrame = av_frame_alloc();
    ret = avcodec_receive_frame(avCodecContext, avFrame);
    if (ret == AVERROR(EAGAIN)) {
        //重来
        av_frame_free(&avFrame);
        return;
    } else if (ret != 0) {
        av_frame_free(&avFrame);
        return;
    }
    //ret=0 数据收发正常。成功获取视频原始数据包

//        while (isPlaying && frames.size() > 100) {
//            av_usleep(10 * 1000);
//        }

    if (type == 1) {
        audioChannel->frames.push(avFrame);
//            av_frame_free(&avFrame);
    } else {
        videoChannel->frames.push(avFrame);
//            av_frame_free(&avFrame);
    }


}

