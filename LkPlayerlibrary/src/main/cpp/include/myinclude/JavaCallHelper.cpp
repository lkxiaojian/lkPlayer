//
// Created by root on 2021/5/17.
//

#include "JavaCallHelper.h"
#include "Macro.h"


JavaCallHelper::JavaCallHelper(JNIEnv *jniEnv_, jobject instance_, JavaVM *javaVm_) {

    this->javaVm = javaVm_;
    this->jniEnv = jniEnv_;
    this->instance = jniEnv->NewGlobalRef(instance_);
    jclass clazz = jniEnv->GetObjectClass(instance);
    jmd_error=jniEnv->GetMethodID(clazz,"onPlayError","(Ljava/lang/String;I)V");
    jmd_prepared = jniEnv->GetMethodID(clazz, "onPrepared", "()V");


}


JavaCallHelper::~JavaCallHelper() {
    javaVm = nullptr;
    jniEnv->DeleteGlobalRef(instance);
    instance = nullptr;
}

void JavaCallHelper::onPrepared(int thread) {
    if (thread == THREAD_MAIN) {
        jniEnv->CallVoidMethod(instance, jmd_prepared);
    } else {
        JNIEnv *env_child = nullptr;
        javaVm->AttachCurrentThread(&env_child, nullptr);
        env_child->CallVoidMethod(instance, jmd_prepared);
        javaVm->DetachCurrentThread();
    }


}



void JavaCallHelper::onError(int thread, char *message,int errCode) {
    if (thread == THREAD_MAIN) {
        jniEnv->CallVoidMethod(instance, jmd_error,&message,errCode);
    } else {
        JNIEnv *env_child = nullptr;
        javaVm->AttachCurrentThread(&env_child, nullptr);
        env_child->CallVoidMethod(instance, jmd_error,&message,errCode);
        javaVm->DetachCurrentThread();
    }
}




