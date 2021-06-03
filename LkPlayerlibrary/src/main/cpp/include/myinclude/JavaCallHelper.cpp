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
    jmd_error = jniEnv->GetMethodID(clazz, "onPlayError", "(I)V");
    jmd_progress = jniEnv->GetMethodID(clazz, "setProgress", "(I)V");
    jmd_complete=jniEnv->GetMethodID(clazz,"onComplete","()V");

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


/**
 *  回调error
 * @param thread
 * @param errCode
 */
void JavaCallHelper::onError(int thread, int errCode) {
    if (thread == THREAD_MAIN) {
        jniEnv->CallVoidMethod(instance, jmd_error, errCode);
    } else {
        JNIEnv *env_child = nullptr;
        javaVm->AttachCurrentThread(&env_child, nullptr);
        env_child->CallVoidMethod(instance, jmd_error, errCode);
        javaVm->DetachCurrentThread();
    }
}

/**
 *  回调进度
 * @param thread
 * @param progress
 */
void JavaCallHelper::onProgress(int thread, int progress) {
    if (thread == THREAD_MAIN) {
        jniEnv->CallVoidMethod(instance, jmd_progress, progress);
    } else {
        JNIEnv *env_child = nullptr;
        javaVm->AttachCurrentThread(&env_child, nullptr);
        env_child->CallVoidMethod(instance, jmd_progress, progress);
        javaVm->DetachCurrentThread();
    }

}



void JavaCallHelper::onComplete(int thread) {
    if (thread == THREAD_MAIN) {
        jniEnv->CallVoidMethod(instance, jmd_complete);
    } else {
        JNIEnv *env_child = nullptr;
        javaVm->AttachCurrentThread(&env_child, nullptr);
        env_child->CallVoidMethod(instance, jmd_complete);
        javaVm->DetachCurrentThread();
    }
}





