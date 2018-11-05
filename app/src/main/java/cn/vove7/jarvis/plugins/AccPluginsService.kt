package cn.vove7.jarvis.plugins

import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.services.PluginsService
import cn.vove7.vtp.log.Vog

/**
 * # AccPluginsService
 * 注册方式：MyAccessibilityService.registerPlugin(AccPluginsService)
 * @author 17719247306
 * 2018/9/3
 */

abstract class AccPluginsService : PluginsService {

    var opened = false

    /**
     * 注册
     */
    fun register() {
        MyAccessibilityService.registerPlugin(this)
    }

    /**
     * 取消注册
     */
    fun unregister() {
        MyAccessibilityService.unregisterPlugin(this)
    }

    /**
     * 不可主动调用
     * 注册方式：MyAccessibilityService.registerPlugin(AccPluginsService)
     */
    override fun bindService() {
        Vog.d(this, "bindService ---> $this")
        onBind()
        opened = true
    }

    override fun unBindServer() {
        Vog.d(this, "unBindServer ---> $this")
        onUnBind()
        opened = false
    }

    fun restart() {
        MyAccessibilityService.unregisterPlugin(this)
        MyAccessibilityService.registerPlugin(this)
//        unBindServer()
//        bindService()
    }

}