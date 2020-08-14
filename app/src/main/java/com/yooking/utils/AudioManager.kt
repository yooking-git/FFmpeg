package com.yooking.utils

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import com.yooking.ffmpegdemo.AudioService
import com.yooking.utils.ext.no
import com.yooking.utils.ext.yes


/**
 * 收款播报广播工具
 * Created by yooking on 2020/8/13.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
object AudioManager {

    var PATH_CONCAT_AUDIO = "/${SoundFileUtils.soundPath}/concatAudio.mp3"

    const val KEY_MESSAGE = "message"
    private const val KEY_ACTION = "audioReceiver"

    private const val KEY_CALLBACK = "audioCallback"
    private const val KEY_MONEY = "money"

    fun sendBroadcast(context: Context, message: String) {
        val intent = Intent()
        intent.action = KEY_ACTION
        intent.putExtra(KEY_MESSAGE, message)
        context.sendBroadcast(intent)
    }

    private fun startAudioService(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            context.startForegroundService(Intent(context, AudioService().javaClass))
        } else {
            context.startService(Intent(context, AudioService().javaClass))
        }
    }

    fun stopAudioService(context: Context) {
        context.stopService(Intent(context, AudioService().javaClass))
    }

    fun checkPermissionAndStartService(context: FragmentActivity) {
        val permissionMap = mapOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE to "文件写入权限",
            Manifest.permission.READ_EXTERNAL_STORAGE to "文件读取权限"
        )
        //1 权限判断
        PermissionX.init(context)
            .permissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .onExplainRequestReason { scope, deniedList ->
                var permissionNames = ""
                for (perName in deniedList) {
                    permissionNames += "\n" + permissionMap[perName]
                }
                scope.showRequestReasonDialog(
                    deniedList,
                    "应用需要以下:${permissionNames}\n用于初始化语音播报功能",
                    "确定"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                var permissionNames = ""
                for (perName in deniedList) {
                    permissionNames += "\n" + permissionMap[perName]
                }
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "应用需要以下:${permissionNames}\n用于初始化语音播报功能，请到设置中授权",
                    "确定"
                )
            }
            .request { allGranted, _, _ ->
                allGranted.yes {
                    //启动服务
                    AlertDialog.Builder(context)
                        .setTitle("请选择播放的语音")
                        .setCancelable(false)
                        .setNeutralButton("支付宝") { _, _ ->
                            SoundFileUtils.soundPath = SoundFileUtils.aliSoundPath
                            startAudioService(context)
                        }.setNegativeButton("默认") { _, _ ->
                            SoundFileUtils.soundPath = SoundFileUtils.defSoundPath
                            startAudioService(context)
                        }.show()

                }
            }
    }

    //接收播报信号相关
    private var audioMessageReceiver: BroadcastReceiver? = null

    fun registerBroadcast(context: Context, callback: SendMessageCallback) {
        //注册广播接收器
        val filter = IntentFilter()
        filter.addAction(KEY_ACTION)
        audioMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                (intent == null).no {
                    //
                    L.i(intent!!.getStringExtra(KEY_MESSAGE) ?: error("接收到异常数据"))
                    //判断是否正在执行
                    val message: String =
                        intent.getStringExtra(KEY_MESSAGE) ?: error("")
                    callback.getMessage(message)
                }
            }
        }
        context.registerReceiver(audioMessageReceiver, filter)
    }

    fun sendCallbackMessage(context: Context, message: String) {
        val intent = Intent()
        intent.action = KEY_CALLBACK
        intent.putExtra(KEY_MONEY, message)
        context.sendBroadcast(intent)
    }

    fun destroySendMessageCallback(context: Context) {
        (audioMessageReceiver != null).yes {
            context.unregisterReceiver(audioMessageReceiver)
            audioMessageReceiver = null
        }
    }

    interface SendMessageCallback {
        fun getMessage(message: String)
    }


    //服务回调相关
    private var callbackReceiver: BroadcastReceiver? = null

    fun addAudioServiceCallback(context: Context, callback: AudioServiceCallback) {
        //注册广播接收器
        val filter = IntentFilter()
        filter.addAction(KEY_CALLBACK)
        callbackReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                (intent == null).no {
                    callback.isRunning(intent!!.getStringExtra(KEY_MONEY))
                }
            }
        }
        context.registerReceiver(callbackReceiver, filter)
    }

    fun destroyCallback(context: Context) {
        (callbackReceiver != null).yes {
            context.unregisterReceiver(callbackReceiver)
            callbackReceiver = null
        }
    }

    interface AudioServiceCallback {
        fun isRunning(message: String?)
    }
}