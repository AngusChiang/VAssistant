package cn.vove7.jarvis

import android.content.Intent
import android.os.Build
import android.util.Log
import cn.vove7.androlua.LuaApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.MessageEvent
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.CrashHandler
import cn.vove7.jarvis.utils.debugserver.RemoteDebugServer
import cn.vove7.vtp.log.Vog
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import io.github.kbiakov.codeview.classifier.CodeProcessor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread


class App : LuaApp() {

    private lateinit var mainService: Intent
    lateinit var services: Array<Intent>

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        Vog.init(this, Log.VERBOSE).log2Local(Log.ERROR)
        mainService = Intent(this, MainService::class.java)
        services = arrayOf(mainService)
        CrashHandler.init()

        if (BuildConfig.DEBUG)
            RemoteDebugServer.start()
        startServices()
        thread {
            val storeFileName = "wdasfd"
            val keyPrefix = ""
            val seedKey = "fddfouafpiua".toByteArray()
            SecuredPreferenceStore.init(applicationContext, storeFileName, keyPrefix, seedKey, DefaultRecoveryHandler())
            AppConfig.init()
            AdvanAppHelper.updateAppList()
            CodeProcessor.init(this)
            GlobalLog
        }
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

    override fun onTerminate() {
        services.forEach {
            stopService(it)
        }
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
