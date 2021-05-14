#include <jni.h>
#include <string>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}


extern "C"
JNIEXPORT jstring

Java_com_xiaojian_lkplayer_LkPlayer_native_1startPlay(JNIEnv
                                                      *env,
                                                      jobject thiz, jstring
                                                      url_) {

    const char *path = env->GetStringUTFChars(url_, 0);
//总上下文
    avformat_network_init();
    AVFormatContext *formatContext = avformat_alloc_context();
    AVDictionary *options = NULL;
    av_dict_set(&options, "timeout", "300000", 0);

    int ret = avformat_open_input(&formatContext, path, NULL, &options);
    if (ret) {
        return nullptr;
    }
    //获取流
    avformat_find_stream_info(formatContext, &options);
    int video_steam_idx = -1;//视频流id
    for (int i = 0; i < formatContext->nb_streams; ++i) {
        if (formatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_steam_idx = i;
            break;
        }
    }
    //解码器
    AVCodecParameters *codecpar = formatContext->streams[video_steam_idx]->codecpar;
    const AVCodec * avCodec=avcodec_find_decoder(codecpar->codec_id);
    AVCodecContext *avCodecContext= avcodec_alloc_context3(avCodec);
    avcodec_parameters_to_context(avCodecContext,codecpar);
    avcodec_open2(avCodecContext,avCodec,NULL);
    //解码yuv数据
    AVPacket *avPacket= av_packet_alloc();
    //从视频流中获取数据包
    av_read_frame(formatContext,avPacket);


}