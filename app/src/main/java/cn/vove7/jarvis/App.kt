package cn.vove7.jarvis

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import cn.jiguang.analytics.android.api.JAnalyticsInterface
import cn.jpush.android.api.JPushInterface
import cn.vove7.common.activities.RunnableActivity.Companion.runInShellActivity
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runWithClock
import cn.vove7.jarvis.plugins.PowerListener
import cn.vove7.jarvis.receivers.AppInstallReceiver
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.services.ForegroundService
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.*
import cn.vove7.smartkey.android.AndroidSettings
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.Subscribe


@Suppress("MemberVisibilityCanBePrivate")
class App : GlobalApp() {

    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess) {
            Vog.d("非主进程")
            return
        }
        AndroidSettings.init(this)
        CrashHandler.install()
        runWithClock("加载配置") {
            AppLogic.onLaunch()
        }
        AppBus.reg(this)

        startMainServices()
    }

    private fun startMainServices() {
        MainService.start()
    }

    @Subscribe
    fun onUserEvent(event: String) {
        if (event == AppBus.EVENT_USER_INIT) {
            JPushInterface.setAlias(this, 0, UserInfo.getUserId().toString())
        } else if (event == AppBus.EVENT_LOGOUT) {
            JPushInterface.deleteAlias(this, 0)
        }
    }

    //供脚本api
    override fun startActivity(intent: Intent?) {
        //防止在外部无法打开其他应用
        val component = intent?.component
        val isVApp = component?.className?.let {
            kotlin.runCatching { Class.forName(it) }.isSuccess
        } ?: false
        Vog.d("isVApp $isVApp")
        if (!isVApp && AppConfig.openAppCompat) {
            runInShellActivity {
                it.startActivity(intent)
                it.finish()
            }
        } else {
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            super.startActivity(intent)
        }
    }

}

class InitCp : ContentProvider() {

    override fun onCreate(): Boolean {
        runOnNewHandlerThread(name = "延时启动", delay = 2000) {
            launch {
                openAccessibilityServiceAuto()
                setAssistantAppAuto()
            }

            JPushInterface.setDebugMode(BuildConfig.DEBUG)
            JPushInterface.init(context)
            JAnalyticsInterface.init(context)
            JAnalyticsInterface.setDebugMode(BuildConfig.DEBUG)
            ShortcutUtil.initShortcut()
            AdvanAppHelper.getPkgList()
            startBroadcastReceivers()
            launchExtension()

            val foreService = Intent(context, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(foreService)
            } else {
                context?.startService(foreService)
            }
        }
        return true
    }

    private fun startBroadcastReceivers() {
        runOnNewHandlerThread("startBroadcastReceivers", delay = 2000) {
            PowerEventReceiver.start()
            ScreenStatusListener.start()
            AppInstallReceiver.start()
            UtilEventReceiver.start()
//            BTConnectListener.start()
        }
    }

    private fun launchExtension() {
        if (AppConfig.extPowerIndicator) {
            PowerListener.start()
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}