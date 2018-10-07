package cn.vove7.jarvis.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.vtp.log.Vog

/**
 * # PowerEventReceiver
 *
 * @author Administrator
 * 2018/10/8
 */
class PowerEventReceiver : BroadcastReceiver() {
    companion object {
        var isCharging = false
        val receiver: PowerEventReceiver by lazy {
            PowerEventReceiver()
        }

        val intentFilter: IntentFilter by lazy {
            val i = IntentFilter()
            i.addAction(Intent.ACTION_POWER_CONNECTED)
            i.addAction(Intent.ACTION_POWER_DISCONNECTED)
            i
        }

        fun start() {
            GlobalApp.APP.registerReceiver(receiver, intentFilter)
        }

        fun stop() {
            GlobalApp.APP.unregisterReceiver(receiver)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!AppConfig.isAutoVoiceWakeupCharging) {
            Vog.d(this, "onReceive --->isAutoVoiceWakeupCharging 未开启")
            return
        }
        when (intent?.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                if (!AppConfig.voiceWakeup) {
                    Vog.d(this, "onReceive ---> 正在充电 开启语语音唤醒")
                    AppBus.post(SpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP))
                } else {
                    Vog.d(this, "onReceive ---> 正在充电 语音唤醒已开启")
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                if (!AppConfig.voiceWakeup) {//未开启
                    //关闭
                    Vog.d(this, "onReceive ---> 充电结束 语音唤醒关闭")

                    AppBus.post(SpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP))
                }
            }
        }

    }

}