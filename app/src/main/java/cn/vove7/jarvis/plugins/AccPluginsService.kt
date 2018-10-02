package cn.vove7.jarvis.plugins

import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.services.PluginsService
import cn.vove7.vtp.log.Vog

/**
 * # AccPluginsService
 *
 * @author 17719247306
 * 2018/9/3
 */

abstract class AccPluginsService : PluginsService {

    var opened = false
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
        MyAccessibilityService.unregisterEvent(this)
        MyAccessibilityService.registerEvent(this)
//        unBindServer()
//        bindService()
    }

}