package cn.vove7.jarvis

//import com.argusapm.android.api.Client
//import com.argusapm.android.core.Config
//import com.argusapm.android.network.cloudrule.RuleSyncRequest
//import com.argusapm.android.network.upload.CollectDataSyncUpload
import android.content.Context
import android.content.Intent
import android.os.Build
import cn.vove7.androlua.LuaApp
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.droidplugin.RePluginManager
import cn.vove7.jarvis.receivers.AppInstallReceiver
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.services.AssistSessionService
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.AppNotification
import cn.vove7.jarvis.tools.CrashHandler
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.jarvis.view.openAccessibilityServiceAuto
import cn.vove7.vtp.log.Vog
import io.github.kbiakov.codeview.classifier.CodeProcessor


class App : GlobalApp() {

    private val mainService: Intent by lazy { Intent(this, MainService::class.java) }

    lateinit var services: Array<Intent>
    override fun onCreate() {
        super.onCreate()
        Vog.d("onCreate ---> begin ${System.currentTimeMillis() / 1000}")
        ins = this

        CrashHandler.init()
        services = arrayOf(mainService)
        AppConfig.init()//加载配置
        Vog.d("onCreate ---> 配置加载完成")

        runOnNewHandlerThread("app_load", delay = 1000) {
            if (AppConfig.FIRST_LAUNCH_NEW_VERSION || BuildConfig.DEBUG)
                LuaApp.init(this,AppConfig.FIRST_LAUNCH_NEW_VERSION)
            startServices()
            CodeProcessor.init(this@App)
            ShortcutUtil.initShortcut()
            startBroadcastReceivers()
            runOnPool {
                openAccessibilityServiceAuto(this@App)
                AppNotification.updateNotificationChannel(this)
            }
            RePluginManager().launchWithApp()
            Vog.d("onCreate ---> 结束 ${System.currentTimeMillis() / 1000}")
            System.gc()
        }

    }

    private fun startServices() {
        runOnPool {
            services.forEach {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                } else {
                    startService(it)
                }
            }
            startService(Intent(this, AssistSessionService::class.java))
        }
    }

    private fun startBroadcastReceivers() {
        runOnNewHandlerThread("startBroadcastReceivers") {
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
        services.forEach {
            stopService(it)
        }
        stopBroadcastReceivers()
        super.onTerminate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        val builder = Config.ConfigBuilder()
//                .setAppContext(this)
//                .setAppName(getString(R.string.app_name))
//                .setRuleRequest(RuleSyncRequest())
//                .setUpload(CollectDataSyncUpload())
//                .setAppVersion(BuildConfig.VERSION_NAME)
//                .setApmid(if (BuildConfig.DEBUG) "j735c9gol8uw"
//                else "nggjim4etqcq")
//        Client.attach(builder.build())
//        Client.startWork()
    }
}