package cn.vove7.jarvis.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.jarvis.speech.wakeup.MyWakeup
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog

/**
 * # PowerEventReceiver
 *
 * @author Administrator
 * 2018/10/8
 */
class PowerEventReceiver : BroadcastReceiver() {
    companion object {
        var open = false
        var isCharging: Boolean = false //初始状态?
            get() {
                Vog.d(this, "isCharging ---> $field")
                return field
            }
        private val receiver: PowerEventReceiver by lazy {
            Vog.d(this, "pow ---> ")
            PowerEventReceiver()
        }

        private val intentFilter: IntentFilter by lazy {
            val i = IntentFilter()
            i.addAction(Intent.ACTION_POWER_CONNECTED)
            i.addAction(Intent.ACTION_POWER_DISCONNECTED)
            i
        }

        fun start() {
            open = true
            GlobalApp.APP.registerReceiver(receiver, intentFilter)
        }

        fun stop() {
            open = false
            GlobalApp.APP.unregisterReceiver(receiver)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!AppConfig.isAutoVoiceWakeupCharging) {
            Vog.d(this, "onReceive ---> isAutoVoiceWakeupCharging 未开启")
            return
        }
        //打开充电自动开启唤醒
        when (intent?.action) {
            Intent.ACTION_POWER_CONNECTED -> {//连接充电器
                isCharging = true
                if (!MyWakeup.opened) {//开启 并 已自动关闭
                    Vog.d(this, "onReceive ---> 正在充电 开启语语音唤醒")
                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP)
                } else {
                    Vog.d(this, "onReceive ---> 正在充电 语音唤醒已开启")
                    //关闭定时器
                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP_TIMER)
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {//断开
                isCharging = false
                if (!AppConfig.voiceWakeup) {//未开启
                    //关闭
                    Vog.d(this, "onReceive ---> 充电结束 语音唤醒关闭")

                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP)
                } else {//开启
                    //开启定时器
                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP_TIMER)
                }
            }
        }

    }

}