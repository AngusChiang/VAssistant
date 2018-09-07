package cn.vove7.jarvis.view.statusbar

import android.annotation.SuppressLint
import android.os.Handler
import android.support.v4.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.vtp.notification.ChannelBuilder
import cn.vove7.vtp.notification.NotificationHelper
import cn.vove7.vtp.notification.NotificationIcons

/**
 * # StatusAnimation
 *
 * @author 17719247306
 * 2018/9/2
 */
abstract class StatusAnimation {

    @SuppressLint("NewApi")
    val c = ChannelBuilder.with("StatusBarIcon", "IconAnimation", NotificationManagerCompat.IMPORTANCE_LOW).build()

    abstract var title: String
    abstract var beginAniId: Int
    abstract var failedAniId: Int
    open protected val finishId: Int? = null

    private val notifier = NotificationHelper(GlobalApp.APP, c)

    fun begin() {
        notifier.showNotification(127, title, "", NotificationIcons(beginAniId))
    }

    /**
     * such as play some effect
     */
    open fun onFailed() {}

    fun failed() {
        notifier.showNotification(127, title, "", NotificationIcons(failedAniId))
        onFailed()
        Handler().postDelayed({
            hide()
        }, 2000)
    }

    open fun finish() {
        if(finishId==null) return
        notifier.showNotification(127, title, "", NotificationIcons(finishId!!))
        Handler().postDelayed({
            hide()
        }, 2000)
    }


    fun hide() {
        notifier.removeAll()
    }
}