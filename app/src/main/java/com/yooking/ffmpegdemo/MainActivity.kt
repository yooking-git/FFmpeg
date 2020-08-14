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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var holder: ViewHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermission()

        AudioManager.addAudioServiceCallback(this@MainActivity,
            object : AudioManager.AudioServiceCallback {
                override fun isRunning(message: String?) {
                    runOnUiThread {
                        (message == null).no {
                            holder.setText(
                                R.id.tv_main_money,
                                "准备播报的金额数为:\n${message!!.replace("s", "")}"
                            )
                        }

                    }
                }
            })

        holder = ViewHolder(this@MainActivity)

        holder.setOnClickListener<Button>(
            R.id.btn_main_start, R.id.btn_main_auto,
            onClickListener = View.OnClickListener {
                when (it.id) {
                    R.id.btn_main_start -> {
                        val money = holder.getText(R.id.et_main_money)
                        money.isEmpty().no {
                            val incomeMoney = Format.formatIncomeMoney(money)
                            L.i("用户输入的金额数为:${incomeMoney.replace("s", "")}")

                            AudioManager.sendBroadcast(this, incomeMoney)
                        }.otherwise {
                            AudioManager.sendBroadcast(this, Format.formatUserCancel())
                        }
                    }
                    R.id.btn_main_auto -> {
                        val money = holder.getText(R.id.et_main_money)
                        val duration = holder.getText(R.id.et_main_duration)
                        val count = holder.getText(R.id.et_main_count)

                        (money.isEmpty() || duration.isEmpty() || count.isEmpty())
                            .no {
                                run(money.toInt(), count.toInt(), duration.toInt() * 1000)
                            }.otherwise {
                                AudioManager.sendBroadcast(
                                    this@MainActivity,
                                    Format.formatIncomeMoney(money)
                                )
                            }
                    }
                }
            }
        )
    }

    private fun initPermission() {
        AudioManager.checkPermissionAndStartService(this)
    }

    private fun run(money: Int, count: Int, duration: Int) = GlobalScope.launch {
        repeat(count) {
            L.i("第${it}次循环")
            val randomMoney = random(money).toString()

            val incomeMoney = Format.formatIncomeMoney(randomMoney)
            AudioManager.sendBroadcast(this@MainActivity, incomeMoney)

            delay(random(duration).toLong())
        }
    }

    private fun random(num: Int): Int {
        val min = (num * 0.8).toInt()
        val max = (num * 1.2).toInt()
        return (min..max).random()
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioManager.stopAudioService(this@MainActivity)
        AudioManager.destroyCallback(this@MainActivity)
    }
}
