package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.plugins.VoiceWakeupStrategy
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog

/**
 * # PowerEventReceiver
 * 充电状态监听
 * fixed 启动App时无法获得当前充电状态
 * @author Administrator
 * 2018/10/8
 */
object PowerEventReceiver : DyBCReceiver(), OnPowerEvent {
    val event: OnPowerEvent = this //can be list 方便二次开发
    var needBatteryLevelChanged = false
    override val intentFilter: IntentFilter by lazy {
        val i = IntentFilter()
        i.addAction(Intent.ACTION_POWER_CONNECTED)
        i.addAction(Intent.ACTION_POWER_DISCONNECTED)
        i.addAction(Intent.ACTION_BATTERY_LOW)
        i.addAction(Intent.ACTION_BATTERY_OKAY)
        if (needBatteryLevelChanged)
            i.addAction(Intent.ACTION_BATTERY_CHANGED)
        i
    }
    var isCharging: Boolean = SystemBridge.isCharging //初始状态?

    /**
     * 接受事件，事件分发
     * @param context Context?
     * @param intent Intent?
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        //打开充电自动开启唤醒
        when (intent?.action) {
            Intent.ACTION_POWER_CONNECTED -> {//连接充电器
                isCharging = true
                event.onCharging()
            }
            Intent.ACTION_POWER_DISCONNECTED -> {//断开
                isCharging = false
                event.onDischarging()
            }
            Intent.ACTION_BATTERY_LOW -> {//低电量
                event.onLowBattery()
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) //电量的刻度
                val maxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) //最大
                val l = level * 100 / maxLevel
                event.onBatteryScaleChange(l)
            }
            Intent.ACTION_BATTERY_OKAY -> {
                event.onFullBattery()
            }
        }

    }

//    private var isDisableAdKillerService = false
//        get() {
//            val v = field
//            field = false
//            return v
//        }
    /**
     * 关闭服务标记
     */
    private var isDisableAccessibilityService = false
        get() {
            val v = field
            field = false
            return v
        }

    /**
     * 小于20算低电量
     */
    val lowBatteryLevel: Boolean
        get() = SystemBridge.batteryLevel < 20

    /**
     * 省电模式
     */
    var powerSavingMode = lowBatteryLevel

    override fun onLowBattery() {
        powerSavingMode = true
        if (AppConfig.disableAccessibilityOnLowBattery &&
                AccessibilityApi.accessibilityService != null) {//自动关闭无障碍
            AccessibilityApi.accessibilityService?.powerSavingMode()
            AdKillerService.gc()
            isDisableAccessibilityService = true
        }
        if (MainService.instance?.speechRecoService?.wakeupI?.opened == true) {
            AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP_WITHOUT_SWITCH)
        }
    }

    override fun onCharging() {
        //充电继续服务
        powerSavingMode = false

        if (isDisableAccessibilityService) {//开启无障碍
            Vog.d(this, "onCharging ---> 开启无障碍")
            AccessibilityApi.accessibilityService?.disablePowerSavingMode()
        }

        if (!AppConfig.isAutoVoiceWakeupCharging) {
            Vog.d(this, "onReceive ---> isAutoVoiceWakeupCharging 未开启")
            return
        }
        //充电自动开启唤醒
        if (WakeupI.instance?.opened == false) {//开启 并 已自动关闭
            Vog.d(this, "onReceive ---> 正在充电 开启语音唤醒")
            //开启了无障碍
            if (VoiceWakeupStrategy.canOpenRecord())
                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP_WITHOUT_SWITCH)//不打开语音唤醒开关

        } else {
            Vog.d(this, "onReceive ---> 正在充电 语音唤醒已开启")
            //关闭定时器
            AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP_TIMER)
        }
    }

    override fun onDischarging() {
        if (!AppConfig.isAutoVoiceWakeupCharging) return
        if (!AppConfig.voiceWakeup) {//未开启
            //关闭
            Vog.d(this, "onReceive ---> 充电结束 语音唤醒关闭")
            AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP_WITHOUT_SWITCH)
        } else {//开启
            //开启定时器
            AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP_TIMER)
        }
    }

    override fun onFullBattery() {
    }

    override fun onBatteryScaleChange(batteryLevel: Int) {
        Vog.d(this, "onBatteryScaleChange ---> $batteryLevel")
    }
}

interface OnPowerEvent {
    /**
     * 连接充电器
     */
    fun onCharging()

    /**
     * 充电器断开
     */
    fun onDischarging()

    /**
     * 低电量
     */
    fun onLowBattery()

    /**
     * 满电
     */
    fun onFullBattery()

    fun onBatteryScaleChange(batteryLevel: Int)

}