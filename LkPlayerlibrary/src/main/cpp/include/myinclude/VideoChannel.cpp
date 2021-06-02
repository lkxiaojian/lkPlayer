//
// Created by root on 2021/5/17.
//




#include "VideoChannel.h"
#include "Macro.h"

/**
 * 丢包 avpacket
 * @param q
 */
void dropAVPacket(queue<AVPacket *> &q) {
    while (!q.empty()) {
        AVPacket *avPacket = q.front();
        //I B P 帧  不能丢 I 帧
        if (avPacket->flags != AV_PKT_FLAG_KEY) {
            //丢弃非I 帧
            BaseChannel::releaseAVPacket(&avPacket);
            q.pop();
        } else {
            break;
        }

    }
}

/**
 * 丢包 avframe
 * @param q
 */
void dropAVFrame(queue<AVFrame *> &q) {
    if (!q.empty()) {
        AVFrame *avFrame = q.front();
        BaseChannel::releaseAVFrame(&avFrame);
        q.pop();
    }
}


VideoChannel::VideoChannel(int id, AVCodecContext *avCodecContext, int fps, AVRational time_base,
                           JavaCallHelper *javaCallHelper)
        : BaseChannel(id,
                      avCodecContext, time_base, javaCallHelper) {

    this->fps = fps;
    packets.setSyncOpt(dropAVPacket);
    frames.setSyncOpt(dropAVFrame);
}

VideoChannel::~VideoChannel() {
//    avcodec_free_context(&avCodecContext);
//    avCodecContext = nullptr;
}

int VideoChannel::stop() {
    isPlaying = false;
    javaCallHelper = nullptr;
    packets.setWork(0);
    frames.setWork(0);
//    pthread_mutex_destroy(&mutex);
//    pthread_cond_destroy(&cond);
//    pthread_join(pid_video_decode, nullptr);
//    pthread_join(pid_video_play, nullptr);
    return 0;
}


void *video_play(void *args) {
    auto *videoChannel = static_cast<VideoChannel *>(args);
    videoChannel->start_play();
    return nullptr;
}


void *video_decode(void *args) {
    auto *videoChannel = static_cast<VideoChannel *>(args);
    videoChannel->start_decode();
    return nullptr;
}

void VideoChannel::start() {
    isPlaying = true;
    packets.setWork(1);
    frames.setWork(1);
    pthread_create(&pid_video_decode, nullptr, video_decode, this);
    pthread_create(&pid_video_play, nullptr, video_play, this);
}

/**
 * 视频解码
 */
void VideoChannel::start_decode() {
    AVPacket *packet = nullptr;
    while (isPlaying) {
        int ret = packets.pop(packet);
        if (!isPlaying) {
            //如果停止播放了，跳出循环 释放packet
            break;
        }
        if (!ret) {
            //取数据包失败
            continue;
        }

        //拿到了视频数据包（编码压缩了的），需要把数据包给解码器进行解码
        ret = avcodec_send_packet(avCodecContext, packet);
        if (ret) {
            //往解码器发送数据包失败
            break;
        }
        releaseAVPacket(&packet);
        AVFrame *avFrame = av_frame_alloc();
        ret = avcodec_receive_frame(avCodecContext, avFrame);
//        LOGE("av_err2str--->%s", av_err2str(ret));
        if (ret == AVERROR(EAGAIN)) {
            //重来
            continue;
        } else if (ret != 0) {
            break;
        }
        //ret=0 数据收发正常。成功获取视频原始数据包
        while (isPlaying && frames.size() > 100) {
            av_usleep(10 * 1000);
            continue;
        }

        frames.push(avFrame);
    }

    releaseAVPacket(&packet);

}

void VideoChannel::start_play() {
    pthread_mutex_lock(&mutex);
    AVFrame *avFrame = nullptr;
    //对原始数据进行转换yuv->rgba
    SwsContext *swsContext = sws_getContext(avCodecContext->width, avCodecContext->height,
                                            avCodecContext->pix_fmt,
                                            avCodecContext->width, avCodecContext->height,
                                            AV_PIX_FMT_RGBA, SWS_BILINEAR, NULL, NULL, NULL);

    uint8_t *dst_data[4];
    int det_linesize[4];
    //给dst_data det_linesize 申请内存
    av_image_alloc(dst_data, det_linesize, avCodecContext->width, avCodecContext->height,
                   AV_PIX_FMT_RGBA, 1);

    //sleep:fps>时间
    double delay_time_per_frame = 1.0 / fps;

    while (isPlaying) {
        if (isPause) {
            pthread_cond_wait(&cond, &mutex);
        }
        int ret = frames.pop(avFrame);
        if (!isPlaying) {
            //如果停止播放，跳出循环 释放packet
            break;
        }

        if (!ret) {
            //取数据包失败
            continue;
        }

        //取到了yuv原始数据，进行格式转换
        sws_scale(swsContext, avFrame->data, avFrame->linesize, 0, avCodecContext->height, dst_data,
                  det_linesize);
        //进行休眠

        double extra_delay = avFrame->repeat_pict / (2 * fps);
        double real_delay = delay_time_per_frame + extra_delay;
//获取视频的事件
        double video_time = avFrame->best_effort_timestamp * av_q2d(time_base);
        if (!audioChannel) {
            //没有音频
            av_usleep((delay_time_per_frame + extra_delay) * 1000000);
            if (javaCallHelper) {
                javaCallHelper->onProgress(THREAD_CHILD, video_time);
            }
        } else {
            double audioTime = audioChannel->audio_time;
            double time_diff = video_time - audioTime;
//            LOGE("视频比音频快：%lf", fabs(time_diff));
            if (time_diff > 0) {
                //视频比音频快
                //seek 后 time_diff 的值会很大
                if (time_diff > 1) {
                    av_usleep(real_delay * 2 * 1000000);
                } else {
                    av_usleep((real_delay + time_diff) * 1000000);
                }
            } else if (time_diff < 0) {
                //音频比视频快 追音频（丢视频包）
                //视频包 packets 和frames
                if (fabs(time_diff) >= 0.05) {
                    //时间大于0。05 ，有明显的的延迟感
                    //丢包：要操作队列的中的数据
                    packets.sync();
//                    frames.sync();
                    continue;
                }
            }
        }

        //dst_data :AV_PIX_FMT_RGBA格式的数据
        //渲染，回调出去 native-lib
        renderCallBack(dst_data[0], det_linesize[0], avCodecContext->width, avCodecContext->height);
        releaseAVFrame(&avFrame);
    }

    releaseAVFrame(&avFrame);
    isPlaying = false;

    av_free(&dst_data[0]);
    sws_freeContext(swsContext);
    pthread_mutex_unlock(&mutex);
    return;

}

void VideoChannel::setRenderCallBack(RenderCallBack back) {
    this->renderCallBack = back;
}

void VideoChannel::setAudioChannel(AudioChannel *audioChannel) {
    this->audioChannel = audioChannel;
}

void VideoChannel::setPauseOrResume(bool flag) {
    this->isPause = flag;
    if (!isPause) {
        pthread_cond_signal(&cond);
    }
}


