package com.yooking.ffmpegdemo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import com.yooking.java.CommandFactory
import com.yooking.java.FFmpegCallback
import com.yooking.utils.AudioManager
import com.yooking.utils.F
import com.yooking.utils.L
import com.yooking.utils.SoundFileUtils
import com.yooking.utils.ext.BooleanExt
import com.yooking.utils.ext.no
import com.yooking.utils.ext.otherwise
import com.yooking.utils.ext.yes
import java.io.File


/**
 * 语音播报服务
 * Created by yooking on 2020/8/13.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
class AudioService : Service() {

    companion object {
        private const val CHANNEL_NAME = "AudioServiceChannel"
        private const val CHANNEL_ID = "10086"
    }

    private var isRunning = false
    private lateinit var audioList: MutableList<String>

    override fun onCreate() {
        super.onCreate()

        //显示前台通知
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        startForeground(110, getNotification())

        audioList = ArrayList()

        AudioManager.registerBroadcast(this@AudioService,
            object : AudioManager.SendMessageCallback {
                override fun getMessage(message: String) {
                    isRunning.yes {
                        //如果正在运行，则将数据加入等待区
                        audioList.add(message)
                    }.otherwise {
                        run(message)
                    }
                }
            })
        //将数据导出到用户的App中
        val path = SoundFileUtils.soundPath
        val assetsNames: Array<String>? = F.getAssetsNames(this@AudioService, path)
        (assetsNames != null).yes {
            F.assets2Files(this@AudioService, path, assetsNames!!)
        }
    }

    private fun getNotification(): Notification {
        val builder = Notification.Builder(this)
            .setSmallIcon(R.mipmap.icon_head)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_message))
        //设置Notification的ChannelID,否则不能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID)
        }
        return builder.build()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        L.i(getString(R.string.service_destroy))
        super.onDestroy()
        AudioManager.destroySendMessageCallback(this@AudioService)
    }

    private fun run(message: String) {
        AudioManager.sendCallbackMessage(this@AudioService, message)
        (audioList.size > 0).yes {
            audioList.removeAt(0)
        }
        isRunning = true
        checkFileIsExist(message)
        parsingData(message, object : FFmpegCallback {
            override fun onBegin() {
                //音频文件生成缓冲中
            }

            override fun onEnd(resultCode: Int, resultMsg: String?) {
                //音频文件生成成功
                player()
            }
        })
    }

    private fun parsingData(str: String, callback: FFmpegCallback): BooleanExt<Unit> {
        val builder = CommandFactory.get().build(this@AudioService)
        val path = SoundFileUtils.soundPath
        val basePath = F.getExternalStorageDirectory(this@AudioService)
        var isAllExist = true

        for (fileName in SoundFileUtils.getFileNamesUnDuplication(str)) {
            val file = File(basePath + File.separator + path + File.separator + fileName)
            file.exists().no {
                //检测到目标文件时无法播报
                isAllExist = false
            }
        }
        return isAllExist.yes {
            //            builder.add(basePath + File.separator + path + File.separator + "tts_success.mp3")//默认添加收款成功消息
            for (fileName in SoundFileUtils.getFileNames(str)) {
                builder.add(basePath + File.separator + path + File.separator + fileName)
            }
            builder.setRemoveFile(false)
                .excute(basePath + AudioManager.PATH_CONCAT_AUDIO, callback)
        }
    }

    private fun player() {
        val basePath = F.getExternalStorageDirectory(this@AudioService)
        val file = File(basePath + AudioManager.PATH_CONCAT_AUDIO)
        L.i(file.absolutePath)
        file.exists().yes {
            val player = MediaPlayer()
            player.setDataSource(this@AudioService, Uri.parse(file.absolutePath))
            player.prepare()
            player.start()
            player.setOnCompletionListener {
                (audioList.size > 0).yes {
                    run(audioList[0])
                }.otherwise {
                    isRunning = false
                }
            }
        }
    }

    private fun checkFileIsExist(str: String) {
//        holder.setText(R.id.tv_main_hint, "开始解析数据")
        //检测音频文件是否存在
        val path = SoundFileUtils.soundPath
        val basePath = F.getExternalStorageDirectory(this@AudioService)

        //清除之前播放的文件
        F.delete(basePath + AudioManager.PATH_CONCAT_AUDIO)

        for (fileName in SoundFileUtils.getFileNamesUnDuplication(str)) {
            val file = File(basePath + File.separator + path + File.separator + fileName)
            L.i("fileName:${fileName}isExist？${file.exists()}")
            file.exists().no {
                //检测到目标文件不存在时则创建数据
                F.assets2File(this@AudioService, path, fileName)
            }
        }
    }
}