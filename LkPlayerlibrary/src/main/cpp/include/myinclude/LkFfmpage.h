//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_LKFFMPAGE_H
#define LKPLAYER_LKFFMPAGE_H

#include <cstring>
#include <pthread.h>
#include "JavaCallHelper.h"
#include "VideoChannel.h"
#include "myinclude/AudioChannel.h"
#include "Macro.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/error.h>
};


class LkFfmpage {
public:
    LkFfmpage(JavaCallHelper *javaCallHelper, char *Path);

    ~LkFfmpage();

    void prepare();

    void _prepare();

    void start();

    void _start();

    void setRenderCallBack(RenderCallBack renderCallBack);

    void stop();
    void _stop(LkFfmpage *lkFfmpage);
    int getDuration() const;

    jint setSeekToProgress(jint i);

private:
    JavaCallHelper *javaCallHelper = 0;
    AudioChannel *audioChannel = 0;
    VideoChannel *videoChannel = 0;
    char *dataSource;
    pthread_t pid_prepare;
    pthread_t pid_start;
    pthread_t pid_sop;
    pthread_mutex_t  seekMutex;
    bool isPlaying;
    AVFormatContext *avFormatContext= nullptr;
    RenderCallBack renderCallBack;
    int duration=0;


};


#endif //LKPLAYER_LKFFMPAGE_H
