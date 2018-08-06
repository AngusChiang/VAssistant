package cn.vove7.jarvis.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.vtp.log.Vog

/**
 *
 *
 * Created by Vove on 2018/7/5
 */
class ScrollableViewFinder(accessibilityService: AccessibilityApi) : ViewFinder(accessibilityService) {
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        Vog.d(this, "findCondition ${node.className}")

        return node.isScrollable
    }
}