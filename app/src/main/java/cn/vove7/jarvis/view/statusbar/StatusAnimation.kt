package cn.vove7.jarvis.view.statusbar

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.R
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
abstract class StatusAnimation {

    open val nId = 127
    val c = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ChannelBuilder.with("StatusBarIcon", "IconAnimation", NotificationManagerCompat.IMPORTANCE_LOW).build()
    } else null

    abstract var title: String
    abstract var beginAniId: Int
    open var failedAniId: Int = R.drawable.ic_dissatisfied

    open var successId = -1
    open val finishId: Int? = null

    private val notifier = NotificationHelper(GlobalApp.APP, c)

    fun begin() {
        hideThread?.interrupt()
        try {
            notifier.showNotification(nId, title, "", NotificationIcons(beginAniId))
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
    }

    /**
     * 显示内容
     * @param c String message
     */
    fun show(c: String) {
        try {
            notifier.showNotification(nId, title, c, NotificationIcons(beginAniId))
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
    }

    fun showAndHideDelay(msg: String, delay: Long = 1500) {
        show(msg)
        hideDelay(delay)
    }

    /**
     * such as play some effect
     */
    open fun onFailed() {}

    fun failedAndHideDelay(msg: String? = null, delay: Long = 2500L) {
        failed(msg)
        hideDelay(delay)
    }

    fun failed(msg: String? = null) {
        hideThread?.interrupt()
        try {
            notifier.showNotification(nId, title, msg ?: "", NotificationIcons(failedAniId))
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
        onFailed()
    }

    open fun finish() {
        if (finishId == null) return
        hideThread?.interrupt()
        try {
            notifier.showNotification(nId, title, "", NotificationIcons(finishId!!))
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
        hideDelay()
    }

    var hideThread: Thread? = null
    fun hideDelay(delay: Long = 500) {
        Vog.d(this, "hideDelay ---> $delay")
        synchronized(StatusAnimation::class.java) {
            if (hideThread != null) return
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

    fun success() {
        hideThread?.interrupt()
        if (successId != -1) {
            try {
                notifier.showNotification(nId, title, "", NotificationIcons(successId))
            } catch (e: Exception) {
                GlobalLog.err(e)
            }
        }
        hideDelay(1000)
    }
}