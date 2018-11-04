package cn.vove7.jarvis.plugins

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.common.netacc.NetHelper

/**
 * # VoiceWakeupStrategy
 * todo 后台通话？
 * @author Administrator
 * 2018/10/9
 */
class VoiceWakeupStrategy : AccPluginsService() {
    val list = mutableListOf<ActionScope>()
    fun buildList() {//Net
        list.clear()
        NetHelper.postJson<List<ActionScope>>(ApiUrls.CLOUD_PARSE, BaseRequestModel<Any>()) { _, b ->
            val l = b?.data
            if (b?.isOk() == true && l != null) {
                list.addAll(l)
            } else {
                GlobalApp.toastShort("唤醒冲突数据初始化失败")
            }
        }
    }

    override fun onBind() {
        buildList()
    }

    override fun onUnBind() {
        list.clear()
    }

    override fun onUiUpdate(root: AccessibilityNodeInfo?) {

    }

    override fun onAppChanged(appScope: ActionScope) {
        if (AppConfig.voiceWakeup) {
            AppBus.post(SpeechAction.ActionCode.ACTION_STOP_WAKEUP_WITHOUT_SWITCH)
        }
    }
}