//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_VIDEOCHANNEL_H
#define LKPLAYER_VIDEOCHANNEL_H


#include "myinclude/BaseChannel.h"
#include "AudioChannel.h"

extern "C" {
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
}

typedef void (*RenderCallBack) (uint8_t *, int, int, int);
class VideoChannel: public BaseChannel {

public:
    VideoChannel(int id, AVCodecContext *avCodecContext,int fps,AVRational time_base,JavaCallHelper *javaCallHelper);
    virtual ~VideoChannel();
    void start();
    void stop();
    void start_decode();
    void start_play();

    void setRenderCallBack(RenderCallBack renderCallBack);
    void setAudioChannel( AudioChannel *audioChannel);
    void setPauseOrResume(bool flag);
private:
    pthread_t  pid_video_decode;
    pthread_t  pid_video_play;
    RenderCallBack renderCallBack;
    int fps;
    AudioChannel *audioChannel;


};


#endif //LKPLAYER_VIDEOCHANNEL_H
