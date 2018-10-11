package cn.vove7.jarvis.services

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.datamanager.parse.model.ActionScope

/**
 * # PluginsService
 *
 * @author 17719247306
 * 2018/9/3
 */
interface PluginsService {
    /**
     * 界面更新事件
     * @param root AccessibilityNodeInfo
     */
    fun onUiUpdate(root: AccessibilityNodeInfo?)

    fun onAppChanged(appScope: ActionScope)

    fun onBind() {}
    fun bindService()
    fun onUnBind() {}
    fun unBindServer()
}