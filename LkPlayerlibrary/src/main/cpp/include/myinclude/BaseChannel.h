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

class BaseChannel {
public:
     BaseChannel(int id);

    virtual ~BaseChannel();

    static void releaseAVPacket(AVPacket **);

    static void releaseAVFrame(AVFrame **);

    SafeQueue<AVFrame *> frames;
    SafeQueue<AVPacket *> packets;
    int id;
    virtual void start()=0;
    virtual void stop()=0;
};


#endif //LKPLAYER_BASECHANNEL_H
