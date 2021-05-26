//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_BASECHANNEL_H
#define LKPLAYER_BASECHANNEL_H
extern "C" {
#include <libavcodec/packet.h>
#include <libavutil/frame.h>
};


#include "safe_queue.h"
extern "C"{
#include <libavcodec/avcodec.h>
#include <libavcodec/avcodec.h>
#include <libavutil//time.h>
}

class BaseChannel {
public:
     BaseChannel(int id, AVCodecContext *codecContext,AVRational time_base);

    virtual ~BaseChannel();

    static void releaseAVPacket(AVPacket **);

    static void releaseAVFrame(AVFrame **);

    SafeQueue<AVFrame *> frames;
    SafeQueue<AVPacket *> packets;
    int id;
    virtual void start()=0;
    virtual void stop()=0;
    bool isPlaying= false;
    AVCodecContext *avCodecContext= nullptr;
    AVRational time_base;
    double audio_time;

};


#endif //LKPLAYER_BASECHANNEL_H
