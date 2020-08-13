package com.yooking.utils

import android.util.Log
import com.yooking.ffmpegdemo.BuildConfig
import com.yooking.utils.ext.yes

/**
 * Log工具类
 * Created by yooking on 2020/8/11.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
object L {
    private val isDebug: Boolean = BuildConfig.DEBUG

    fun i(message: String) {
        isDebug.yes {
            val tag: String = Exception().stackTrace[1].fileName +
                    ":" + Exception().stackTrace[1].methodName +
                    "(" + Exception().stackTrace[1].lineNumber + ")"
            Log.i(tag, message)
        }
    }

    fun e(message: String) {
        isDebug.yes {
            val tag: String = Exception().stackTrace[1].fileName +
                    ":" + Exception().stackTrace[1].methodName +
                    "(" + Exception().stackTrace[1].lineNumber + ")"
            Log.e(tag, message)
        }
    }

    fun e(message: String, throwable: Throwable?) {
        isDebug.yes {
            val tag: String = Exception().stackTrace[1].fileName +
                    ":" + Exception().stackTrace[1].methodName +
                    "(" + Exception().stackTrace[1].lineNumber + ")"
            Log.e(tag, message, throwable)
        }
    }
}