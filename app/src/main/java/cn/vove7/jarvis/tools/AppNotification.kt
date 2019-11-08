package cn.vove7.jarvis.tools

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.R
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

    private val notificationHelper get() = NotificationHelper(GlobalApp.APP, c, true)


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

    /**
     * 广播通知
     * @param id Int
     * @param title String
     * @param content String
     * @param broadcastIntent Intent
     * @param iconId Int
     */
    fun broadcastNotification(
            id: Int,
            title: String,
            content: String,
            broadcastIntent: Intent,
            iconId: Int = R.mipmap.ic_launcher_vassist
    ) {
        notificationHelper.showNotification(
                nId = id,
                title = title,
                content = content,
                broadcastIntent = broadcastIntent,
                icons = NotificationIcons(iconId))
    }

}