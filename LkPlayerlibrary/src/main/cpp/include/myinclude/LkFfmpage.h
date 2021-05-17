//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_LKFFMPAGE_H
#define LKPLAYER_LKFFMPAGE_H


#include "JavaCallHelper.h"
#include "VideoChannel.h"
#include "AudioChannel.h"

class LkFfmpage {
public:
    LkFfmpage(JavaCallHelper *javaCallHelper, char *Path);

    ~LkFfmpage();

    void prepare();

private:
    JavaCallHelper *javaCallHelper = 0;
    AudioChannel *audioChannel = 0;
    VideoChannel *videoChannel = 0;
    char *dataSource;

};


#endif //LKPLAYER_LKFFMPAGE_H
