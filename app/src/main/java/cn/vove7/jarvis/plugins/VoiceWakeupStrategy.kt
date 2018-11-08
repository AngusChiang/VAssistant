package cn.vove7.jarvis.plugins

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.utils.hasMicroPermission
import cn.vove7.common.utils.microPermissionCache
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.statusbar.MicroToggleAnimation
import cn.vove7.jarvis.view.statusbar.StatusAnimation
import cn.vove7.vtp.log.Vog

/**
 * # VoiceWakeupStrategy
 * 解决唤醒冲突 麦克风占用
 *
 * 情景：
 * case1: 亮屏自动开启语音唤醒 / 在微信内
 *
 * @author Administrator
 * 9/21/2018
 */
object VoiceWakeupStrategy : AccPluginsService() {
    private val statusAni: StatusAnimation by lazy { MicroToggleAnimation() }


    override fun onUiUpdate(root: AccessibilityNodeInfo?) {}

    //是否关闭唤醒 by VoiceWakeupStrategy
    var closed = false

    override fun onAppChanged(appScope: ActionScope) {//
        val appInfo = AdvanAppHelper.getAppInfo(appScope.packageName) ?: return
        if (appInfo.hasMicroPermission()) {//有麦克风权限的App
            //case 1 进入App 自动休眠 ->  ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY
            //wakeupI?.opened开启时，在内关闭唤醒
            if (MainService.instance?.speechRecoService?.wakeupI?.opened == true) {
                MainService.instance?.onCommand(AppBus.ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY)
                Vog.d(this, "VoiceWakeupStrategy ---> 关闭语音唤醒")
                closed = true
                if (AppConfig.notifyCloseMico) {
                    statusAni.failedAndHideDelay("关闭语音唤醒", 2000)
                }
            }
        } else if (closed && MainService.instance?.speechRecoService?.timerEnd == false) {//已自动关闭 并且定时器有效
            Vog.d(this, "VoiceWakeupStrategy ---> 开启语音唤醒")
            closed = false

            if (AppConfig.notifyCloseMico) {
                statusAni.begin()
                statusAni.hideDelay(2000)
            }
            MainService.instance?.onCommand(AppBus.ORDER_START_VOICE_WAKEUP_WITHOUT_NOTIFY)
        }
    }

    override fun unBindServer() {
        super.unBindServer()
        microPermissionCache.clear()
    }

    /**
     * 充电/亮屏自动开启唤醒 前 判断当前App
     *
     * @return Boolean
     */
    fun canOpenRecord(): Boolean {
        return AccessibilityApi.accessibilityService?.currentAppInfo?.let {
            //开启无障碍 并且开启麦克风冲突 and 当前有麦克风权限
            if (!AppConfig.fixVoiceMico || !it.hasMicroPermission()) {
                true
            } else {
                if (opened) {//通知 开启定时器
                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP_TIMER)
                    closed = true //设置标志
                }
                Vog.d(this, "canOpenRecord ---> 在有麦克风权限的App内/不打开唤醒")
                false
            }
        } ?: true
    }
}