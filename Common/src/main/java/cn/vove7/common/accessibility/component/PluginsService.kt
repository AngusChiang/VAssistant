package cn.vove7.common.accessibility.component

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
     * @param root AccessibilityNodeInfo  根节点
     * @param eventData Pair<Int,AccessibilityNodeInfo?> first:类型 second:事件节点
     */
    fun onUiUpdate(root: AccessibilityNodeInfo?/*, eventData: Pair<Int,AccessibilityNodeInfo?>*/) {}

    fun onAppChanged(appScope: ActionScope)

    fun onBind() {}
    fun bindService()
    fun onUnBind() {}
    fun unBindServer()
}