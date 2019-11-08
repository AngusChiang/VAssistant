package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.common.app.AppConfig
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.utils.StubbornFlag
import cn.vove7.jarvis.plugins.VoiceWakeupStrategy
import cn.vove7.jarvis.services.MainService
import cn.vove7.vtp.log.Vog

/**
 * # ScreenStatusListener
 * 开屏|熄屏监听器
 * @author Administrator
 * 2018/10/28
 */
object ScreenStatusListener : DyBCReceiver(), ScreenEvent {

    var screenOn: Boolean = true

    val event: ScreenEvent = this
    override val intentFilter: IntentFilter
        get() = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {
                Vog.d("亮屏")
                screenOn = true
                event.onScreenOn()
            }
            Intent.ACTION_SCREEN_OFF -> {
                Vog.d("灭屏")
                screenOn = false
                event.onScreenOff()
            }
            Intent.ACTION_USER_PRESENT -> {
                Vog.d("解锁")
                event.onUnlock()
            }
        }
    }

    override fun onScreenOn() {
        if (PowerEventReceiver.lowBatteryLevel) {
            Vog.d("低电量模式")
            return
        }
        if (AppConfig.openVoiceWakeUpIfAutoSleep && AppConfig.voiceWakeup && !MainService.wakeupOpen) {
            if (VoiceWakeupStrategy.canOpenRecord())//判断当前App 是否使用麦克风
                AppBus.post(AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH)//开启语音唤醒，不打开语音唤醒开关

        }
    }

    override fun onUnlock() {
        if (closeTag && AppConfig.fixVoiceMicro && AppConfig.voiceWakeup) {//也许关屏已开启
            Vog.d("开屏关闭唤醒")
            VoiceWakeupStrategy.closeWakeup()
        }
    }

    /**
     * (在微信页面关闭唤醒时, 熄屏开启唤醒)标志
     */
    private var closeTag by StubbornFlag(false)

    override fun onScreenOff() {//在当前App 关闭唤醒时,熄屏 应开启唤醒
        if (VoiceWakeupStrategy.closed && AppConfig.voiceWakeup) {//被自动关闭,熄屏开启
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