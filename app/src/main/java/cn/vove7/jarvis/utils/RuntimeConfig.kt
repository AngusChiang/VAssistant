package cn.vove7.jarvis.utils

import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import kotlin.concurrent.thread

/**
 * # RuntimeConfig
 *
 * @author Administrator
 * 2018/9/16
 */
object RuntimeConfig {
    //key... value
    var vibrateWhenStartReco = true
    var isToastWhenRemoveAd = true
    var isAdBlockService = false

    fun init() {
        thread {
            reload()
        }
    }

    //load
    fun reload() {
        val sp = SpHelper(GlobalApp.APP)
        vibrateWhenStartReco = sp.getBoolean(R.string.key_vibrate_reco_begin, true)
        isToastWhenRemoveAd = sp.getBoolean(R.string.key_show_toast_when_remove_ad, true)
        isAdBlockService = sp.getBoolean(R.string.key_open_ad_block, false)

        Vog.d(this, "reload ---> RuntimeConfig")

    }

    override fun toString(): String {

        return "\nvibrateWhenStartReco: $vibrateWhenStartReco"
    }
}