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
 * 开屏|熄屏监听器
 * @author Administrator
 * 2018/10/28
 */
object ScreenStatusListener : DyBCReceiver(), ScreenEvent {

    val event: ScreenEvent = this
    override val intentFilter: IntentFilter by lazy {
        val i = IntentFilter()
        i.addAction(Intent.ACTION_SCREEN_OFF)
        i.addAction(Intent.ACTION_SCREEN_ON)
        i.addAction(Intent.ACTION_USER_PRESENT)
        i
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {
                Vog.d(this, "onReceive ---> 亮屏")
                event.onScreenOn()
            }
            Intent.ACTION_SCREEN_OFF -> {
                Vog.d(this, "onReceive ---> 灭屏")
                event.onScreenOff()
            }
            Intent.ACTION_USER_PRESENT -> {
                Vog.d(this, "onReceive ---> 解锁")
                event.onUnlock()
            }
        }
    }

    override fun onScreenOn() {
        if (PowerEventReceiver.lowBatteryLevel) {
            Vog.d(this, "onScreenOn ---> 低电量模式")
            return
        }
        if (AppConfig.openVoiceWakeUpIfAutoSleep && AppConfig.voiceWakeup && WakeupI.instance?.opened == false) {
            if (VoiceWakeupStrategy.canOpenRecord())//判断当前App 是否使用麦克风
                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP_WITHOUT_SWITCH)//不打开语音唤醒开关

        }
    }

    override fun onUnlock() {
        if (closeTag && AppConfig.fixVoiceMico) {//也许关屏已开启
            Vog.d(this, "onScreenOn ---> 开屏关闭唤醒")
            VoiceWakeupStrategy.closeWakeup()
        }
    }

    /**
     * (在微信页面关闭唤醒时, 熄屏开启唤醒)标志
     */
    var closeTag = false
        get() {
            val v = field
            field = false
            return v
        }

    override fun onScreenOff() {//在当前App 关闭唤醒时,熄屏 应开启唤醒
        if (VoiceWakeupStrategy.closed) {//被自动关闭,熄屏开启
            VoiceWakeupStrategy.startWakeup()
            closeTag = true
        }
    }
}

interface ScreenEvent {
    fun onScreenOn()
    fun onScreenOff()
    fun onUnlock()
}