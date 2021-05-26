//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_AUDIOCHANNEL_H
#define LKPLAYER_AUDIOCHANNEL_H


#include "BaseChannel.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
extern "C"{
#include <libswresample/swresample.h>
};

class AudioChannel: public BaseChannel {
public:
    AudioChannel(int id, AVCodecContext *avCodecContext,AVRational time_base);
    virtual ~AudioChannel();
    void start();
    void stop();

    void start_audio_decode();

    void start_audio_play();

    int getPCM();

    uint8_t *out_buffers = 0;
    int out_channels;
    int out_sampleSize;
    int out_sampleRate;
    int out_buffers_size;



private:
    pthread_t  pid_audio_decode;
    pthread_t  pid_audio_play;

    //引擎
    SLObjectItf engineObject = 0;
    //引擎接口
    SLEngineItf engineInterface = 0;
    //混音器
    SLObjectItf outputMixObject = 0;
    //播放器
    SLObjectItf bqPlayerObject = 0;
    //播放器接口
    SLPlayItf bqPlayerPlay =0;
    //播放器队列接口
    SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue = 0;
    SwrContext *swrContext=0;

};


#endif //LKPLAYER_AUDIOCHANNEL_H
