package cn.vove7.jarvis

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import cn.jpush.android.api.JPushInterface
import cn.vove7.common.activities.RunnableActivity.Companion.runInShellActivity
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runWithClock
import cn.vove7.jarvis.droidplugin.RePluginManager
import cn.vove7.jarvis.receivers.AppInstallReceiver
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.CrashHandler
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.jarvis.view.openAccessibilityServiceAuto
import cn.vove7.vtp.log.Vog
import com.qihoo360.replugin.gen.RePluginHostConfig
import com.umeng.commonsdk.UMConfigure
import org.greenrobot.eventbus.Subscribe


@Suppress("MemberVisibilityCanBePrivate")
class App : GlobalApp() {

    private lateinit var mainService: MainService

    override fun onCreate() {
        super.onCreate()
        runWithClock("加载配置") {
            AppConfig.init()
        }

        if (!isMainProcess) {
            Vog.d("非主进程")
            return
        }
        AppBus.reg(this)
        CrashHandler.install()

        runOnNewHandlerThread("app_load") {
            startMainServices()
            runOnPool {
                openAccessibilityServiceAuto()
            }
        }
        //延时初始化
        runOnNewHandlerThread(name = "延时启动", delay = 2000) {
            JPushInterface.setDebugMode(BuildConfig.DEBUG)
            JPushInterface.init(this)
            ShortcutUtil.initShortcut()
            AdvanAppHelper.getPkgList()
            startBroadcastReceivers()
            RePluginManager().launchWithApp()
            initUm()
        }

    }

    @Synchronized
    private fun startMainServices() {
        if (!::mainService.isInitialized) {
            mainService = MainService()
        }
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

    private fun stopBroadcastReceivers() {
        PowerEventReceiver.stop()
        ScreenStatusListener.stop()
        AppInstallReceiver.stop()
        UtilEventReceiver.stop()
    }

    override fun attachBaseContext(base: Context?) {
        //fix 4.x
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            val y = RePluginHostConfig.ACTIVITY_PIT_COUNT_TASK
        }
        super.attachBaseContext(base)
    }

    private fun initUm() {
        UMConfigure.init(this, BuildConfig.UM_KEY, "default", UMConfigure.DEVICE_TYPE_PHONE, "")
        UMConfigure.setLogEnabled(BuildConfig.DEBUG)
    }

    override fun onTerminate() {
        MainService.instance?.destroy()
        stopBroadcastReceivers()
        super.onTerminate()
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
            try {
                Class.forName(it)
                true
            } catch (e: Throwable) {
                false
            }
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