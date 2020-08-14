package com.yooking.utils

import com.yooking.utils.ext.no
import com.yooking.utils.ext.yes

/**
 *
 * Created by yooking on 2020/8/12.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
object SoundFileUtils {

    const val defSoundPath = "def"
    const val aliSoundPath = "alipay"

    var soundPath = defSoundPath

    //       val unit1 = arrayListOf("万", "亿")
    //        val unit2 = arrayListOf("", "拾", "佰", "仟")
    //        val value = arrayListOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")

    private val soundMap: Map<String, String> = mapOf(
        "s" to "tts_success.mp3",
        "f" to "tts_user_cancel_pay.mp3",
        "零" to "tts_0.mp3",
        "壹" to "tts_1.mp3",
        "贰" to "tts_2.mp3",
        "叁" to "tts_3.mp3",
        "肆" to "tts_4.mp3",
        "伍" to "tts_5.mp3",
        "陆" to "tts_6.mp3",
        "柒" to "tts_7.mp3",
        "捌" to "tts_8.mp3",
        "玖" to "tts_9.mp3",
        "拾" to "tts_ten.mp3",
        "佰" to "tts_hundred.mp3",
        "仟" to "tts_thousand.mp3",
        "万" to "tts_ten_thousand.mp3",
        "亿" to "tts_ten_million.mp3",
        "点" to "tts_dot.mp3",
        "元" to "tts_yuan.mp3"
    )

    fun getFileNamesUnDuplication(formatStr: String): List<String> {
        val list: MutableList<String> = ArrayList()
        formatStr.toCharArray().forEach { char: Char ->
            soundMap.containsKey(char.toString()).yes {
                val soundFile = soundPath + "_" + (soundMap[char.toString()] ?: error("无对应数据"))
                list.contains(soundFile).no {
                    //去除重复
                    list.add(soundFile)
                }
            }
        }
        return list
    }

    fun getFileNames(formatStr: String): List<String> {
        val list: MutableList<String> = ArrayList()
        formatStr.toCharArray().forEach { char: Char ->
            soundMap.containsKey(char.toString()).yes {
                val soundFile = soundPath + "_" + soundMap[char.toString()] ?: error("无对应数据")
                list.add(soundFile)
            }
        }
        return list
    }
}