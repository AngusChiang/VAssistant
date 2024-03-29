package cn.vove7.common.accessibility.viewnode

import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.GlobalActionExecutor
import cn.vove7.common.utils.ScreenAdapter
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextTransHelper
import java.lang.Thread.sleep

/**
 * 视图节点
 * @property node 无障碍视图节点
 */
class ViewNode(val node: AccessibilityNodeInfo) : ViewOperation, Comparable<ViewNode> {

    /**
     * 文本相似度
     */
    var similarityText: Float = 0f

    private var childrenCache: Array<ViewNode>? = null

    companion object {
        const val tryNum = 10
    }

    override val boundsInParent: Rect
        get() {
            val out = Rect()
            node.getBoundsInParent(out)
            return out
        }


    override val bounds: Rect
        get() {
            val out = Rect()
            node.getBoundsInScreen(out)
            return out
        }


    override val parent: ViewNode?
        get() {
            val it = node.parent
            return if (it != null) {
                ViewNode(it)
            } else null
        }

    override fun tryClick(): Boolean {
        return tryOp(AccessibilityNodeInfo.ACTION_CLICK)
//        if (r) return true
        // global op
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            //获得中心点
//            return globalClick()
//        }
//        return false
    }

    override fun globalClick(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //获得中心点
            val relp = ScreenAdapter.getRelPoint(getCenterPoint())
            return GlobalActionExecutor.click(relp.x, relp.y)
        } else {
            GlobalLog.log("globalClick 需要7.0+")
        }
        return false
    }

    /**
     * 尝试操作次数
     * 点击，长按，选择
     * 尝试点击父级
     */
    private fun tryOp(action: Int): Boolean {
        var p = node
        var i = 0
        while (i < tryNum && !p.performAction(action)) {
            if (p.parent?.also { p = it } == null) {
                Vog.d("尝试->$i p.parent == null")
                return false
            }
            i++
        }
        val b = i != tryNum
        Vog.d("尝试->$i $b")
        return b
    }

    private var lastGetChildTime = 0L
    override val children: Array<ViewNode>
        get() {
            synchronized(lastGetChildTime) {
                val now = System.currentTimeMillis()
                if (childrenCache != null && now - lastGetChildTime < 10000L) {//10s有效期
                    return childrenCache ?: emptyArray()
                }
                lastGetChildTime = now
                val cs = mutableListOf<ViewNode>()
                for (i in 0 until node.childCount) {
                    val c = node.getChild(i)
                    if (c != null) {
                        cs.add(ViewNode(c))
                    }
                }
                return cs.toTypedArray().also { childrenCache = it }
            }
        }

    override fun getChildCount(): Int = node.childCount


    override fun childAt(i: Int): ViewNode? {
        val cn = node.getChild(i)
        if (cn != null) {
            return ViewNode(cn)
        }
        return null
//        val cs = getChilds()
//        if (i < 0 || i >= cs.size) {
//            GlobalLog.err("索引超出范围 $i [0 - ${cs.size}]")
//            return null
//        }
//        return cs[i]
    }

    override fun click(): Boolean = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)

    override fun doubleClick(): Boolean {
        return if (tryClick()) {
            sleep((ViewConfiguration.getDoubleTapTimeout() + 50).toLong())
            tryClick()
        } else false
    }

    override fun tryLongClick(): Boolean {
        return tryOp(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    override fun longClick(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    override fun select(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_SELECT)
    }

    override fun trySelect(): Boolean {
        return tryOp(AccessibilityNodeInfo.ACTION_SELECT)
    }

    override fun scrollUp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id)
        } else {
            GlobalLog.err("scrollUp need SDK M")
            false
        }
    }

    override fun scrollDown(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id)
        } else {
            GlobalLog.err("scrollDown need SDK M")
            false
        }
    }

    override fun scrollForward(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
    }

    override fun scrollBackward(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD.id)
    }

    override fun scrollLeft(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id)
        } else {
            GlobalLog.err("scrollLeft need SDK M")
            false
        }
    }

    override fun scrollRight(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id)
        } else {
            GlobalLog.err("scrollRight need SDK M")
            false
        }
    }

    override var text: CharSequence?
        get() {
            val text = node.text
            Vog.d("$text")
            return text?.toString()
        }
        set(v) {
            val arg = Bundle()
            arg.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, v)
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg)
        }

    override fun desc(): String? {
        return node.contentDescription?.toString()
    }

    override fun appendText(s: CharSequence) {
        text = buildString {
            append(text)
            append(s)
        }
    }

    /**
     * @param ep 额外参数
     */
    override fun setText(text: CharSequence, ep: String?): Boolean {
        val arg = Bundle()
        arg.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, transText(text, ep))
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg)
    }

    override fun setTextWithInitial(text: CharSequence): Boolean {
        return setText(text, "1")
    }

    /**
     * text转变
     */
    private fun transText(text: CharSequence, ep: String?): CharSequence {
        if (ep == null) return text
        return when (ep) {
            "1" -> {//转中文拼音首字母
                TextTransHelper(GlobalApp.APP).chineseStr2Pinyin(text.toString(), true)
            }
            else -> {
                text
            }
        }.also {
            Vog.d("transText $ep $it")
        }
    }

    override fun trySetText(text: CharSequence): Boolean {
        val arg = Bundle()
        arg.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        var p = node
        var i = 0
        while (i < tryNum && !p.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg)) {
            p = node.parent
            i++
        }
        val b = i != tryNum
        Vog.d("尝试-> $b")
        return b
    }

    override fun getCenterPoint(): Point {
        val rect = bounds
        val x = (rect.left + rect.right) / 2
        val y = (rect.top + rect.bottom) / 2
        return Point(x, y)
    }

    override fun swipe(dx: Int, dy: Int, delay: Int): Boolean {
        val c = ScreenAdapter.getRelPoint(getCenterPoint())
        return GlobalActionExecutor.swipe(c.x, c.y, c.x + dx, c.y + dy, delay)
    }

    override fun focus(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
    }

    override fun compareTo(other: ViewNode): Int {
        return ((other.similarityText - similarityText) * 100).toInt()
    }

    override fun toString(): String {
        return nodeSummary(node)
    }

    fun sortOutInfo(): ViewInfo {
        return ViewInfo(
                text,
                node.contentDescription,
                classType,
                boundsInParent,
                bounds
//                node.isClickable,
//                null,
//                node.canOpenPopup()
        )
    }

    val classType: String?
        get() = node.className.let { it.substring(it.lastIndexOf('.') + 1) }


    private fun nodeSummary(node: AccessibilityNodeInfo): String {
        val id = node.viewIdResourceName
        val desc = node.contentDescription

        return "{class: " + classType +
                (if (id == null) "" else ", id: " + id.substring(id.lastIndexOf('/') + 1)) +
                (if (node.text == null) "" else ", text: ${node.text}") +
                (if (desc == null) "" else ", desc: $desc") +
                (", bounds: $bounds" + ", childCount: ${getChildCount()}") +
                (if (node.isClickable) ", Clickable" else "") + '}'
    }

    /**
     * 从该节点搜索
     * @return ViewFindBuilder
     */
    override fun finder(): ViewFindBuilder {
        return ViewFindBuilder(this.node)
    }

    fun isVisibleToUser(): Boolean = node.isVisibleToUser

    override fun isClickable(): Boolean {
        return node.isClickable
    }
}
