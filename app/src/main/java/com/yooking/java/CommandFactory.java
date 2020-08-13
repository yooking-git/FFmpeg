package com.yooking.java;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.yooking.utils.F;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频合并工厂
 * Created by yooking on 2020/8/10.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
public class CommandFactory {

    private static CommandFactory instance;

    private CommandFactory() {
    }

    public static CommandFactory get() {
        if (instance == null) {
            synchronized (CommandFactory.class) {
                if (instance == null) {
                    instance = new CommandFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化build
     */
    public Builder build(Context context) {
        return new Builder(context);
    }

    public class Builder {
        private List<String[]> commandList;
        private List<String> fileList;

        private List<String[]> transformList;
        private List<String> transformFileList;
        private final String basePath;

        private Builder(Context activity) {
            basePath = F.INSTANCE.getExternalStorageDirectory(activity);
            fileList = new ArrayList<>();
            transformList = new ArrayList<>();
            transformFileList = new ArrayList<>();
        }

        private boolean isRemoveFile = false;

        /**
         * 添加要进行合并的音频文件 按顺序合并
         *
         * @param filePath 音频文件绝对路径
         */
        public Builder add(String filePath) {

            String defaultPath = basePath + File.separator + "sound/copy";
            String path = defaultPath + fileList.size() + ".mp3";

            Log.i(">>>>>>>>>>>", "fileList.size:" + fileList.size());

            String[] transformCmd = FFmpegCmd.transformAudio(filePath, "libmp3lame", path);
            transformList.add(transformCmd);
            transformFileList.add(path);

            fileList.add(filePath);
            return this;
        }

        /**
         * 是否删除源文件 默认false
         *
         * @param removeFile 默认false
         */
        public Builder setRemoveFile(boolean removeFile) {
            isRemoveFile = removeFile;
            return this;
        }

        private void makeCommandList(String jointAudioPath) {
            if (commandList != null) {
                commandList.clear();
            } else {
                commandList = new ArrayList<>();
            }
            commandList.addAll(transformList);
            String[] jointAudioCmd = FFmpegCmd.concatAudio(transformFileList, jointAudioPath);
            commandList.add(jointAudioCmd);
        }

        /**
         * 开始执行合并操作
         */
        public void excute(String jointAudioPath, FFmpegCallback callback) {
            makeCommandList(jointAudioPath);
            FFmpegCmd.execute(commandList, new FFmpegCallback() {
                @Override
                public void onBegin() {
                    callback.onBegin();
                }

                @Override
                public void onEnd(int resultCode, String resultMsg) {
                    if (isRemoveFile) {
                        for (String filePath : fileList) {
                            F.INSTANCE.delete(filePath);
                        }
                    }
                    for (String filePath : transformFileList) {
                        F.INSTANCE.delete(filePath);
                    }
                    callback.onEnd(resultCode, resultMsg);
                }
            });
        }
    }


}
