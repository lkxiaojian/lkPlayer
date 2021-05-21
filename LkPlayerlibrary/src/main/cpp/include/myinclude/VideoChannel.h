//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_VIDEOCHANNEL_H
#define LKPLAYER_VIDEOCHANNEL_H


#include "myinclude/BaseChannel.h"

class VideoChannel: public BaseChannel {
public:
    VideoChannel(int id);
    virtual ~VideoChannel();
    void start();
    void stop();

};


#endif //LKPLAYER_VIDEOCHANNEL_H
