package cn.vove7.accessibilityservicedemo

import android.app.Application
import android.content.Intent
import android.util.Log
import cn.vove7.accessibilityservicedemo.services.MainService
import cn.vove7.accessibilityservicedemo.speech.services.SpeechService
import cn.vove7.appbus.MessageEvent
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.InitDbData
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class App : Application() {

    lateinit var voiceService: Intent
    lateinit var mainService: Intent
    override fun onCreate() {
        EventBus.getDefault().register(this)
        instance = this
        super.onCreate()
        Vog.init(this, Log.DEBUG).log2Local(Log.DEBUG)
        voiceService = Intent(this, SpeechService::class.java)
        mainService = Intent(this, MainService::class.java)
        startService(voiceService)
        startService(mainService)
        DAO.init(this)
        if (BuildConfig.DEBUG)
            InitDbData.init()
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
