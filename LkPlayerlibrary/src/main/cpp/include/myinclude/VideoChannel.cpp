//
// Created by root on 2021/5/17.
//




#include "VideoChannel.h"
#include "Macro.h"


VideoChannel::VideoChannel(int id, AVCodecContext *avCodecContext) : BaseChannel(id,
                                                                                 avCodecContext) {

}

VideoChannel::~VideoChannel() {

}

void VideoChannel::stop() {

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
    AVPacket *packet = 0;
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
        LOGE("av_err2str--->%s", av_err2str(ret));
        if (ret == AVERROR(EINVAL)) {
            //重来
            continue;
        } else if (ret != 0) {
            break;
        }

        //ret=0 数据收发正常。成功获取视频原始数据包
        frames.push(avFrame);
    }

    releaseAVPacket(&packet);

}

void VideoChannel::start_play() {
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
    while (isPlaying) {
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
        //dst_data :AV_PIX_FMT_RGBA格式的数据
        //渲染，回调出去 native-lib
        renderCallBack(dst_data[0], det_linesize[0], avCodecContext->width, avCodecContext->height);
        releaseAVFrame(&avFrame);
    }
    releaseAVFrame(&avFrame);
    isPlaying = false;
    av_free(&dst_data[0]);
    sws_freeContext(swsContext);

}

void VideoChannel::setRenderCallBack(RenderCallBack back) {
    this->renderCallBack = back;
}


