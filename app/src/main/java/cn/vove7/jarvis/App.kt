package cn.vove7.jarvis

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.util.Log
import cn.vove7.androlua.LuaApp
import cn.vove7.common.appbus.MessageEvent
import cn.vove7.common.bridges.RootHelper
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.CrashHandler
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.vtp.log.Vog
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import io.github.kbiakov.codeview.classifier.CodeProcessor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread

class App : LuaApp() {

    private val mainService: Intent by lazy { Intent(this, MainService::class.java) }
    lateinit var services: Array<Intent>
    override fun onCreate() {
        Vog.d(this, "onCreate ---> begin ${System.currentTimeMillis() / 1000}")
        super.onCreate()
        ins = this
        EventBus.getDefault().register(this)

        CrashHandler.init()

        services = arrayOf(mainService)
        Vog.d(this, "onCreate ---> startServices ${System.currentTimeMillis() / 1000}")
        val storeFileName = "wdasfd"
        val keyPrefix = ""
        val seedKey = "fddfouafpiua".toByteArray()
        SecuredPreferenceStore.init(applicationContext, storeFileName, keyPrefix, seedKey, DefaultRecoveryHandler())
        AppConfig.init()
        startServices()

        Handler().postDelayed({
            thread {
                CodeProcessor.init(this)
                ShortcutUtil.addWakeUpShortcut()
//                AdvanAppHelper.updateAppList()
                startBroadcastReceivers()
                if (AppConfig.autoOpenASWithRoot) {
                    RootHelper.openAppAccessService(packageName,
                            "${MyAccessibilityService::class.qualifiedName}")
                }
                Vog.d(this, "service thread ---> finish ${System.currentTimeMillis() / 1000}")
            }
        },1000)
        if (!BuildConfig.DEBUG)
            Vog.init(this, Log.ERROR)
        Vog.d(this, "onCreate ---> end ${System.currentTimeMillis() / 1000}")
    }

    private fun startServices() {
        thread {
            services.forEach {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                } else {
                    startService(it)
                }
            }
        }
    }

    private fun startBroadcastReceivers() {
        PowerEventReceiver.start()
        ScreenStatusListener.start()
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
