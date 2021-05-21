//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_AUDIOCHANNEL_H
#define LKPLAYER_AUDIOCHANNEL_H


#include "BaseChannel.h"

class AudioChannel: public BaseChannel {
public:
    AudioChannel(int id);
    virtual ~AudioChannel();
    void start();
    void stop();

};


#endif //LKPLAYER_AUDIOCHANNEL_H
