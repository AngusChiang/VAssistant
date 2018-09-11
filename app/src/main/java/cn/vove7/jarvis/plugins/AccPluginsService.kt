package cn.vove7.jarvis.plugins

import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.services.OnAccessibilityEvent
import cn.vove7.vtp.log.Vog

/**
 * # AccPluginsService
 *
 * @author 17719247306
 * 2018/9/3
 */

abstract class AccPluginsService : OnAccessibilityEvent {

    fun bindServer() {
        Vog.d(this,"bindServer ---> $this")
        MyAccessibilityService.registerEvent(this)
    }
    fun unBindServer() {
        Vog.d(this,"unBindServer ---> $this")
        MyAccessibilityService.unregisterEvent(this)
    }

}