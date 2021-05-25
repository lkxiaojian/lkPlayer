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
    jmd_prepared = jniEnv->GetMethodID(clazz, "onPrepared", "()V");
    jmd_error=jniEnv->GetMethodID(clazz,"onPlayError","(I)V");



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



void JavaCallHelper::onError(int thread,int errCode) {
    if (thread == THREAD_MAIN) {
        jniEnv->CallVoidMethod(instance, jmd_error,errCode);
    } else {
        JNIEnv *env_child = nullptr;
        javaVm->AttachCurrentThread(&env_child, nullptr);
        env_child->CallVoidMethod(instance, jmd_error,errCode);
        javaVm->DetachCurrentThread();
    }
}




