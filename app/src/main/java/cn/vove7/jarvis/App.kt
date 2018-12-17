package cn.vove7.jarvis

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import cn.vove7.androlua.LuaApp
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.RootHelper
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
import cn.vove7.jarvis.tools.CrashHandler
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
//import com.argusapm.android.api.Client
//import com.argusapm.android.core.Config
//import com.argusapm.android.network.cloudrule.RuleSyncRequest
//import com.argusapm.android.network.upload.CollectDataSyncUpload
import io.github.kbiakov.codeview.classifier.CodeProcessor


class App : GlobalApp() {

    private val mainService: Intent by lazy { Intent(this, MainService::class.java) }
    private val assistService: Intent by lazy { Intent(this, AssistSessionService::class.java) }

    lateinit var services: Array<Intent>
    override fun onCreate() {
        Vog.d(this, "onCreate ---> begin ${System.currentTimeMillis() / 1000}")
        super.onCreate()
        ins = this

        CrashHandler.init()
        LuaApp.init(this)
        services = arrayOf(mainService, assistService)
        AppConfig.init()//加载配置
        Vog.d(this, "onCreate ---> 配置加载完成")

        runOnNewHandlerThread("app_load") {
            startServices()
            CodeProcessor.init(this@App)
            ShortcutUtil.addWakeUpShortcut()
//                AdvanAppHelper.updateAppList()
            startBroadcastReceivers()
            runOnPool {
                if (AppConfig.autoOpenASWithRoot && !PermissionUtils.accessibilityServiceEnabled(this@App)) {
                    RootHelper.openSelfAccessService()
                }
            }
            //插件自启 fixme
            RePluginManager().launchWithApp()
            Vog.d(this, "onCreate ---> 结束 ${System.currentTimeMillis() / 1000}")

        }
        if (!BuildConfig.DEBUG) {
            try {
                Vog.init(this, Log.ERROR)
            } catch (e: Exception) {
            }
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