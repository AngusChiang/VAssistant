package cn.vove7.common.accessibility

import cn.vove7.common.view.finder.ViewShowListener
import cn.vove7.common.viewnode.ViewNode

interface AccessibilityOperation {
    fun findFirstNodeById(id: String): ViewNode?
    fun findFirstNodeByDesc(desc: String): ViewNode?
    fun findFirstNodeByIdAndText(id: String, text: String): ViewNode?
    fun findNodeById(id: String): List<ViewNode>
    fun findFirstNodeByText(text: String): ViewNode?
    fun findFirstNodeByTextWhitFuzzy(text: String): ViewNode?

    fun findNodeByText(text: String): List<ViewNode>

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
//    fun removeAllNotifier(finderNotify: ViewShowListener)
}
