package cn.vove7.jarvis.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.SpannableStringBuilder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.withFailLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.helper.ConnectiveNsdHelper
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.tools.fixApplicationNewTask
import cn.vove7.vtp.notification.ChannelBuilder

/**
 * # ForegroundService
 *
 * @author Vove
 * 2019/11/9
 */
class ForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "foreground_service_2"

        @JvmStatic
        fun refreshTitle() {
            val i = Intent(GlobalApp.APP, ForegroundService::class.java)
            i.action = "REFRESH_TITLE"
            runInCatch(log = true) {
                GlobalApp.APP.startService(i)
            }
        }

        fun start(context: Context) {
            val foreService = Intent(context, ForegroundService::class.java)
            kotlin.runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(foreService)
                } else {
                    context.startService(foreService)
                }
            }.withFailLog()
        }
    }

    private val channel
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChannelBuilder.with(CHANNEL_ID, "前台服务", NotificationManagerCompat.IMPORTANCE_MIN)
                .build().apply {
                    setShowBadge(false)
                    enableVibration(false)
                    enableLights(false)
                }
        } else null

    private val builder
        get() = NotificationCompat.Builder(this, CHANNEL_ID).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel!!)
            }
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
                        0, UtilEventReceiver.getIntent(AppBus.ACTION_STOP_WAKEUP), 0)
                )
            }
            if (!RemoteDebugServer.stopped) {
                addAction(0, "关闭调试".spanColor(googleRed),
                    PendingIntent.getBroadcast(this@ForegroundService,
                        0, UtilEventReceiver.getIntent(AppBus.ACTION_STOP_DEBUG_SERVER), 0)
                )
            } else if(BuildConfig.DEBUG) {
                addAction(0, "远程调试", getPendingIntent(VoiceAssistActivity.SWITCH_DEBUG_MODE))
            }
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(R.mipmap.ic_launcher_vassist)
            setOngoing(true)
            setContentTitle("VAssist前台服务")
            buildContent().also{
                if(it.isNotEmpty()) {
                    setContentText(it)
                }
            }
        }.build()


    private val statusList by lazy {
        mutableListOf<CharSequence>()
    }

    private fun buildContent(): CharSequence {
        val status = statusList
        status.clear()

        if (!RemoteDebugServer.stopped) {
            status += "远程调试(${SystemBridge.getLocalIpAddress()})[${RemoteDebugServer.clientCount}]"
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
        GlobalApp.ForeService = this
        ConnectiveNsdHelper.start()
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
        GlobalApp.ForeService = GlobalApp.APP
    }

    override fun startActivity(intent: Intent?) {
        intent?.fixApplicationNewTask()?.let {
            super.startActivity(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}