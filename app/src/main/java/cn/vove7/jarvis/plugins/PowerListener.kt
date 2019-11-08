package cn.vove7.jarvis.plugins

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.BatteryManager.BATTERY_STATUS_FULL
import cn.vove7.jarvis.receivers.DyBCReceiver
import cn.vove7.jarvis.services.MainService


object PowerListener : DyBCReceiver() {
    override val intentFilter: IntentFilter
        get() = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }


    /**
     * 判断充满提醒
     */
    private var flag = false

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                if (status == BATTERY_STATUS_FULL && !flag) {
                    flag = true //置flag  确保提醒一次
                    MainService.speak(PluginConfig.onFullText)
                }
            }
            Intent.ACTION_POWER_CONNECTED -> {//连接充电器
                MainService.speak(PluginConfig.onChargingText)
            }
            Intent.ACTION_BATTERY_LOW -> {//低电量
                MainService.speak(PluginConfig.onLowText)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {//重置flag
                flag = false
            }
        }
    }
}
