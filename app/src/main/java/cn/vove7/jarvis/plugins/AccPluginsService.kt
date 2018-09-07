package cn.vove7.jarvis.plugins

import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.services.OnAccessibilityEvent

/**
 * # AccPluginsService
 *
 * @author 17719247306
 * 2018/9/3
 */

abstract class AccPluginsService : OnAccessibilityEvent {

    fun bindServer() {
        MyAccessibilityService.registerEvent(this)
    }

}