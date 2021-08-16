package cn.vove7.jarvis.view.statusbar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import cn.vove7.vtp.notification.NotificationIcons
import java.util.*

/**
 * # NotificationHelper
 *
 * Created by Vove on 2018/7/26
 */
class NotificationHelper(
        var context: Context,
        val channelId: String,
        val channel: NotificationChannel? = null
) {

    private lateinit var notificationManager: NotificationManager

    private val notificationIds = mutableSetOf<Int>()

    init {
        buildBuilder()
        initNotificationManager()
    }


    private fun initNotificationManager(): NotificationManager {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channel != null) {
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }

    private fun buildBuilder() = NotificationCompat.Builder(context, channelId)

    fun removeAll() {
        synchronized(notificationIds) {
            notificationIds.forEach {
                notificationManager.cancel(it)
            }
            notificationIds.clear()
        }
    }

    fun showNotification(
            nId: Int, title: String, content: String,
            icons: NotificationIcons,
            ntfBuilder: NotificationCompat.Builder.() -> Unit = {}
    ) {
        notificationIds.add(nId)
        val builder = buildBuilder()
        builder.setSmallIcon(icons.smallIcon)
                .setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
        ntfBuilder(builder)

        notificationManager.notify(nId, builder.build())
    }

    fun sendNewNotification(
            title: String, content: String,
            icons: NotificationIcons,
            ntfBuilder: NotificationCompat.Builder.() -> Unit
    ) {
        val nId = Random().nextInt()
        showNotification(nId, title, content, icons, ntfBuilder)
    }

    /**
     * 更新下载进度
     * @param id    id
     * @param intent 下载完成后的广播intent
     */
    fun notifyDownloadProgress(id: Int, title: String, msg: String, max: Int, progress: Int, intent: Intent?) {
        val builder = buildBuilder()

        builder.setOngoing(progress != max)
        builder.setSmallIcon(if (max == progress)
            android.R.drawable.stat_sys_download_done
        else
            android.R.drawable.stat_sys_download)
                .setContentText(msg)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setProgress(max, progress, progress < 0)
                .setContentTitle(title)
        if (intent != null) {
            val contentIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setContentIntent(contentIntent)
        }
        val notification = builder.build()
        notificationManager.notify(id, notification)
    }

}
