package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi

/**
 * # ScreenTextFinder
 *
 * @author Administrator
 * 2018/10/14
 */
class ScreenTextFinder(startNode: AccessibilityNodeInfo?) : ViewFinder(startNode) {
    var isWeb = false
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        if (node.className.endsWith("WebView", ignoreCase = true)) {
            isWeb = true
            return false
        }
        return node.childCount == 0 && (node.text != null && node.text.trim() != "")
                || (isWeb && node.contentDescription ?: "" != "")
    }

}