//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_VIDEOCHANNEL_H
#define LKPLAYER_VIDEOCHANNEL_H


#include "myinclude/BaseChannel.h"

typedef void (*RenderCallBack) (uint8_t *, int, int, int);
class VideoChannel: public BaseChannel {

public:
    VideoChannel(int id, AVCodecContext *avCodecContext);
    virtual ~VideoChannel();
    void start();
    void stop();
    void start_decode();
    void start_play();

    void setRenderCallBack(RenderCallBack renderCallBack);

private:
    pthread_t  pid_video_decode;
    pthread_t  pid_video_play;
    RenderCallBack renderCallBack;
};


#endif //LKPLAYER_VIDEOCHANNEL_H
