package cn.vove7.jarvis.services

import android.annotation.TargetApi
import android.os.Build
import cn.vove7.common.app.AppConfig
import cn.vove7.common.appbus.AppBus
import cn.vove7.jarvis.R

/**
 * 语音唤醒 状态栏快捷设置
 */
@TargetApi(Build.VERSION_CODES.N)
class VoiceWpTileService : SimpleTileService(), TileLongClickable {
    override var toggleState: Boolean
        get() = AppConfig.voiceWakeup
        set(value) {}

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

    override fun onLongClick() {
        onClick()
    }

    override var activeIcon: Int = R.drawable.ic_hearing
    override var inactiveIcon: Int = R.drawable.ic_unhearing

}
