package cn.vove7.jarvis.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.notification.ChannelBuilder

/**
 * # ForegroundService
 *
 * @author Vove
 * 2019/11/9
 */
class ForegroundService : Service() {

    private val channel
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelBuilder.with("foreground_service", "前台服务", NotificationManagerCompat.IMPORTANCE_MIN)
                    .build().apply {
                        enableVibration(false)
                        enableLights(false)
                    }
        } else null

    private val builder
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = channel!!
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
            NotificationCompat.Builder(this, c.id)
        } else {
            NotificationCompat.Builder(this)
        }

    override fun onCreate() {
        super.onCreate()
        GlobalLog.log("开启前台服务")
        startForeground(1111, builder.apply {
            setContentTitle("VAssist前台服务")
            setContentText("如果不想看到此通知，可长按进入通知管理关闭此通知")
        }.build())
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}