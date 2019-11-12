package cn.vove7.jarvis.plugins

import cn.vove7.smartkey.AConfig
import cn.vove7.smartkey.annotation.Config
import cn.vove7.smartkey.key.noCacheKey

/**
 * # PluginConfig
 *
 * @author Vove
 * 2019/11/8
 */
@Config("plugin")
object PluginConfig : AConfig() {

    val onFullText by noCacheKey("吃饱了 吃饱了", "full_power_hint_text")
    val onLowText by noCacheKey("饿死了 饿死了", "low_power_hint_text")
    val onChargingText by noCacheKey("吃饭了 吃饭了", "charging_hint_text")

}