package com.yooking.java;

import android.annotation.SuppressLint;

import androidx.annotation.IntDef;

import com.yooking.utils.L;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by yooking on 2020/8/12.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
public class FFmpegCmd {

    static {
        System.loadLibrary("media-handle");
    }

    private static final int STATE_INIT = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_FINISH = 2;
    private static final int STATE_ERROR = 3;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_INIT, STATE_RUNNING, STATE_FINISH, STATE_ERROR})
    public @interface FFmpegState {
    }

    private native static int handle(String[] commands);

    public static void onProgressCallback(int position, int duration, @FFmpegState int state) {
        L.INSTANCE.i("onProgress position=" + position
                + "--duration=" + duration + "--state=" + state);
    }

    /**
     * Execute FFmpeg multi commands
     *
     * @param commands the String array of command
     */
    public static void execute(final List<String[]> commands,FFmpegCallback callback) {
        new Thread(() -> {
            //call JNI interface to execute FFmpeg cmd
            callback.onBegin();
            int result = 0;
            int count = 0;
            L.INSTANCE.i(commands.toString());
            for (String[] command : commands) {
                result = handle(command);
                count++;
                L.INSTANCE.i(count + " result=" + result);
            }
            callback.onEnd(result, null);
        }).start();
    }

    /**
     * 合并
     */
    public static String[] concatAudio(List<String> fileList, String targetFile) {
//        ffmpeg -i concat:%s|%s -acodec copy %s
        if (fileList == null || fileList.size() == 0) {
            return null;
        }
        StringBuilder concatBuilder = new StringBuilder();
        concatBuilder.append("concat:");
        for (String file : fileList) {
            concatBuilder.append(file).append("|");
        }
        String concatStr = concatBuilder.substring(0, concatBuilder.length() - 1);
        String concatAudioCmd = "ffmpeg -i %s -acodec copy %s";
        concatAudioCmd = String.format(concatAudioCmd, concatStr, targetFile);
        return concatAudioCmd.split(" ");
    }

    /**
     *  转码
     */
    public static String[] transformAudio(String srcFile, String acodec, String targetFile) {
        String transformAudioCmd = "ffmpeg -i %s -acodec %s -ac 2 -ar 44100 %s";
        transformAudioCmd = String.format(transformAudioCmd, srcFile, acodec, targetFile);
        return transformAudioCmd.split(" ");
    }

    /**
     * 剪切
     */
    @SuppressLint("DefaultLocale")
    public static String[] cutAudio(String srcFile, int startTime, int duration, String targetFile) {
        String cutAudioCmd = "ffmpeg -i %s -acodec copy -vn -ss %d -t %d %s";
        cutAudioCmd = String.format(cutAudioCmd, srcFile, startTime, duration, targetFile);
        return cutAudioCmd.split(" ");
    }
}
