package cn.vove7.jarvis

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import cn.vove7.androlua.LuaApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.MessageEvent
import cn.vove7.common.bridges.RootHelper
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.droidplugin.RePluginManager
import cn.vove7.jarvis.receivers.AppInstallReceiver
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.services.AssistSessionService
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.CrashHandler
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import io.github.kbiakov.codeview.classifier.CodeProcessor
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class App : LuaApp() {

    private val mainService: Intent by lazy { Intent(this, MainService::class.java) }
    private val assistService: Intent by lazy { Intent(this, AssistSessionService::class.java) }

    lateinit var services: Array<Intent>
    override fun onCreate() {
        Vog.d(this, "onCreate ---> begin ${System.currentTimeMillis() / 1000}")
        super.onCreate()
        ins = this
        AppBus.reg(this)
        CrashHandler.init()

        services = arrayOf(mainService, assistService)
        AppConfig.init()//加载配置
        Vog.d(this, "onCreate ---> 配置加载完成")
        HandlerThread("app_load").apply {
            start()
            Handler(looper).post {
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
                RePluginManager().launchWithApp(this@App)
                Vog.d(this, "onCreate ---> 结束 ${System.currentTimeMillis() / 1000}")

                quitSafely()
            }
        }
        if (!BuildConfig.DEBUG)
            Vog.init(this, Log.ERROR)
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
//            BTConnectListener.start()
        }
    }

    private fun stopBroadcastReceivers() {
        PowerEventReceiver.stop()
        ScreenStatusListener.stop()
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


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: MessageEvent) {
        when (event.what) {
            MessageEvent.WHAT_MSG_INFO -> {
                Vog.d(this, event.toString())
            }
            MessageEvent.WHAT_MSG_ERR -> Vog.e(this, event.toString())
        }
    }
}

class RePluginApp:LuaApp() {

}