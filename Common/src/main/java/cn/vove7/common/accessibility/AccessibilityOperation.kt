package cn.vove7.common.accessibility

import cn.vove7.common.accessibility.viewnode.ViewNode

interface AccessibilityOperation {
    fun findFirstNodeById(id: String): ViewNode?
    fun findFirstNodeByDesc(desc: String): ViewNode?
    fun findFirstNodeByIdAndText(id: String, text: String): ViewNode?
    fun findNodeById(id: String): List<ViewNode>
    fun findFirstNodeByText(text: String): ViewNode?
    fun findFirstNodeByTextWhitFuzzy(text: String): ViewNode?

    fun findNodeByText(text: String): List<ViewNode>

    fun getRootViewNode(): ViewNode?

}
