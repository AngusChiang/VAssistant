package cn.vove7.jarvis.tools

import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.vtp.notification.ChannelBuilder
import cn.vove7.vtp.notification.NotificationHelper
import cn.vove7.vtp.notification.NotificationIcons
import java.util.*

/**
 * # AppNotification
 * 通知管理
 * 随时通知
 * @author Administrator
 * 2018/11/24
 */
object AppNotification {

    val channel by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelBuilder.with("应用通知", "应用通知", NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .build()
        } else null
    }

    val notificationHelper by lazy {
        NotificationHelper(GlobalApp.APP, channel)
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