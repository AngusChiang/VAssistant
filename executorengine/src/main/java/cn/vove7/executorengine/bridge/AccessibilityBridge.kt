package cn.vove7.executorengine.bridge

import android.accessibilityservice.AccessibilityService
import cn.vove7.executorengine.model.ViewNode

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
abstract class AccessibilityBridge : AccessibilityService(), AccessibilityOperation

interface AccessibilityOperation {
    fun findFirstNodeById(id: String): ViewNode?
    fun findNodeById(id: String): List<ViewNode>
    fun findFirstNodeByText(text: String): ViewNode?
    fun findNodeByText(text: String): List<ViewNode>

    fun back(): Boolean
    fun recentInterface(): Boolean
    fun home(): Boolean
    fun showNotification(): Boolean
}


