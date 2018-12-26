package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.vtp.log.Vog
import kotlin.concurrent.thread

/**
 * App安装卸载广播监听
 */
object AppInstallReceiver : DyBCReceiver() {
    override val intentFilter: IntentFilter by lazy {
        IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        runOnPool {
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