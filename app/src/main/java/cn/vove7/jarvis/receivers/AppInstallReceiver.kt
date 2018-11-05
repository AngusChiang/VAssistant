package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.vtp.log.Vog
import kotlin.concurrent.thread

/**
 * App安装卸载广播监听
 */
object AppInstallReceiver : DyBCReceiver() {
    override val intentFilter: IntentFilter by lazy {
        val i = IntentFilter()
        i.addAction(Intent.ACTION_POWER_CONNECTED)
        i.addAction(Intent.ACTION_POWER_DISCONNECTED)
        i
    }

    override fun onReceive(context: Context, intent: Intent) {
        thread {
            AdvanAppHelper.updateAppList()
        }
        val pkg = intent.data?.schemeSpecificPart
        Vog.d(this, "onReceive ---> $pkg")
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
        }
        if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
        }
        if (intent.action == Intent.ACTION_PACKAGE_REPLACED) {
        }

    }

}