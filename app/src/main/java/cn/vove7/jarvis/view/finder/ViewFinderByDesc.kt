package cn.vove7.jarvis.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.view.finder.ViewFinder

/**
 *
 *
 * Created by Vove on 2018/7/5
 */
class ViewFinderByDesc : ViewFinder {

    lateinit var desc: String

    constructor(accessibilityService: AccessibilityApi, desc: String) : super(accessibilityService) {
        this.desc = desc
    }

    constructor(accessibilityService: AccessibilityApi) : super(accessibilityService)

    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        val d = node.contentDescription ?: ""
        return desc == d
    }
}