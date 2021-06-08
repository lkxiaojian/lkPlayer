//
// Created by root on 2021/5/17.
//

#include "AudioChannel.h"
#include "Macro.h"


AudioChannel::AudioChannel(int id, AVCodecContext *avCodecContext, AVRational time_base,
                           JavaCallHelper *javaCallHelper)
        : BaseChannel(id,
                      avCodecContext, time_base, javaCallHelper) {
    out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
    out_sampleSize = av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);
    out_sampleRate = 44100;
    //通道数* 采样率*2（16bit=2字节）
    out_buffers_size = out_channels * out_sampleRate * out_sampleSize;
    out_buffers = static_cast<uint8_t *>(malloc(out_buffers_size));
    memset(out_buffers, 0, out_buffers_size);


    swrContext = swr_alloc_set_opts(nullptr, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16,
                                    out_sampleRate, avCodecContext->channel_layout,
                                    avCodecContext->sample_fmt,
                                    avCodecContext->sample_rate,
                                    0, nullptr);
    //初始化重采样上下文
    swr_init(swrContext);

}

AudioChannel::~AudioChannel() {
    if (swrContext) {
        swr_free(&swrContext);
        swrContext = nullptr;
    }
    DELETE(out_buffers)
}

void *task_audio_decode(void *args) {
    auto *audioChannel = static_cast<AudioChannel *>(args);

    audioChannel->start_audio_decode();
    return nullptr;
}

void *task_audio_play(void *args) {
    auto *audioChannel = static_cast<AudioChannel *>(args);
    audioChannel->start_audio_play();
    return nullptr;
}

void AudioChannel::start() {
    isPlaying = true;
    packets.setWork(1);
    frames.setWork(1);
//    pthread_create(&pid_audio_decode, nullptr, task_audio_decode, this);
    pthread_create(&pid_audio_play, nullptr, task_audio_play, this);
}

int AudioChannel::stop() {
    isPlaying = false;
    javaCallHelper = nullptr;
    packets.setWork(0);
    frames.setWork(0);
    pthread_join(pid_audio_decode, nullptr);
    pthread_join(pid_audio_play, nullptr);

    if (swrContext) {
        swr_free(&swrContext);
        swrContext = 0;
    }
    /**
    * 7、释放
    */
    //7.1 设置播放器状态为停止状态
    if (bqPlayerPlay != nullptr) {
        (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
    }
    //7.2 销毁播放器
    if (bqPlayerObject != nullptr) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = nullptr;
        bqPlayerBufferQueue = nullptr;
    }
    //7.3 销毁混音器
    if (outputMixObject != nullptr) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = nullptr;
    }
    //7.4 销毁引擎
    if (engineObject != nullptr) {
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
        engineInterface = nullptr;
    }
    return 0;
}

/**
 * 音频解码
 */
void AudioChannel::start_audio_decode() {
    AVPacket *packet = nullptr;
    while (isPlaying) {
        int ret = packets.pop(packet);
        if (!isPlaying) {
            //如果停止播放了，跳出循环 释放packet
            break;
        }
        if (!ret) {
            //取数据包失败
            continue;
        }

        //拿到了视频数据包（编码压缩了的），需要把数据包给解码器进行解码
        ret = avcodec_send_packet(avCodecContext, packet);
        if (ret) {
            //往解码器发送数据包失败
            break;
        }
        releaseAVPacket(&packet);
        AVFrame *avFrame = av_frame_alloc();
        ret = avcodec_receive_frame(avCodecContext, avFrame);
//        LOGE("av_err2str--->%s", av_err2str(ret));
        if (ret == AVERROR(EAGAIN)) {
            //重来
            continue;
        } else if (ret != 0) {
            break;
        }
        //ret=0 数据收发正常。成功获取视频原始数据包
        while (isPlaying && frames.size() > 100) {
            av_usleep(10 * 1000);
        }
        frames.push(avFrame);
    }
    releaseAVPacket(&packet);
}

//4.3 创建回调函数
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {


    auto *audioChannel = static_cast<AudioChannel *>(context);
    if (audioChannel->isPlaying) {
        int pcm_size = audioChannel->getPCM();
        if (pcm_size > 0 && audioChannel->isPlaying) {
            (*bq)->Enqueue(bq, audioChannel->out_buffers, pcm_size);
        }
    }
}

/**
 * 音频播放
 */
void AudioChannel::start_audio_play() {
    if (!isPlaying) {
        return;
    }
    /**
    * 1、创建引擎并获取引擎接口
    */
    SLresult result;
    // 1.1 创建引擎对象：SLObjectItf engineObject
    result = slCreateEngine(&engineObject, 0, nullptr, 0, nullptr, nullptr);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    // 1.2 初始化引擎
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    // 1.3 获取引擎接口 SLEngineItf engineInterface
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineInterface);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    /**
     * 2、设置混音器
     */
    // 2.1 创建混音器：SLObjectItf outputMixObject
    result = (*engineInterface)->CreateOutputMix(engineInterface, &outputMixObject, 0,
                                                 nullptr, nullptr);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    // 2.2 初始化混音器
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    /**
     * 3、创建播放器
     */
    //3.1 配置输入声音信息
    //创建buffer缓冲类型的队列 2个队列
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                       2};
    //pcm数据格式
    //SL_DATAFORMAT_PCM：数据格式为pcm格式
    //2：双声道
    //SL_SAMPLINGRATE_44_1：采样率为44100
    //SL_PCMSAMPLEFORMAT_FIXED_16：采样格式为16bit
    //SL_PCMSAMPLEFORMAT_FIXED_16：数据大小为16bit
    //SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT：左右声道（双声道）
    //SL_BYTEORDER_LITTLEENDIAN：小端模式
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
                                   SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                                   SL_BYTEORDER_LITTLEENDIAN};

    //数据源 将上述配置信息放到这个数据源中
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};

    //3.2 配置音轨（输出）
    //设置混音器
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, nullptr};
    //需要的接口 操作队列的接口
    const SLInterfaceID ids[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    //3.3 创建播放器
    result = (*engineInterface)->CreateAudioPlayer(engineInterface, &bqPlayerObject, &audioSrc,
                                                   &audioSnk, 1, ids, req);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    //3.4 初始化播放器：SLObjectItf bqPlayerObject
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    //3.5 获取播放器接口：SLPlayItf bqPlayerPlay
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    /**
     * 4、设置播放回调函数
     */
    //4.1 获取播放器队列接口：SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE, &bqPlayerBufferQueue);

    //4.2 设置回调 void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
    (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, this);

    /**
     * 5、设置播放器状态为播放状态
     */
    (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);

    /**
     * 6、手动激活回调函数
     */
    bqPlayerCallback(bqPlayerBufferQueue, this);


}

int AudioChannel::getPCM() {
//    pthread_mutex_lock(&mutex);

    int pcm_data_size = 0;
    AVFrame *frame = nullptr;
    while (isPlaying) {
        if (isPause) {
            pthread_cond_wait(&cond, &mutex);
        }
        int ret = frames.pop(frame);
        if (!isPlaying) {
            //如果停止播放了，跳出循环 释放packet
            break;
        }
        if (!ret) {
            //取数据包失败
            continue;
        }
//        LOGE("音频播放中");
        //pcm数据在 frame中
        //这里获得的解码后pcm格式的音频原始数据，有可能与创建的播放器中设置的pcm格式不一样
        //重采样？example:resample

        //假设输入10个数据，有可能这次转换只转换了8个，还剩2个数据（delay）
        //断点：1024 * 48000

        //swr_get_delay: 下一个输入数据与下下个输入数据之间的时间间隔
        int64_t delay = swr_get_delay(swrContext, frame->sample_rate);

        //a * b / c
        //AV_ROUND_UP：向上取整
        int64_t out_max_samples = av_rescale_rnd(frame->nb_samples + delay, frame->sample_rate,
                                                 out_sampleRate,
                                                 AV_ROUND_UP);

        //上下文
        //输出缓冲区
        //输出缓冲区能容纳的最大数据量
        //输入数据
        //输入数据量
        int out_samples = swr_convert(swrContext, &out_buffers, out_max_samples,
                                      (const uint8_t **) (frame->data), frame->nb_samples);

        // 获取swr_convert转换后 out_samples个 *2 （16位）*2（双声道）
        pcm_data_size = out_samples * out_sampleSize * out_channels;
        //获取音频时间
        audio_time = frame->best_effort_timestamp * av_q2d(time_base);
        if (javaCallHelper && isPlaying) {
            javaCallHelper->onProgress(THREAD_CHILD, audio_time);
        }
        break;

    }//end while
//    pthread_mutex_unlock(&mutex);
    releaseAVFrame(&frame);
    return pcm_data_size;

}

void AudioChannel::setPauseOrResume(bool flag) {
    this->isPause = flag;
    if (!isPause) {
        pthread_cond_signal(&cond);
    }

}




