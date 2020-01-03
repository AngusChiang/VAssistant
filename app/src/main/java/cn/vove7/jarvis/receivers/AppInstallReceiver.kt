package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.vtp.log.Vog

/**
 * App安装卸载广播监听
 */
object AppInstallReceiver : DyBCReceiver() {
    override val intentFilter: IntentFilter
        get() = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }


    override fun onReceive(context: Context, intent: Intent) {
//        launch {
//            AdvanAppHelper.updateAppList()
//        }
        val pkg = intent.data?.schemeSpecificPart ?: return
        Vog.d("$pkg")
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            AdvanAppHelper.addNewApp(pkg)
        }
        if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
            AdvanAppHelper.removeAppCache(pkg)
        }
        if (intent.action == Intent.ACTION_PACKAGE_REPLACED) {
        }

    }

}