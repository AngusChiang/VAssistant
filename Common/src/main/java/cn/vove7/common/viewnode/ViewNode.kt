package cn.vove7.common.viewnode

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper
import java.lang.Thread.sleep

/**
 * 视图节点
 */
class ViewNode(val node: AccessibilityNodeInfo) : ViewOperation, Comparable<ViewNode> {

    /**
     * 文本相似度
     */
    var similarityText: Float = 0f

    companion object {
        const val tryNum = 10
    }

    override fun tryClick(): Boolean = tryOp(AccessibilityNodeInfo.ACTION_CLICK)

    /**
     * 尝试操作次数
     * 点击，长按，选择
     * 尝试点击父级
     */
    private fun tryOp(action: Int): Boolean {
        var p = node
        var i = 0
        while (i < tryNum && !p.performAction(action)) {
            if (p.parent == null) {
                Vog.d(this, "尝试->$i p.parent == null")
                return false
            }
            p = p.parent
            i++
        }
        val b = i != tryNum
        Vog.d(this, "尝试->$i $b")
        return b

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun scrollUp(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun scrollDown(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun scrollForward(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun scrollBackward(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD.id)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun scrollLeft(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun scrollRight(): Boolean {
        return node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id)
    }

    override fun getText(): String? {
        val text = node.text
        Vog.d(this, "$text")
        return text as String?
    }

    /**
     * @param ep 额外参数
     */
    override fun setText(text: String, ep: String?): Boolean {
        val arg = Bundle()
        arg.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, transText(text, ep))
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg)
    }

    override fun setText(text: String): Boolean {
        val arg = Bundle()
        arg.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg)
    }

    /**
     * text转变
     */
    private fun transText(text: String, ep: String?): String {
        if (ep == null) return text
        return when (ep) {
            "1" -> {//转中文拼音首字母
                TextHelper.chineseStr2Pinyin(text, true)
            }
            else -> {
                text
            }
        }
    }

    override fun trySetText(text: String): Boolean {
        val arg = Bundle()
        arg.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        var p = node
        var i = 0
        while (i < tryNum && !p.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg)) {
            p = node.parent
            i++
        }
        val b = i != tryNum
        Vog.d(this, "尝试-> $b")
        return b
    }

    override fun focus(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
    }

    override fun compareTo(other: ViewNode): Int {
        return ((other.similarityText - similarityText) * 100).toInt()
    }

    override fun await() {

    }
}
