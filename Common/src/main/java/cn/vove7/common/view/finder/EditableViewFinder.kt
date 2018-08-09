package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.view.finder.ViewFinder

/**
 * 查找编辑框
 *
 * Created by Vove on 2018/7/5
 */
class EditableViewFinder(accessibilityService: AccessibilityApi) : ViewFinder(accessibilityService) {
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        return node.isEditable
    }

}