package cn.vove7.jarvis

import android.content.Intent
import android.util.Log
import cn.vove7.androlua.LuaApp
import cn.vove7.appbus.MessageEvent
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.InitLuaDbData
import cn.vove7.datamanager.InitSimpleDbData
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.speech.services.SpeechService
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class App : LuaApp() {

    lateinit var voiceService: Intent
    lateinit var mainService: Intent

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        Vog.init(this, Log.VERBOSE).log2Local(Log.ERROR)
        voiceService = Intent(this, SpeechService::class.java)
        mainService = Intent(this, MainService::class.java)
        startService(voiceService)
        startService(mainService)
        DAO.init(this)
        if (BuildConfig.DEBUG)
            InitLuaDbData.init()
    }


    companion object {
        lateinit var instance: App
    }

    override fun onTerminate() {
        stopService(voiceService)
        stopService(mainService)
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
