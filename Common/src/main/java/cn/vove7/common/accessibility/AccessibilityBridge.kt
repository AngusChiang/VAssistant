package cn.vove7.common.accessibility

import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.view.finder.ViewFinder

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


