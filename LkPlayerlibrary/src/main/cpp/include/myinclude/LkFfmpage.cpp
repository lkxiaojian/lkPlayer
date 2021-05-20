//
// Created by root on 2021/5/17.
//




#include "LkFfmpage.h"


LkFfmpage::LkFfmpage(JavaCallHelper *javaCallHelper, char *dataSource) {
    this->javaCallHelper = javaCallHelper;
    this->dataSource = new char[strlen(dataSource) + 1];
    strcpy(this->dataSource, dataSource);

}

LkFfmpage::~LkFfmpage() {
    DELETE(dataSource)
    DELETE(javaCallHelper)
}

/**
 *  准备线程的真正执行的函数
 * @return
 */
void *task_prepare(void *args) {

    LkFfmpage *ffmpage = static_cast<LkFfmpage *>(args);
    ffmpage->_prepare();
    return 0;
}

/**
 * 播放准备
 */
void LkFfmpage::prepare() {
    //文件 io
    //直播 网络
    //创建主线程
    pthread_create(&pid_prepare, nullptr, task_prepare, nullptr);
}

void LkFfmpage::_prepare() {
    //打开输入
    AVFormatContext *avFormatContext = avformat_alloc_context();
    AVDictionary *options = nullptr;
    av_dict_set(&options, "timeout", "1000000", 0);
    int ret = avformat_open_input(&avFormatContext, dataSource, 0, &options);
    av_dict_free(&options);
    if (ret) {
     //失败
        LOGE("打开媒体失败: %s",av_err2str(ret));
    }

}

