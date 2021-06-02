//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_BASECHANNEL_H
#define LKPLAYER_BASECHANNEL_H


#include "safe_queue.h"
#include "JavaCallHelper.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavcodec/avcodec.h>
#include <libavutil/time.h>
#include <libavcodec/packet.h>
#include <libavutil/frame.h>
}

class BaseChannel {
public:
    BaseChannel(int id, AVCodecContext *codecContext, AVRational time_base,
                JavaCallHelper *javaCallHelper);

    virtual ~BaseChannel();

    static void releaseAVPacket(AVPacket **);

    static void releaseAVFrame(AVFrame **);

    SafeQueue<AVFrame *> frames;
    SafeQueue<AVPacket *> packets;
    int id;

    virtual void start() = 0;

    virtual int stop() = 0;

    bool isPlaying = false;
    AVCodecContext *avCodecContext = nullptr;
    AVRational time_base;
    double audio_time;
    JavaCallHelper *javaCallHelper = 0;
    bool isPause= false;// true 暂停 false 播放
    pthread_mutex_t mutex;
    pthread_cond_t cond;

};


#endif //LKPLAYER_BASECHANNEL_H
