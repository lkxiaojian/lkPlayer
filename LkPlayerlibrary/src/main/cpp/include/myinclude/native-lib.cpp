#include <jni.h>
#include <string>
#include <android/native_window_jni.h>
#include <zconf.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>

}
#include "myinclude/JavaCallHelper.h"
#include "myinclude/LkFfmpage.h"
JavaVM *javaVm= nullptr;
jint JNI_OnLoad(JavaVM *vm ,void * reserved){
    javaVm=vm;
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lkxiaojian_lkplayerlibrary_LkPlayer_native_1prepare(JNIEnv *env, jobject thiz,
                                                             jstring url) {
    const char *path = env->GetStringUTFChars(url, nullptr);
    auto *javaCallHelper=new JavaCallHelper(env,thiz,javaVm);
    auto *lkFfmpage=new LkFfmpage(javaCallHelper,const_cast<char *>(path));
    lkFfmpage->prepare();
    return nullptr;
}



extern "C"
JNIEXPORT jstring JNICALL
Java_com_lkxiaojian_lkplayerlibrary_LkPlayer_native_1startPlay(JNIEnv *env, jobject thiz,
                                                               jstring url_, jobject surface) {

    const char *path = env->GetStringUTFChars(url_, nullptr);
//总上下文
    avformat_network_init();
    AVFormatContext *formatContext = avformat_alloc_context();
    ANativeWindow *aNativeWindow = ANativeWindow_fromSurface(env, surface);
    AVDictionary *options = nullptr;
    av_dict_set(&options, "timeout", "300000", 0);

    int ret = avformat_open_input(&formatContext, path, nullptr, &options);
    if (ret) {
        return nullptr;
    }
    //获取流
    avformat_find_stream_info(formatContext, nullptr);
    int video_steam_idx = -1;//视频流id
    for (int i = 0; i < formatContext->nb_streams; ++i) {
        if (formatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_steam_idx = i;
            break;
        }
    }
    //解码器
    AVCodecParameters *codecpar = formatContext->streams[video_steam_idx]->codecpar;
    const AVCodec *avCodec = avcodec_find_decoder(codecpar->codec_id);
    AVCodecContext *avCodecContext = avcodec_alloc_context3(avCodec);
    avcodec_parameters_to_context(avCodecContext, codecpar);
    avcodec_open2(avCodecContext, avCodec, nullptr);
    //解码yuv数据
    AVPacket *avPacket = av_packet_alloc();
    //从视频流中获取数据包
    SwsContext *swsContext = sws_getContext(avCodecContext->width, avCodecContext->height,
                                            avCodecContext->pix_fmt,
                                            avCodecContext->width, avCodecContext->height,
                                            AV_PIX_FMT_RGBA, SWS_BILINEAR, nullptr, nullptr,
                                            nullptr);

    ANativeWindow_setBuffersGeometry(aNativeWindow, avCodecContext->width, avCodecContext->height,
                                     WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer outBuffer;
    while (av_read_frame(formatContext, avPacket) >= 0) {
        avcodec_send_packet(avCodecContext, avPacket);
        AVFrame *avFrame = av_frame_alloc();
        ret = avcodec_receive_frame(avCodecContext, avFrame);
        if (ret == AVERROR(EAGAIN)) {
            continue;
        } else if (ret < 0) {
            break;
        }

        //接受的容器
        uint8_t *det_data[4];
        //每一行的首地址
        int det_linesize[8];
        av_image_alloc(det_data, det_linesize, avCodecContext->width, avCodecContext->height,
                       AV_PIX_FMT_RGBA, 1);
        //绘制
        sws_scale(swsContext, avFrame->data, avFrame->linesize, 0, avFrame->height, det_data,
                  det_linesize);
        if (avPacket->stream_index == video_steam_idx) {
            //非零   正在解码
            if (ret == 0) {

                ANativeWindow_lock(aNativeWindow, &outBuffer, nullptr);

                //渲染
                auto *firstWindow = static_cast<uint8_t *>(outBuffer.bits);
                //输入源
                uint8_t *src_data = det_data[0];
                //每一行有多少字节rgba
                int destStride = outBuffer.stride * 4;
                int src_linesize = det_linesize[0];
                for (int i = 0; i < outBuffer.height; ++i) {
                    memcpy(firstWindow + i * destStride, src_data + i * src_linesize, destStride);
                }
                ANativeWindow_unlockAndPost(aNativeWindow);
                usleep(1000 * 16);
                av_frame_free(&avFrame);
            }
        }
    }

    ANativeWindow_release(aNativeWindow);
    avcodec_close(avCodecContext);
    avformat_free_context(formatContext);

    return nullptr;
}