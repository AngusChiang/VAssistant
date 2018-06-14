package cn.vove7.accessibilityservicedemo

import android.app.Application
import android.content.Intent
import cn.vove7.accessibilityservicedemo.speech.services.SpeechService
import cn.vove7.accessibilityservicedemo.utils.MessageEvent
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class App : Application() {

    var mainActivity: MainActivity? = null

    lateinit var voiceService: Intent
    override fun onCreate() {
        EventBus.getDefault().register(this)
        instance = this
        super.onCreate()
        startService(Intent(this, SpeechService::class.java))
    }


    companion object {
        lateinit var instance: App
    }

    override fun onTerminate() {
        stopService(voiceService)
        super.onTerminate()
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: MessageEvent) {
        when (event.what) {
            MessageEvent.WHAT_INFO -> {
                Vog.d(this, event.toString())
            }
            MessageEvent.WHAT_ERR -> Vog.e(this, event.toString())
        }
    }


}
