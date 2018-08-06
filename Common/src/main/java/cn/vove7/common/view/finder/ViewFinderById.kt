package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi

/**
 * # ViewFinderById
 *
 * Created by Vove on 2018/7/6
 */
class ViewFinderById : ViewFinder {
    lateinit var viewId: String

    constructor(accessibilityService: AccessibilityApi, id: String) : super(accessibilityService) {
        this.viewId = id
    }

    constructor(accessibilityService: AccessibilityApi) : super(accessibilityService)


    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        return if (node.viewIdResourceName != null) {
            node.viewIdResourceName.endsWith("/$viewId")// :id/view_id
        } else false
    }
}