package cn.vove7.jarvis

import android.content.Intent
import android.os.Build
import android.util.Log
import cn.vove7.androlua.LuaApp
import cn.vove7.appbus.MessageEvent
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.InitLuaDbData
import cn.vove7.jarvis.debugserver.LuaDebugServer
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.SpeechRecoService
import cn.vove7.jarvis.services.SpeechSynService
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread

class App : LuaApp() {

    private lateinit var voiceService: Intent
    private lateinit var mainService: Intent
    private lateinit var synService: Intent
    lateinit var services: Array<Intent>

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        Vog.init(this, Log.VERBOSE).log2Local(Log.ERROR)
        mainService = Intent(this, MainService::class.java)
        voiceService = Intent(this, SpeechRecoService::class.java)
//        synService = Intent(this, SpeechSynService::class.java)
        services = arrayOf(mainService, voiceService)//, synService)

        LuaDebugServer(this).start()
        startServices()
        DAO.init(this)
        if (BuildConfig.DEBUG)
            InitLuaDbData.init()
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
