package cn.vove7.common.accessibility

import android.accessibilityservice.AccessibilityService
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.vtp.app.AppInfo

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
abstract class AccessibilityApi : AccessibilityService(),
        AccessibilityBridge {
    abstract fun getService(): AccessibilityService

    val currentScope = ActionScope()
    var currentActivity: String = ""
        protected set

    var currentAppInfo: AppInfo? = null
        protected set

    companion object {
        var accessibilityService: AccessibilityApi? = null
        fun isOpen(): Boolean {
            return accessibilityService != null
        }
    }

}

interface AccessibilityBridge {
    /**
     * 等待出现指定View  with /id/text/desc
     * 特殊标记
     */
    fun waitForView(executor: CExecutorI, finder: ViewFinder)

    fun getRootViewNode(): ViewNode?
    fun waitForActivity(executor: CExecutorI, scope: ActionScope)
    /**
     * remove all notifier when was interrupted
     */
    fun removeAllNotifier(executor: CExecutorI)
}


