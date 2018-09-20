package cn.vove7.jarvis.view.statusbar

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.notification.ChannelBuilder
import cn.vove7.vtp.notification.NotificationHelper
import cn.vove7.vtp.notification.NotificationIcons
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * # StatusAnimation
 *
 * @author 17719247306
 * 2018/9/2
 */
private val nId = 127

abstract class StatusAnimation {

    val c = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ChannelBuilder.with("StatusBarIcon", "IconAnimation", NotificationManagerCompat.IMPORTANCE_LOW).build()
    } else null

    abstract var title: String
    abstract var beginAniId: Int
    abstract var failedAniId: Int
    open protected val finishId: Int? = null

    private val notifier = NotificationHelper(GlobalApp.APP, c)

    fun begin() {
        hideThread?.interrupt()
        notifier.showNotification(nId, title, "", NotificationIcons(beginAniId))
    }

    /**
     * such as play some effect
     */
    open fun onFailed() {}

    fun failed() {
        hideThread?.interrupt()
        notifier.showNotification(nId, title, "", NotificationIcons(failedAniId))
        onFailed()
        hideDelay()
    }

    open fun finish() {
        if (finishId == null) return
        hideThread?.interrupt()
        notifier.showNotification(nId, title, "", NotificationIcons(finishId!!))
        hideDelay()
    }

    var hideThread: Thread? = null
    fun hideDelay(delay: Long = 500) {
        Vog.d(this, "hideDelay ---> $delay")
        hideThread = thread {
            try {
                sleep(delay)
                (GlobalApp.APP.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .cancel(nId)
            } catch (e: Exception) {
            } finally {
                hideThread = null
            }
        }
    }

}