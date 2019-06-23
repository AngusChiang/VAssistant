package cn.vove7.jarvis.plugins

import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.component.AbsAccPluginService
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.utils.hasMicroPermission
import cn.vove7.jarvis.services.MainService
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.view.statusbar.StatusAnimation
import cn.vove7.jarvis.view.statusbar.MicroToggleAnimation
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
object VoiceWakeupStrategy : AbsAccPluginService() {
    private val statusAni: StatusAnimation by lazy { MicroToggleAnimation() }

    //是否关闭唤醒 by VoiceWakeupStrategy
    var closed = false

    var lastPkg = ""
    override fun onAppChanged(appScope: ActionScope) {//
        if (MainService.wpTimerEnd) {
            Vog.d("已定时关闭")
            closed = false
            return
        }
        if (lastPkg == appScope.packageName) return
        val appInfo = AdvanAppHelper.getAppInfo(appScope.packageName) ?: return
        lastPkg = appScope.packageName
        if (appInfo.hasMicroPermission()) {//有麦克风权限的App
            //case 1 进入App 自动休眠 ->  ACTION_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY
            //wakeupI?.opened开启时，在内关闭唤醒
            if (MainService.instance?.speechRecogService?.wakeupI?.opened == true) {
                closeWakeup()
            }
        } else if (closed && AppConfig.voiceWakeup) {//已自动关闭 并且定时器有效
            startWakeup()
        }
    }

    fun closeWakeup() {
        if(!AppConfig.voiceWakeup) return
        MainService.instance?.onCommand(AppBus.ACTION_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY)
        Vog.d("VoiceWakeupStrategy ---> 关闭语音唤醒")
        closed = true
        if (AppConfig.notifyCloseMicro) {
            statusAni.failedAndHideDelay("关闭语音唤醒", 2000)
        }
    }

    override fun onUnBind() {
        closed = false//关闭 / 初始标志
    }

    fun startWakeup() {
        if(!AppConfig.voiceWakeup) return
        Vog.d("VoiceWakeupStrategy ---> 开启语音唤醒")
        closed = false

        if (AppConfig.notifyCloseMicro) {
            statusAni.begin()
            statusAni.hideDelay(2000)
        }
        MainService.instance?.onCommand(AppBus.ACTION_START_VOICE_WAKEUP_WITHOUT_NOTIFY)
    }


    /**
     * 充电/亮屏自动开启唤醒 前 判断当前App
     *
     * @return Boolean
     */
    fun canOpenRecord(): Boolean {
        return AccessibilityApi.accessibilityService?.currentAppInfo?.let {
            //开启无障碍 并且开启麦克风冲突 and 当前有麦克风权限
            if (!AppConfig.fixVoiceMicro || !it.hasMicroPermission()) {
                true
            } else {
                if (opened) {//通知 开启定时器
                    AppBus.post(AppBus.ACTION_START_WAKEUP_TIMER)
                    closed = true //设置标志
                }
                Vog.d("canOpenRecord ---> 在有麦克风权限的App内/不打开唤醒")
                false
            }
        } ?: true
    }
}