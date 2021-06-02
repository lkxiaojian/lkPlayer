//
// Created by root on 2021/5/17.
//

#include "BaseChannel.h"

BaseChannel::~BaseChannel() {
    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond);
    packets.clear();
    frames.clear();
    if(avCodecContext!= nullptr){
        avcodec_close(avCodecContext);
        avcodec_free_context(&avCodecContext);
        avCodecContext= nullptr;
    }

}

void BaseChannel::releaseAVPacket(AVPacket **pPacket) {
    if (pPacket) {
        av_packet_free(pPacket);
        *pPacket = nullptr;
    }
}

void BaseChannel::releaseAVFrame(AVFrame **pFrame) {
    if (pFrame) {
        av_frame_free(pFrame);
        *pFrame = nullptr;
    }
}

BaseChannel::BaseChannel(int id, AVCodecContext *codecContext,AVRational time_base,JavaCallHelper *javaCallHelper) {
    this->id = id;
    this->avCodecContext=codecContext;
    this->time_base=time_base;
    this->javaCallHelper=javaCallHelper;
    pthread_mutex_init(&mutex, nullptr);
    pthread_cond_init(&cond,nullptr);
    packets.setReleaseCallBack(releaseAVPacket);
    frames.setReleaseCallBack(releaseAVFrame);
}


