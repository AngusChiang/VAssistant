package cn.vove7.accessibilityservicedemo.model

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.accessibilityservicedemo.utils.NormalOperation
import cn.vove7.vtp.log.Vog

class ViewNode(val node: AccessibilityNodeInfo) : NormalOperation {

    override fun click(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    override fun longClick(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    override fun select(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_SELECT)
    }

    override fun scrollUp(): Boolean {
        val arg = Bundle()


        return false
    }

    override fun scrollDown(): Boolean {
        return false
    }

    override fun getText(): String? {
        val text = node.text
        Vog.d(this, "$text")
        return text as String?
    }

    override fun setText(text: String): Boolean {
        val arg = Bundle()
        arg.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arg)
    }

    override fun focus(): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
    }
}