package com.yooking.ffmpegdemo

import android.Manifest
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import com.yooking.java.CommandFactory
import com.yooking.java.FFmpegCallback
import com.yooking.utils.F
import com.yooking.utils.Format
import com.yooking.utils.L
import com.yooking.utils.SoundFileUtils
import com.yooking.utils.ext.no
import com.yooking.utils.ext.otherwise
import com.yooking.utils.ext.yes
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG_BEGIN = 1
        const val TAG_LOADING = 2
        const val TAG_END = 3
        const val TAG_ERROR = 4

        const val MEDIA_WAIT = 0
        const val MEDIA_START = 1
        const val MEDIA_END = 2

        const val MEDIA_NAME = "MEDIA_NAME"
    }

    private lateinit var formatStr: String

    private val PATH_CONCAT_AUDIO = File.separator + "concatAudio.mp3"

    private val fileHandler: Handler = Handler {
        //文件处理Handler
        when (it.what) {
            TAG_BEGIN -> {
                L.i("开始解析数据")
                begin()
            }
            TAG_LOADING -> {
                L.i("正在处理数据")
                holder.setText(R.id.tv_main_hint, "正在处理数据")
                loading()
            }
            TAG_END -> {
                L.i("数据解析完毕")
                holder.setText(R.id.tv_main_hint, "数据解析完毕,准备播报")
//                deleteCopy()
                player()
            }
            TAG_ERROR -> {
                L.i("数据解析失败")
                holder.setText(R.id.tv_main_hint, "数据解析失败")
            }
            else -> {
                L.e("传入参数有误")
            }
        }
        false
    }

    private fun begin() {
        holder.setText(R.id.tv_main_hint, "开始解析数据")
        //检测音频文件是否存在
        val path = "sound"
        val basePath = F.getExternalStorageDirectory(this)

        //清除之前播放的文件
        F.delete(basePath + PATH_CONCAT_AUDIO)

        //成功通知语音是否存在
//        val successFileName = "tts_success.mp3"
//        val successPath = basePath + File.separator + path + File.separator + successFileName
//        F.isExist(successPath).no {
//            holder.addText(
//                R.id.tv_main_hint,
//                "\n检测文件$successFileName 不存在，正在创建数据"
//            )
//            F.assets2File(this, path, successFileName)
//        }
        for (fileName in SoundFileUtils.getFileNamesUnDuplication(formatStr)) {
            val file = File(basePath + File.separator + path + File.separator + fileName)
            L.i("检测文件${fileName}是否存在？${file.exists()}")
            file.exists().no {
                //检测到目标文件不存在时则创建数据
                holder.addText(
                    R.id.tv_main_hint,
                    "\n检测文件${fileName}不存在，正在创建数据"
                )
                F.assets2File(this, path, fileName)
            }
        }

        fileHandler.sendEmptyMessage(TAG_LOADING)
    }

    private fun loading() {
        val builder = CommandFactory.get().build(this)
        val path = "sound"
        val basePath = F.getExternalStorageDirectory(this)
        var isAllExist = true
//        val successPath = basePath + File.separator + path + File.separator + "tts_success.mp3"
//        F.isExist(successPath).no {
//            isAllExist = false
//        }
        for (fileName in SoundFileUtils.getFileNamesUnDuplication(formatStr)) {
            val file = File(basePath + File.separator + path + File.separator + fileName)
            file.exists().no {
                //检测到目标文件时无法播报
                isAllExist = false
            }
        }
        isAllExist.yes {
            //            builder.add(basePath + File.separator + path + File.separator + "tts_success.mp3")//默认添加收款成功消息
            for (fileName in SoundFileUtils.getFileNames(formatStr)) {
                builder.add(basePath + File.separator + path + File.separator + fileName)
            }
            builder.setRemoveFile(false)
                .excute(basePath + PATH_CONCAT_AUDIO,
                    object : FFmpegCallback {
                        override fun onBegin() {

                        }

                        override fun onEnd(resultCode: Int, resultMsg: String?) {
                            fileHandler.sendEmptyMessage(TAG_END)
                        }
                    })
        }.otherwise {
            fileHandler.sendEmptyMessage(TAG_ERROR)
        }
    }

    private fun player() {
        val basePath = F.getExternalStorageDirectory(this)
        val file = File(basePath + PATH_CONCAT_AUDIO)
        L.i(file.absolutePath)
        file.exists().yes {
            val player = MediaPlayer()
            player.setDataSource(this, Uri.parse(file.absolutePath))
            player.prepare()
            player.start()
        }
    }

    private lateinit var holder: ViewHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermission()

        holder = ViewHolder(this@MainActivity)

        holder.setOnClickListener<Button>(
            R.id.btn_main_start,
            onClickListener = View.OnClickListener {
                val money = holder.getText(R.id.et_main_money)
                money.isEmpty().no {
                    formatStr = "s" + Format.formatChineseMoney(money)
                    L.i("用户输入的金额数为:${formatStr}")
                    holder.setText(R.id.tv_main_money, "准备播报的金额数为:\n$formatStr")


                    fileHandler.sendEmptyMessage(TAG_BEGIN)
                }.otherwise {
                    holder.setText(R.id.tv_main_hint, "请输入金额！！")
                }
            }
        )
    }

    private fun initPermission() {
        val permissionMap = mapOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE to "文件写入权限",
            Manifest.permission.READ_EXTERNAL_STORAGE to "文件读取权限"
        )
        //1 权限判断
        PermissionX.init(this)
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
                    //将数据导出到用户的App中
                    val path = "sound"
                    val assetsNames: Array<String>? = F.getAssetsNames(this@MainActivity, path)
                    (assetsNames != null).yes {
                        F.assets2Files(this@MainActivity, path, assetsNames!!)
                    }
                }
            }
    }
}
