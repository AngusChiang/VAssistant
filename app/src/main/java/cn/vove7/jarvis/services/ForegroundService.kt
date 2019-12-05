package cn.vove7.jarvis.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity
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
            ChannelBuilder.with("foreground_service_2", "前台服务", NotificationManagerCompat.IMPORTANCE_MIN)
                    .build().apply {

                        setShowBadge(false)
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

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            component = ComponentName(this@ForegroundService, VoiceAssistActivity::class.java)
        }
        return PendingIntent.getActivity(this, 0, intent, 0)
    }

    private val foreNotification
        get() = builder.apply {
            addAction(0, "唤醒", getPendingIntent(VoiceAssistActivity.WAKE_UP))
            addAction(0, "屏幕助手", PendingIntent.getActivity(this@ForegroundService, 0, ScreenAssistActivity.createIntent(delayCapture = true), 0))
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(R.mipmap.ic_launcher_vassist)
            setOngoing(true)
            setContentTitle("VAssist前台服务(可长按关闭)")
        }.build()

    override fun onCreate() {
        super.onCreate()
        GlobalLog.log("开启前台服务")
        startForeground(1111, foreNotification)
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}