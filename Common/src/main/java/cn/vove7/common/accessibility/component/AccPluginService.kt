package cn.vove7.common.accessibility.component

import cn.vove7.common.datamanager.parse.model.ActionScope

/**
 * # AccPluginService
 * 无障碍 插件服务
 * @author 17719247306
 * 2018/9/3
 */
interface AccPluginService {
    /**
     * 不再分发此事件
     * 界面更新事件
     * @param root AccessibilityNodeInfo  根节点
     * @param eventData Pair<Int,AccessibilityNodeInfo?> first:类型 second:事件节点
     */
//    fun onUiUpdate(root: AccessibilityNodeInfo?/*, eventData: Pair<Int,AccessibilityNodeInfo?>*/) {}

    /**
     * 界面改变事件
     * @param appScope ActionScope
     */
    fun onAppChanged(appScope: ActionScope)

    fun onBind() {}
    fun bindService()
    fun onUnBind() {}
    fun unBindServer()
}