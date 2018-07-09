package cn.vove7.jarvis.view.finder

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 查找编辑框
 *
 * Created by Vove on 2018/7/5
 */
class EditableViewFinder(accessibilityService: AccessibilityService) : ViewFinder(accessibilityService) {
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        return node.isEditable
    }

}