package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.vtp.log.Vog

/**
 * 查找符合条件的AccessibilityNodeInfo
 */
abstract class ViewFinder(var accessibilityService: AccessibilityApi) {
    val rootNode: AccessibilityNodeInfo?
        get() {
            return try {
                accessibilityService.rootInActiveWindow
            } catch (e: Exception) {
                null
            }
        }

    open fun findFirst(): ViewNode? {
        return findFirst(false)
    }

    /**
     * @param includeInvisible Boolean 是否包含不可见
     * @return ViewNode?
     */
    fun findFirst(includeInvisible: Boolean = false): ViewNode? {
        //不可见
        val r = traverseAllNode(rootNode, includeInvisible = includeInvisible)
        Vog.i(this, "findFirst ${r != null}")
        return r
    }

    val list = mutableListOf<ViewNode>()

    fun find(): Array<ViewNode> {
        return findAll()
    }

    fun findAll(): Array<ViewNode> {
        return findAll(false)
    }

    fun findAll(includeInvisible: Boolean = false): Array<ViewNode> {
        list.clear()
        traverseAllNode(rootNode, true, includeInvisible)
        val l = mutableListOf<ViewNode>()
        l.addAll(list)
        return l.toTypedArray()
    }

    /**
     * 深搜遍历
     *
     * @param node AccessibilityNodeInfo?
     * @param all Boolean true 搜索全部返回list else return first
     * @param includeInvisible Boolean 包含不可见节点
     * @return ViewNode?
     */
    private fun traverseAllNode(node: AccessibilityNodeInfo?, all: Boolean = false,
                                includeInvisible: Boolean = false): ViewNode? {
        if (node == null) return null
        (0 until node.childCount).forEach { index ->
            Vog.v(this, "traverseAllNode ${node.className} $index/${node.childCount}")
            val childNode = node.getChild(index)
            if (childNode != null) {
                if (!includeInvisible && !childNode.isVisibleToUser) {//TODO check it
                    Vog.d(this, "unVisibleToUser ---> ${childNode.text}")
                    return@forEach
                }
                if (findCondition(childNode)) {
                    if (all) {
                        list.add(ViewNode(childNode))
                    } else return ViewNode(childNode)
                } else {
                    if (all) {
                        traverseAllNode(childNode, true)
                    } else {
                        val r = traverseAllNode(childNode)
                        if (r != null) return r
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