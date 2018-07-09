package cn.vove7.jarvis.view.finder

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo

/**
 *
 *
 * Created by Vove on 2018/7/5
 */
class ViewFinderByDesc : ViewFinder {

    lateinit var desc: String

    constructor(accessibilityService: AccessibilityService, desc: String) : super(accessibilityService) {
        this.desc = desc
    }

    constructor(accessibilityService: AccessibilityService) : super(accessibilityService)

    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        val d = node.contentDescription ?: ""
        return desc == d
    }
}