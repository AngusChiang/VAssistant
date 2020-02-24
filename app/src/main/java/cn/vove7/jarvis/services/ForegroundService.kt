package cn.vove7.jarvis.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.SpannableStringBuilder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.vtp.notification.ChannelBuilder

/**
 * # ForegroundService
 *
 * @author Vove
 * 2019/11/9
 */
class ForegroundService : Service() {

    companion object {
        @JvmStatic
        fun refreshTitle() {
            val i = Intent(GlobalApp.APP, ForegroundService::class.java)
            i.action = "REFRESH_TITLE"
            GlobalApp.APP.startService(i)
        }
    }

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
            val googleBlue = ContextCompat.getColor(this@ForegroundService, R.color.google_blue)

            addAction(0, "唤醒".spanColor(googleBlue), getPendingIntent(VoiceAssistActivity.WAKE_UP))
            addAction(0, "屏幕助手".spanColor(googleBlue), PendingIntent.getActivity(this@ForegroundService, 0, ScreenAssistActivity.createIntent(delayCapture = true), 0))

            val googleRed by lazy { ContextCompat.getColor(this@ForegroundService, R.color.google_red) }
            if (MainService.wakeupOpen) {
                addAction(0, "关闭语音唤醒".spanColor(googleRed),
                        PendingIntent.getBroadcast(this@ForegroundService,
                                0, Intent(AppBus.ACTION_STOP_WAKEUP), 0)
                )
            }
            if (!RemoteDebugServer.stopped) {
                addAction(0, "关闭调试".spanColor(googleRed),
                        PendingIntent.getBroadcast(this@ForegroundService,
                                0, Intent(AppBus.ACTION_STOP_DEBUG_SERVER), 0)
                )
            }
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(R.mipmap.ic_launcher_vassist)
            setOngoing(true)
            setContentTitle("VAssist前台服务(可长按关闭)")
            setContentText(buildContent())
        }.build()

    private fun buildContent(): CharSequence {
        val status = mutableListOf<CharSequence>()

        if (!RemoteDebugServer.stopped) {
            status += "远程调试"
        }

        if (MainService.wakeupOpen) {
            status += "语音唤醒".let {
                if (MainService.speechRecogService?.wakeupTimerEnd == true) "$it(已休眠)" else it
            }
        }

        val ss = SpannableStringBuilder()

        val s = status.size - 1
        status.forEachIndexed { i, c ->
            ss.append(c)
            if (i != s) {
                ss.append(" | ")
            }
        }
        return ss
    }

    override fun onCreate() {
        super.onCreate()
        GlobalLog.log("开启前台服务")
        startForeground(1111, foreNotification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "REFRESH_TITLE") {
            startForeground(1111, foreNotification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}