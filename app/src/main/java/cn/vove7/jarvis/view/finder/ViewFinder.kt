package cn.vove7.jarvis.view.finder

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.vtp.log.Vog

/**
 * 查找符合条件的AccessibilityNodeInfo
 */
abstract class ViewFinder(private val accessibilityService: AccessibilityService) {

    fun findFirst(): AccessibilityNodeInfo? {
        val r = traverseAllNode(accessibilityService.rootInActiveWindow)
        Vog.i(this, "findFirst ${r != null}")
        return r
    }

    val list = mutableListOf<AccessibilityNodeInfo>()

    fun findAll(): List<AccessibilityNodeInfo> {
        list.clear()
        traverseAllNode(accessibilityService.rootInActiveWindow, true)
        val l = mutableListOf<AccessibilityNodeInfo>()
        l.addAll(list)
        return l
    }

    /**
     * 深搜遍历
     */
    // TODO: id:mame 搜索失败
    private fun traverseAllNode(node: AccessibilityNodeInfo?, all: Boolean = false): AccessibilityNodeInfo? {
        if (node == null) return null
        (0 until node.childCount).forEach { index ->
            Vog.v(this, "traverseAllNode ${node.className} $index/${node.childCount}")
            val childNode = node.getChild(index)
            if (childNode != null) {
                if (findCondition(childNode)) {
                    if (all) {
                        list.add(childNode)
                    } else return childNode
                } else {
                    if (all) {
                        traverseAllNode(childNode, true)
                    } else {
                        val r = traverseAllNode(childNode)
                        if (r != null)return r
                    }
                    //深搜
                }
            }
        }
        return null
    }

    /**
     * 查找条件
     */
    abstract fun findCondition(node: AccessibilityNodeInfo): Boolean

}