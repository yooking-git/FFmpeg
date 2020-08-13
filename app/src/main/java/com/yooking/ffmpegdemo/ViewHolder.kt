package com.yooking.ffmpegdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.yooking.utils.ext.otherwise
import com.yooking.utils.ext.yes

/**
 *
 * Created by yooking on 2020/8/11.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
class ViewHolder(context: Activity) {

    private val context: Activity = context

    @SuppressLint("UseSparseArrays")
    val map: MutableMap<Int, View> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <T : View> findView(@IdRes id: Int): T {
        return map.containsKey(id).yes {
            map[id] as T
        }.otherwise {
            val view: T = context.findViewById(id)
            map[id] = view
            view
        }
    }

    fun <T : View> setOnClickListener(@IdRes id: Int, onClickListener: View.OnClickListener) {
        findView<T>(id).setOnClickListener(onClickListener)
    }

    fun getText(@IdRes id: Int): String {
        return findView<TextView>(id).text.toString()
    }

    fun setText(@IdRes id: Int, content: String): ViewHolder {
        findView<TextView>(id).text = content
        return this@ViewHolder
    }

    @SuppressLint("SetTextI18n")
    fun addText(@IdRes id: Int, content: String): ViewHolder {
        val textView = findView<TextView>(id)
        textView.text = textView.text.toString() + content
        return this@ViewHolder
    }
}