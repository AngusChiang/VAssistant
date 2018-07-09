package cn.vove7.jarvis.view.finder

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.vtp.log.Vog

/**
 * # ViewFinderById
 *
 * Created by Vove on 2018/7/6
 */
class ViewFinderById : ViewFinder {
    lateinit var viewId: String

    constructor(accessibilityService: AccessibilityService, id: String) : super(accessibilityService) {
        this.viewId = id
    }

    constructor(accessibilityService: AccessibilityService) : super(accessibilityService)


    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        return if (node.viewIdResourceName != null) {
            node.viewIdResourceName.endsWith("/$viewId")// :id/view_id
        }
        else false
    }
}