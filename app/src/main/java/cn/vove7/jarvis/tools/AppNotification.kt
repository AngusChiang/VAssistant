package cn.vove7.jarvis.tools

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.notification.ChannelBuilder
import cn.vove7.vtp.notification.NotificationHelper
import cn.vove7.vtp.notification.NotificationIcons

/**
 * # AppNotification
 * 通知管理
 * 随时通知
 * @author Administrator
 * 2018/11/24
 */
object AppNotification {

    val c
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ChannelBuilder.with("app_notification", "应用通知", NotificationManagerCompat.IMPORTANCE_MAX)
                        .build().apply {
                            enableVibration(true)
                            setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                            vibrationPattern = longArrayOf(0, 200)
                        }
            } else null

    private val notificationHelper by lazy {
        NotificationHelper(GlobalApp.APP, c, true)
    }

    fun updateNotificationChannel(context: Context) {
        val aa = arrayOf("app_notification", "StatusBarIcon", "StatusBarIcon_alert")
        if (AppConfig.FIRST_LAUNCH_NEW_VERSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                notificationChannels?.forEach {
                    if (it.id !in aa) {
                        try {
                            deleteNotificationChannel(it.id)
                        } catch (e: Exception) {
                            GlobalLog.err(e)
                        }
                    }
                }
            }
        }
    }


    /**
     * 随机id 新通知
     * @param title String
     * @param content String?
     * @param iconId Int
     */
    fun newNotification(title: String, content: String? = null, iconId: Int) {
        notificationHelper.sendNewNotification(title, content
            ?: "", NotificationIcons(iconId))
    }

}