package cn.vove7.jarvis

import android.app.ActivityManager
import android.content.Context
import cn.vove7.androlua.LuaApp
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runWithClock
import cn.vove7.jarvis.droidplugin.RePluginManager
import cn.vove7.jarvis.receivers.AppInstallReceiver
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.services.MainService
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.tools.AppNotification
import cn.vove7.jarvis.tools.CrashHandler
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.jarvis.view.openAccessibilityServiceAuto
import cn.vove7.vtp.log.Vog
import com.umeng.commonsdk.UMConfigure
import io.github.kbiakov.codeview.classifier.CodeProcessor


@Suppress("MemberVisibilityCanBePrivate")
class App : GlobalApp() {

    private lateinit var mainService: MainService

    override fun onCreate() {
        super.onCreate()
        Vog.d("onCreate ---> begin ${System.currentTimeMillis() / 1000}")
        ins = this

        CrashHandler.init()
        runWithClock("加载配置") {
            AppConfig.init()
        }

        if(!isMainProcess){
            Vog.d("非主进程")
            return
        }

        runOnNewHandlerThread("app_load") {
            if (AppConfig.FIRST_LAUNCH_NEW_VERSION || BuildConfig.DEBUG)
                LuaApp.init(this, AppConfig.FIRST_LAUNCH_NEW_VERSION)
            startServices()
            CodeProcessor.init(this@App)
            ShortcutUtil.initShortcut()
            startBroadcastReceivers()
            runOnPool {
                openAccessibilityServiceAuto()
                AppNotification.updateNotificationChannel(this)
            }
            RePluginManager().launchWithApp()
            Vog.d("onCreate ---> 结束 ${System.currentTimeMillis() / 1000}")
            System.gc()

            UMConfigure.init(this, "5d00ae230cafb2990c0000e4", "default", UMConfigure.DEVICE_TYPE_PHONE, "")
        }

    }

    private fun startServices() {
        mainService = MainService()
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

    companion object {
        var ins: App? = null

        fun startServices() {
            ins?.startServices()
        }
    }

    override fun onTerminate() {
        MainService.instance?.destroy()
        stopBroadcastReceivers()
        super.onTerminate()
    }

    /**
     * 是否为主进程
     * 未配置主进程名 默认为包名
     */
    val isMainProcess
        get() = this.packageName == currentProcessName

    val currentProcessName
        get(): String? {
            val pid = android.os.Process.myPid()
            val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.runningAppProcesses.forEach { process ->
                if (process.pid == pid)
                    return process.processName.also {
                        Vog.d("进程：${process.processName}")
                    }
            }
            return null
        }
}