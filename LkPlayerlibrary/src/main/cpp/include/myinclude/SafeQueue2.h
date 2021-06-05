////
//// Created by lhc on 21-6-5.
////
//
//#ifndef LKPLAYER_SAFEQUEUE2_H
//#define LKPLAYER_SAFEQUEUE2_H
//
//#include <queue>
//#include <pthread.h>
//
//
//using namespace std;
//
//
//template<typename T>
//class SafeQueue2 {
//    typedef void (*ReleaseCallBack)(T *);
//
//public:
//    SafeQueue2() {
//        pthread_mutex_init(&mutex, 0);
//        pthread_cond_init(&cond,0);
//    };
//
//    ~SafeQueue2() {
//        pthread_mutex_destroy(&mutex);
//        pthread_cond_destroy(&cond);
//    };
//
//
///**
// * 入队
// * @param value
// */
//    void push(T value) {
//        pthread_mutex_lock(&mutex);
//        q.push(value)
//        pthread_mutex_unlock(&mutex);
//    }
//
///**
// * 出队
// * @param value
// */
//    int pop(T &value) {
//        int ret = 0;
//        pthread_mutex_lock(&mutex);
//        pthread_cond_wait(&cond, &mutex);
//        if (!q.empty()) {
//            value = q.front();
//            q.pop();
//            ret = 1;
//        }
//        pthread_mutex_unlock(&mutex);
//        return ret;
//    }
//
//
///**
// * 判断队列是否为空
// * @return
// */
//    int empty() {
//        return q.empty();
//    }
//
///**
// * 队列的大小
// * @return
// */
//    int size() {
//        return q.size();
//    }
//
//    void clear(ReleaseCallBack func) {
//        pthread_mutex_lock(&mutex);
//        while (!q.empty()){
//            T value = q.front();
//            func(&value);
//            q.pop();
//        }
//        pthread_mutex_unlock(&mutex);
//    }
//
//private:
//    queue<T> q;
//    pthread_mutex_t mutex;
//    pthread_cond_t cond;
//
//};
//#endif //LKPLAYER_SAFEQUEUE2_H
