package cn.vove7.jarvis.plugins

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.datamanager.parse.model.ActionScope

/**
 * # WakeUpListener
 * 解决唤醒冲突 麦克风占用
 * @author Administrator
 * 9/21/2018
 */
class WakeUpListener : AccPluginsService() {
    override fun onUiUpdate(root: AccessibilityNodeInfo?) {}

    override fun onAppChanged(appScope: ActionScope) {//


    }
}