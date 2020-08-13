package com.yooking.ffmpegdemo

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.yooking.utils.AudioManager
import com.yooking.utils.Format
import com.yooking.utils.L
import com.yooking.utils.ext.no
import com.yooking.utils.ext.otherwise
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
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
                    val formatStr = Format.formatChineseMoney(money)
                    L.i("用户输入的金额数为:${formatStr}")
                    holder.setText(R.id.tv_main_money, "准备播报的金额数为:\n$formatStr")

                    AudioManager.sendBroadcast(this, formatStr)
//                    start(formatStr)
                }.otherwise {
                    holder.setText(R.id.tv_main_hint, "请输入金额！！")
                }
            }
        )
    }

    private fun initPermission() {
        AudioManager.checkPermissionAndStartService(this)
    }

    fun start(str: String) = GlobalScope.launch {

    }


}
