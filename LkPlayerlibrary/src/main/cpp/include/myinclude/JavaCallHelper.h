//
// Created by root on 2021/5/17.
//

#ifndef LKPLAYER_JAVACALLHELPER_H
#define LKPLAYER_JAVACALLHELPER_H


#include <jni.h>

class JavaCallHelper {
public:
    JavaCallHelper(JNIEnv *jniEnv_, jobject instance_, JavaVM *javaVm_);

    ~JavaCallHelper();

    void onPrepared(int i);

    void onError(int Thread,int errCode);

private:
    JNIEnv *jniEnv;
    jobject instance;
    JavaVM *javaVm;
    jmethodID jmd_prepared;
    jmethodID jmd_error;
};


#endif //LKPLAYER_JAVACALLHELPER_H
