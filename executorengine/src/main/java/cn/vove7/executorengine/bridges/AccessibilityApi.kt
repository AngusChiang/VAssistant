package cn.vove7.executorengine.bridges

import android.accessibilityservice.AccessibilityService
import cn.vove7.datamanager.parse.model.ActionScope
import cn.vove7.executorengine.Executor
import cn.vove7.executorengine.model.ViewNode

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
abstract class AccessibilityApi : AccessibilityService(), AccessibilityOperation {
    val currentScope = ActionScope()
}
interface AccessibilityOperation {
    fun findFirstNodeById(id: String): ViewNode?
    fun findFirstNodeByDesc(desc: String): ViewNode?
    fun findFirstNodeByIdAndText(id: String, text: String): ViewNode?
    fun findNodeById(id: String): List<ViewNode>
    fun findFirstNodeByText(text: String): ViewNode?
    fun findFirstNodeByTextWhitFuzzy(text: String): ViewNode?

    fun findNodeByText(text: String): List<ViewNode>

    fun waitForActivity(executor: Executor, pkg: String, activityName: String?)
    /**
     * 等待界面出现指定ViewId
     * 特殊标记
     */
    fun waitForAppViewId(executor: Executor, pkg: String, viewId: String)

    /**
     * 等待出现指定ViewId
     * 特殊标记
     */
    fun waitForViewId(executor: Executor, viewId: String)

    /**
     * 等待出现指定View Desc
     */
    fun waitForViewDesc(executor: Executor, desc: String)
    /**
     * 等待出现指定View Text
     */
    fun waitForViewText(executor: Executor, text: String)

    fun removeAllNotifier(executor: Executor)

    /**
     * 自动查找
     */
    fun autoFindByText()

    /**
     * 直到找到
     */
    fun utilFindById()

    /**
     * 一直返回，直到这个Activity
     */
    fun backUtilActivity()

    /**
     * 一直上滑，直到出现
     */
    fun scrollUpUtilFind()


}


