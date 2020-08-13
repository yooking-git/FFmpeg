//
// Created by frank on 2019/11/9.
//

#ifndef FFMPEGANDROID_FFMPEG_JNI_DEFINE_H
#define FFMPEGANDROID_FFMPEG_JNI_DEFINE_H

#include <android/log.h>

#define FFMPEG_FUNC(RETURN_TYPE, FUNC_NAME, ...) \
    JNIEXPORT RETURN_TYPE JNICALL Java_com_yooking_java_FFmpegCmd_ ## FUNC_NAME \
    (JNIEnv *env, jobject thiz, ##__VA_ARGS__)\

#endif //FFMPEGANDROID_FFMPEG_JNI_DEFINE_H
