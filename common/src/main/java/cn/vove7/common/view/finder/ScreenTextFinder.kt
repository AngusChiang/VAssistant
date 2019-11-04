package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo

/**
 * # ScreenTextFinder
 *
 * @author Administrator
 * 2018/10/14
 */
class ScreenTextFinder(startNode: AccessibilityNodeInfo? = null) : ViewFinder(startNode) {
    var isWeb = false
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        if (node.className?.endsWith("WebView", ignoreCase = true) == true) {
            isWeb = true
            return false
        }
        return node.childCount == 0 && (node.text != null && node.text.trim() != "")
                || (isWeb && node.contentDescription ?: "" != "")
    }

}