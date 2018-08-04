package cn.vove7.androlua.androbridge

import cn.vove7.executorengine.bridges.AccessibilityApi
import cn.vove7.executorengine.bridges.GlobalActionAutomator
import cn.vove7.executorengine.bridges.ServiceBridge
import cn.vove7.executorengine.bridges.SystemBridge

/**
 * # BridgeManager
 *
 * @author Vove
 * 2018/8/4
 */
data class BridgeManager(
        var accessibilityApi: AccessibilityApi
        , var automator: GlobalActionAutomator
        , var serviceBridge: ServiceBridge
        , var systemBridge: SystemBridge
)