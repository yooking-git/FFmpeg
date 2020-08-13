package com.yooking.utils

import com.yooking.utils.ext.otherwise
import com.yooking.utils.ext.yes
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据校验类
 * Created by yooking on 2020/8/11.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
object Format {

    fun formatTime(time: String, from: String, to: String): String {
        val sdfFrom = SimpleDateFormat(from, Locale.CHINA)
        val date = sdfFrom.parse(time)
        val sdfTo = SimpleDateFormat(to, Locale.CHINA)
        return sdfTo.format(date!!)
    }

    fun formatChineseMoney(money: String): String {
        val unit1 = arrayListOf("万", "亿")
        val unit2 = arrayListOf("", "拾", "佰", "仟")
        val value = arrayListOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")

        val moneyArr = money.split(".")
        val moneyInteger: String = moneyArr[0]
        var moneyDecimal = ""
        if (moneyArr.size > 1) {
            moneyDecimal = moneyArr[1]
        }

        var sum = ""

        val miSize = moneyInteger.length
        (miSize > 16).yes {
            L.e("数值超过亿亿，可能返回的结果不是您想要的")
        }
        for (i in moneyInteger.indices) {
            val j = miSize - i - 1
            val v = value[moneyInteger[j].toString().toInt()]

            val u2 = unit2[i % 4]

            var u1 = ""
            (i % 4 == 0 && i > 0).yes {
                u1 = unit1[(i / 4 - 1) % 2]
            }

            ("零" == v).yes {
                sum = v + u1 + sum
            }.otherwise {
                sum = v + u2 + u1 + sum
            }
        }

        sum = reduceZero(sum)
        ("零" == sum[sum.lastIndex].toString() && "零" != sum).yes {
            sum = sum.substring(0, sum.lastIndex)
        }

        L.i("小数点数值为${(if (moneyDecimal.isEmpty()) "0" else moneyDecimal).toInt()}")
        (moneyDecimal.isNotEmpty() && moneyDecimal.toInt() != 0).yes {
            //小数点后面数值不为空，且值不是0
            sum += "点"
            val v1 = value[moneyDecimal[0].toString().toInt()]
            (moneyDecimal.length > 1 && moneyDecimal[1].toString().toInt() > 0).yes {
                //小数点数值为2位，且第二位数大于0
                val v2 = value[moneyDecimal[1].toString().toInt()]
                sum += v1 + v2
            }.otherwise {
                sum += v1
            }
        }

        sum += "元"
        return sum
    }

    private fun reduceZero(moneyInteger: String): String {
        val m: String = moneyInteger
            .replace("零零", "零")
            .replace("零万", "万")
            .replace("零亿", "亿")
            .replace("亿万", "亿")
        (m.contains("零零") || m.contains("零万")
                || m.contains("零亿") || m.contains("亿万")).yes {
            return reduceZero(m)
        }.otherwise {
            return m
        }
    }
}