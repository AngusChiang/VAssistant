package cn.vove7.jarvis.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.support.annotation.RequiresApi
import cn.vove7.common.appbus.AppBus
import cn.vove7.vtp.log.Vog


/**
 * # BusService
 *
 * @author 17719
 * 2018/8/10
 */
abstract class BusService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    abstract val serviceId: Int

    @RequiresApi(Build.VERSION_CODES.O)
    fun initChannel() {
        val mNotiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mUri = Settings.System.DEFAULT_NOTIFICATION_URI
        val mChannel = NotificationChannel(serviceId.toString(), "后台服务", NotificationManager.IMPORTANCE_LOW)
        mChannel.description = "后台服务"
        mChannel.setSound(mUri, Notification.AUDIO_ATTRIBUTES_DEFAULT)
        mNotiManager.createNotificationChannel(mChannel)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannel()
            val n = Notification.Builder(this, serviceId.toString()).build()
            startForeground(serviceId, n)
        }
        AppBus.reg(this)
        Vog.d(this, "开启服务 ${this.javaClass.simpleName}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Vog.d(this, "onDestroy ${this.javaClass.simpleName}")
        AppBus.unreg(this)
    }
}