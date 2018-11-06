package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.jarvis.plugins.VoiceWakeupStrategy
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog

/**
 * # ScreenStatusListener
 *
 * @author Administrator
 * 2018/10/28
 */
object ScreenStatusListener : DyBCReceiver() {
    override val intentFilter: IntentFilter by lazy {
        val i = IntentFilter()
        i.addAction(Intent.ACTION_SCREEN_OFF)
        i.addAction(Intent.ACTION_SCREEN_ON)
        i
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {
                Vog.d(this, "onReceive ---> 亮屏")
                if (AppConfig.openVoiceWakeUpIfAutoSleep && AppConfig.voiceWakeup && WakeupI.instance?.opened == false) {
                    if(VoiceWakeupStrategy.canOpenRecord())
                        AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP_WITHOUT_SWITCH)//不打开语音唤醒开关
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                Vog.d(this, "onReceive ---> 灭屏")
            }
        }
    }
}