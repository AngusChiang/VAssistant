package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.BuildConfig
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.vtp.log.Vog

/**
 * 查找符合条件的AccessibilityNodeInfo
 */
abstract class ViewFinder(var node: AccessibilityNodeInfo?) {

    val startNode
        get() = node ?: AccessibilityApi
            .requireAccessibility(jump = true).rootInWindow

    open fun findFirst(): ViewNode? {
        return findFirst(false)
    }

    /**
     * 等待View出现 同步 耗时操作
     * 主动搜索
     * @param m Long 时限
     */
    fun waitFor(m: Long = 30000): ViewNode? {
        val t = when {
            m in 0..30000 -> m
            m < 0 -> 0
            else -> 30000
        }
        val beginTime = System.currentTimeMillis()
        var sc = 0
        val ct = Thread.currentThread()
        Vog.d("搜索线程 ---> $ct ${ct.hashCode()}")
        val endTime = beginTime + t
        while (System.currentTimeMillis() < endTime &&
            !ct.isInterrupted) {
            val node = findFirst()
            if (node != null) {
                Vog.d("waitFor ---> 搜索到 $node")
                return node
            } else {
                sc++
                if (sc % 100 == 0)
                    Vog.d("waitFor ---> 搜索次数 $sc 打断: ${ct.isInterrupted}")
            }
        }
        Vog.d("搜索超时${System.currentTimeMillis() - beginTime}/$m or 中断${ct.isInterrupted}")
        return null
    }

    /**
     * @param includeInvisible Boolean 是否包含不可见
     * @return ViewNode?
     */
    fun findFirst(includeInvisible: Boolean = false): ViewNode? {
        //不可见
        val r = traverseAllNode(startNode, includeInvisible = includeInvisible)
        Vog.v("findFirst ${r != null}")
        return r
    }

    val list = mutableListOf<ViewNode>()

    fun find(): Array<ViewNode> {
        return findAll()
    }

    @JvmOverloads
    fun findAll(includeInvisible: Boolean = false): Array<ViewNode> {
        list.clear()
        traverseAllNode(startNode, true, includeInvisible)
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
                                includeInvisible: Boolean = false, depth: Int = 0): ViewNode? {
        if (node == null) return null
        if (depth > 50) {//防止出现无限递归（eg:QQ浏览器首页）
            Vog.d("traverseAllNode ---> 超过最大深度")
            return null
        }
        (0 until node.childCount).forEach { index ->
            if (BuildConfig.DEBUG) {
                Vog.v("traverseAllNode ${node.className} $index/${node.childCount}")
            }
            val childNode = node.getChild(index)
            if (childNode != null) {
                if (BuildConfig.DEBUG) {
                    Vog.v("child[$index]: ${childNode.text}")
                }
                if (!includeInvisible && !childNode.isVisibleToUser) {
                    Vog.v("unVisibleToUser ---> ${childNode.text}")
                    return@forEach
                }
                if (findCondition(childNode)) {
                    if (all) {
                        list.add(ViewNode(childNode))
                    } else return ViewNode(childNode)
                } else {
                    if (all) {
                        traverseAllNode(childNode, true, includeInvisible, depth = depth + 1)
                    } else {
                        val r = traverseAllNode(childNode, includeInvisible, depth = depth + 1)
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