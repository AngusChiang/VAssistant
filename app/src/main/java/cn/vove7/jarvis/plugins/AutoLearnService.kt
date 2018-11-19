package cn.vove7.jarvis.plugins

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.component.AccPluginsService
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.vtp.log.Vog

/**
 * # AutoLearnService
 * 自学习
 * @author Administrator
 * 2018/11/12
 */
object AutoLearnService : AccPluginsService() {
    override fun onUiUpdate(root: AccessibilityNodeInfo?) {
        //解析record
//        eventData.apply {
//            if (first == AccessibilityEvent.TYPE_VIEW_CLICKED)
//                lastEventNodeInfo = second
//
//            Vog.d(this, "onUiUpdate ---> ${nodeSummary(this.second)}")
//        }
    }

    var lastEventNodeInfo: AccessibilityNodeInfo? = null

    var lastPkg: String? = null
    var lastScope: ActionScope? = null
    override fun onAppChanged(appScope: ActionScope) {

        Vog.d(this, "onAppChanged ---> $appScope")
        if (appScope.packageName == lastPkg) {//app内切换
            Vog.d(this, "onAppChanged ---> app内切换")

        } else {//切换App
            lastPkg = appScope.packageName
            Vog.d(this, "onAppChanged ---> 切换应用")
        }
        lastScope = appScope
    }
}