package cn.vove7.jarvis.view.finder

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.vtp.log.Vog

/**
 *
 *
 * Created by Vove on 2018/7/5
 */
class ScrollableViewFinder(accessibilityService: AccessibilityService) : ViewFinder(accessibilityService) {
    override fun findCondition(node: AccessibilityNodeInfo): Boolean{
        Vog.d(this,"findCondition ${node.className}")

        return node.isScrollable
    }
}