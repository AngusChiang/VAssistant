package cn.vove7.jarvis.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import cn.vove7.common.accessibility.AccessibilityApi

/**
 * # GestureService
 * 执行手势无障碍服务
 * 解决卡顿
 * @author Administrator
 * 2018/12/18
 */
class GestureService : AccessibilityApi() {
    override fun onInterrupt() {
    }

    override fun onCreate() {
        grstureService = this
        super.onCreate()
    }

    override fun onDestroy() {
        grstureService = null
        super.onDestroy()
    }

    override fun getService(): AccessibilityService = this

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }
}