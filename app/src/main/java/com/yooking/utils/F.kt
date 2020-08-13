package com.yooking.utils

import android.app.Activity
import android.content.Context
import android.os.Environment
import com.yooking.utils.ext.no
import com.yooking.utils.ext.otherwise
import com.yooking.utils.ext.yes
import java.io.File
import java.io.FileOutputStream

/**
 * File工具类
 * Created by yooking on 2020/8/11.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
object F {

    /**
     * 获取路径/data/user/0
     */
    fun getExternalStorageDirectory(context: Context): String {
        return context.filesDir.absoluteFile.absolutePath
    }

    /**
     * 判断sd卡是否存在
     */
    fun isSdCardExist(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 获取sd卡路径
     */
    fun getSDCardDirectory(context: Activity):String{

        return context.getExternalFilesDir("")!!.path
    }

    /**
     * 获取assets中的文件名
     * @param path assets中的文件夹名称，根目录填""
     */
    fun getAssetsNames(context: Context, path: String): Array<String>? {
        return context.assets.list(path)
    }

    @Deprecated("数据量过大会导致卡顿！！！慎用")
    fun assets2Files(context: Context, assetsPath: String, assetsNames: Array<String>) {
        for (assetsName in assetsNames) {
            Runnable {
                //耗时操作在线程中运行
                assets2File(context, assetsPath, assetsName)
            }.run()
        }
    }

    fun assets2File(context: Context, assetsPath: String, assetsName: String) {
        assets2File(context, assetsPath, assetsName, getExternalStorageDirectory(context))
    }

    fun assets2File(
        context: Context,
        assetsPath: String,
        assetsName: String,
        directoryPath: String
    ) {
        val filePath: String =
            directoryPath + File.separator + assetsPath + File.separator + assetsName
        val dirFile = File(directoryPath + File.separator + assetsPath)
        L.i("${directoryPath}文件夹是否存在？${dirFile.exists()}")
        dirFile.exists().no {
            L.i("${filePath}文件夹创建成功${dirFile.mkdirs()}")
        }
        val file = File(filePath)
        L.i("${filePath}文件是否存在？${file.exists()}")
        file.exists().no {
            L.i("${filePath}文件创建成功${file.createNewFile()}")
            //文件不存在时，开始转移
            val inputStream = context.assets.open(assetsPath + File.separator + assetsName)
            L.i("从资源文件获取数据成功")
            val outputStream = FileOutputStream(filePath)
            L.i("获取输出目标的数据成功")
            val byteArray = ByteArray(512)
            while (inputStream.read(byteArray) != -1) {
                outputStream.write(byteArray)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        }
    }

    fun isExist(filePath: String): Boolean {
        return File(filePath).exists()
    }

    fun delete(filePath: String): Boolean {
        val file = File(filePath)
        file.exists().yes {
            return file.delete()
        }.otherwise {
            L.i("删除失败，文件不存在")
            return false
        }
    }
}