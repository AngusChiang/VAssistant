package cn.vove7.jarvis.view.statusbar

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.notification.ChannelBuilder
import cn.vove7.vtp.notification.NotificationIcons
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * # StatusAnimation
 *
 * @author Vove
 * 2018/9/2
 */
abstract class StatusAnimation {
    open val importLevel: Int = NotificationManagerCompat.IMPORTANCE_LOW
    open val alert: Boolean = false
    open val nId = 127

    private val channelId get() = "StatusBarIcon${if (alert) "_alert" else ""}"

    private val channel
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ChannelBuilder.with(channelId,
                        "状态栏动画${if (alert) "_alert" else ""}", importLevel).build().apply {
                    Vog.d("alert ---> $alert")
                    if (alert) {
                        enableVibration(true)
                        setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                        vibrationPattern = longArrayOf(0, 200)
                    }
                }
            } else null


    abstract var title: String
    abstract var beginAniId: Int
    open var failedAniId: Int = R.drawable.ic_dissatisfied

    open var successId = -1
    open val finishId: Int? = null

    private val notifier by lazy { NotificationHelper(GlobalApp.APP, channelId, channel) }

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
        Vog.d("show ---> $c")
        try {
            notifier.showNotification(nId, title, c, NotificationIcons(beginAniId)) {
                onShowNtf(c, this)
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
    }

    open fun onShowNtf(c: String, builder: NotificationCompat.Builder) {}

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

    fun show(iconRes: Int, msg: String, intent: Intent? = null) {
        notifier.showNotification(nId, title, msg, NotificationIcons(iconRes)) {
            if (intent != null) {
                val i = PendingIntent.getBroadcast(GlobalApp.APP, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                setContentIntent(i)
            }
        }
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
        Vog.d("hideDelay ---> $delay")
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