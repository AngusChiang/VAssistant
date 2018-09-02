package cn.vove7.jarvis.view.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.support.v4.app.NotificationManagerCompat
import cn.vove7.vtp.notification.ChannelBuilder
import cn.vove7.vtp.notification.NotificationHelper
import cn.vove7.vtp.notification.NotificationIcons

/**
 * # StatusAnimation
 *
 * @author 17719247306
 * 2018/9/2
 */
abstract class StatusAnimation(val context: Context) {

    @SuppressLint("NewApi")
    val c = ChannelBuilder.with("StatusBarIcon", "IconAnimation", NotificationManagerCompat.IMPORTANCE_LOW).build()

    protected abstract val title: String

    protected abstract val beginAniId: Int
    protected abstract val failedAniId: Int

    private val notifier = NotificationHelper(context, c)

    fun begin() {
        notifier.showNotification(127, title, "", NotificationIcons(beginAniId))
    }

    /**
     * such as play some effect
     */
    fun onFailed() {}

    fun faied() {
        notifier.showNotification(127, title, "", NotificationIcons(failedAniId))
        onFailed()
        Handler().postDelayed({
            hide()
        },1000)
    }

    fun hide() {
        notifier.removeAll()
    }
}