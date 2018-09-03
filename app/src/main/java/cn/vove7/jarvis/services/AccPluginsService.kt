package cn.vove7.jarvis.services

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