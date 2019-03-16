package cn.vove7.common.accessibility.component

import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.vtp.log.Vog

/**
 * # AbsAccPluginService
 * 注册方式：MyAccessibilityService.registerPlugin(AbsAccPluginService)
 * @author 17719247306
 * 2018/9/3
 */

abstract class AbsAccPluginService : AccPluginService {

    var opened = false

    /**
     * 注册
     */
    fun register() {
        AccessibilityApi.registerPlugin(this)
    }

    /**
     * 取消注册
     */
    fun unregister() {
        AccessibilityApi.unregisterPlugin(this)
    }

    /**
     * 不可主动调用
     * 注册方式：MyAccessibilityService.registerPlugin(AbsAccPluginService)
     */
    override fun bindService() {
        Vog.d("bindService ---> $this")
        onBind()
        opened = true
    }

    override fun unBindServer() {
        Vog.d("unBindServer ---> $this")
        onUnBind()
        opened = false
    }

    fun restart() {
        unregister()
        register()
//        unBindServer()
//        bindService()
    }

}