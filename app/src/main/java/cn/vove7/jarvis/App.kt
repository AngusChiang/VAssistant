package cn.vove7.jarvis

import android.content.Context
import android.os.Build
import cn.jpush.android.api.JPushInterface
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
import com.wanjian.cockroach.Cockroach
import org.greenrobot.eventbus.Subscribe


@Suppress("MemberVisibilityCanBePrivate")
class App : GlobalApp() {

    private lateinit var mainService: MainService

    override fun onCreate() {
        super.onCreate()
        Vog.d("onCreate ---> begin ${System.currentTimeMillis() / 1000}")
        ins = this

        runWithClock("加载配置") {
            AppConfig.init()
        }

        if (!isMainProcess) {
            Vog.d("非主进程")
            return
        }
        AppBus.reg(this)

        Cockroach.install(CrashHandler)

        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        JPushInterface.init(this)
        runOnNewHandlerThread("app_load") {
            startServices()
            ShortcutUtil.initShortcut()
            AdvanAppHelper.getPkgList()
            startBroadcastReceivers()
            runOnPool {
                openAccessibilityServiceAuto()
            }
            RePluginManager().launchWithApp()
            Vog.d("onCreate ---> 结束 ${System.currentTimeMillis() / 1000}")
            System.gc()

            initUm()
        }

    }

    @Synchronized
    private fun startServices() {
        if (!::mainService.isInitialized) {
            mainService = MainService()
        }
    }

    private fun startBroadcastReceivers() {
        runOnNewHandlerThread("startBroadcastReceivers", delay = 5000) {
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

    companion object {
        var ins: App? = null

        fun startServices() {
            ins?.startServices()
        }
    }

    private fun initUm() {
        UMConfigure.init(this, BuildConfig.UM_KEY, "default", UMConfigure.DEVICE_TYPE_PHONE, "")
    }

    override fun onTerminate() {
        MainService.instance?.destroy()
        stopBroadcastReceivers()
        super.onTerminate()
    }

    @Subscribe
    fun onUserInit(event: String) {
        if (event == AppBus.EVENT_USER_INIT) {
            JPushInterface.setAlias(this, 0, UserInfo.getUserId().toString())
        } else if (event == AppBus.EVENT_LOGOUT) {
            JPushInterface.deleteAlias(this, 0)
        }
    }
}