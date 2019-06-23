package cn.vove7.jarvis.services

import android.annotation.TargetApi
import android.os.Build
import cn.vove7.common.appbus.AppBus
import cn.vove7.jarvis.R
import cn.vove7.common.app.AppConfig

/**
 * 语音唤醒 状态栏快捷设置
 */
@TargetApi(Build.VERSION_CODES.N)
class VoiceWpTileService : SimpleTileService() {
    override val initStatus: Boolean
        get() = AppConfig.voiceWakeup

    override fun onActive(): Boolean {
        AppBus.post(AppBus.ACTION_START_WAKEUP)
        return true
    }

    override fun onInactive(): Boolean {
        AppBus.post(AppBus.ACTION_STOP_WAKEUP)
        return true
    }

    override var activeIcon: Int = R.drawable.ic_hearing
    override var inactiveIcon: Int = R.drawable.ic_unhearing

}
