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

JavaVM *javaVm = nullptr;
ANativeWindow *window = nullptr;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
LkFfmpage *lkFfmpage = nullptr;
static const char *mClassName = "com/lkxiaojian/lkplayerlibrary/LkPlayer";


void renderFrame(uint8_t *src_data, int src_lineSize, int width, int height) {
    pthread_mutex_lock(&mutex);
    if (!window) {
        ANativeWindow_release(window);
        return;
    }

    ANativeWindow_setBuffersGeometry(window, width, height, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer window_buffer;
    if (ANativeWindow_lock(window, &window_buffer, nullptr)) {
        ANativeWindow_release(window);
        window = nullptr;
        pthread_mutex_unlock(&mutex);
        return;
    }
    //把buffer中的数据进行赋值（修改）
    auto *dst_data = static_cast<uint8_t *>(window_buffer.bits);
    int dst_lineSize = window_buffer.stride * 4;//ARGB
    //逐行拷贝
    for (int i = 0; i < window_buffer.height; ++i) {
        memcpy(dst_data + i * dst_lineSize, src_data + i * src_lineSize, dst_lineSize);
    }
    ANativeWindow_unlockAndPost(window);
    pthread_mutex_unlock(&mutex);
}


jstring nativePrepare(JNIEnv *env, jobject thiz, jstring url) {
    const char *path = env->GetStringUTFChars(url, nullptr);
    auto *javaCallHelper = new JavaCallHelper(env, thiz, javaVm);
    lkFfmpage = new LkFfmpage(javaCallHelper, const_cast<char *>(path));
    lkFfmpage->setRenderCallBack(renderFrame);
    lkFfmpage->prepare();
    return nullptr;
}


extern "C"
JNIEXPORT jstring JNICALL
nativeStart(JNIEnv *env, jobject thiz) {
    //开始播放
    if (lkFfmpage) {
        lkFfmpage->start();
    }
    return nullptr;
}
extern "C"
JNIEXPORT jstring JNICALL
setSurfaceNative(JNIEnv *env, jobject thiz, jobject surface) {
    pthread_mutex_lock(&mutex);
    if (window) {
        ANativeWindow_release(window);
        window = nullptr;
    }
//创建新的窗口用于视频显示
    window = ANativeWindow_fromSurface(env, surface);
    pthread_mutex_unlock(&mutex);
    return nullptr;

}

static const JNINativeMethod mMethods[] = {
        {"nativePrepare",    "(Ljava/lang/String;)Ljava/lang/String;",     (jstring *) nativePrepare},
        {"setSurfaceNative", "(Landroid/view/Surface;)Ljava/lang/String;", (jstring *) setSurfaceNative},
        {"nativeStart",      "()Ljava/lang/String;",                       (jstring *) nativeStart}
};


jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVm = vm;
    JNIEnv *env = nullptr;
    int ret = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (ret != JNI_OK) {
        return -1;
    }

    jclass nativeClass = env->FindClass(mClassName);
    ret = env->RegisterNatives(nativeClass, mMethods, 3);
    if (ret != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}