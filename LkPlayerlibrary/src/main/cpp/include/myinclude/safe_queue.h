//
// Created by Administrator on 2021/5/21.
//

#ifndef LKPLAYER_SAFE_QUEUE_H
#define LKPLAYER_SAFE_QUEUE_H

#include <queue>
#include <pthread.h>

using namespace std;

template<typename T>
class SafeQueue {
    typedef void (*ReleaseCallBack)(T *);

    typedef void (*SyncOpt )(queue<T> &);

public:
    SafeQueue() {
        pthread_mutex_init(&mutex, nullptr);
        pthread_cond_init(&cond, nullptr);
    };

    ~SafeQueue() {
        pthread_mutex_destroy(&mutex);
        pthread_cond_destroy(&cond);
    };

/**
 * 入队
 * @param value
 */
    void push(T value) {
        pthread_mutex_lock(&mutex);
        if (work) {
            q.push(value);
            pthread_cond_signal(&cond);
        } else {
            //非工作状态
            if (releaseCallBack) {
                releaseCallBack(&value);
            }

        }
        pthread_mutex_unlock(&mutex);
    }

/**
 * 出队
 * @param value
 */
    int pop(T &value) {
        int ret = 0;
        pthread_mutex_lock(&mutex);

        while (work && q.empty()) {
            //工作状态，说明确实需要pop,但是队列为空，需要等待
            pthread_cond_wait(&cond, &mutex);
        }
        if (!q.empty()) {
            value = q.front();
            //弹出
            q.pop();
            ret = 1;
        }
        pthread_mutex_unlock(&mutex);
        return ret;


    }

/**
 *  设置队列工作状态
 * @param work
 */
    void setWork(int work) {
        pthread_mutex_lock(&mutex);
        this->work = work;
        pthread_cond_signal(&cond);
        pthread_mutex_unlock(&mutex);

    }

/**
 * 判断队列是否为空
 * @return
 */
    int empty() {
        return q.empty();
    }

/**
 * 队列的大小
 * @return
 */
    int size() {
        return q.size();
    }

/**
 * 清空队列
 */
    void clear() {
        pthread_mutex_lock(&mutex);
        unsigned int size = q.size();
        for (int i = 0; i < size; ++i) {
            T value = q.front();
            if (releaseCallBack) {
                releaseCallBack(&value);
            }
            q.pop();
        }
        pthread_mutex_unlock(&mutex);
    }

    void setReleaseCallBack(ReleaseCallBack releaseCallBack) {
        this->releaseCallBack = releaseCallBack;
    }

    void setSyncOpt(SyncOpt syncOpt) {
        this->syncOpt = syncOpt;
    }

    /**
     * 同步操作
     */
    void sync() {
        pthread_mutex_lock(&mutex);
        syncOpt(q);
        pthread_mutex_unlock(&mutex);
    }


private:
    queue<T> q;
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    int work;//标记队列是否工作
    ReleaseCallBack releaseCallBack;
    SyncOpt syncOpt;

};


#endif //LKPLAYER_SAFE_QUEUE_H
