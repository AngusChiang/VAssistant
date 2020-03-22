package cn.vove7.jarvis.plugins

import cn.vove7.common.R
import cn.vove7.smartkey.AConfig
import cn.vove7.smartkey.android.noCacheKey
import cn.vove7.smartkey.annotation.Config
import cn.vove7.smartkey.key.noCacheKey
import cn.vove7.smartkey.key.smartKey

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

    val adWaitSecs by noCacheKey(17, keyId = R.string.key_ad_wait_secs)
    var smartKillAd by noCacheKey(false, keyId = R.string.key_smart_find_and_kill_ad) // 跳过自动识别未标记的广告
    var isToastWhenRemoveAd by noCacheKey(true, keyId = R.string.key_show_toast_when_remove_ad)

    var rokidInTimeSendLocation by noCacheKey(false, R.string.key_rokid_send_loc)

    var rokidHomeLoc :Pair<Double, Double>? by smartKey(null, "rokid_home_loc")
}