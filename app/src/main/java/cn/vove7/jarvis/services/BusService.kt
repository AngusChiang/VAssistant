package cn.vove7.jarvis.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import cn.vove7.appbus.AppBus
import cn.vove7.vtp.log.Vog

/**
 * # BusService
 *
 * @author 17719
 * 2018/8/10
 */
open class BusService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()
        AppBus.reg(this)
        Vog.d(this, "开启服务 ${this.javaClass.simpleName}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Vog.d(this,"onDestroy ${this.javaClass.simpleName}")
        AppBus.unreg(this)
    }
}