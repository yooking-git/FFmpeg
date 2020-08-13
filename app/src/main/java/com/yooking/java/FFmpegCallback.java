package com.yooking.java;

/**
 * Created by yooking on 2020/8/12.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
public interface FFmpegCallback {
    void onBegin();
    default void onProgress(int progress, int duration){}
    void onEnd(int resultCode, String resultMsg);
}
