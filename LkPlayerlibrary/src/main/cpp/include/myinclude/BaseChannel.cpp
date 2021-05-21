//
// Created by root on 2021/5/17.
//

#include "BaseChannel.h"
#include "safe_queue.h"


BaseChannel::~BaseChannel() {
    packets.clear();
    frames.clear();

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

BaseChannel::BaseChannel(int id) {
    this->id = id;
    packets.setReleaseCallBack(releaseAVPacket);
    frames.setReleaseCallBack(releaseAVFrame);
}


