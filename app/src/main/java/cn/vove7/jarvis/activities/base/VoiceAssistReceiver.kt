package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.os.Bundle
import cn.vove7.jarvis.services.MainService
import cn.vove7.vtp.log.Vog

/**
 * # VoiceAssistReceiver
 *
 * @author 17719247306
 * 2018/9/10
 */
class VoiceAssistActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Vog.d(this, "onCreate ---> ASSIST wakeup")
        if (MainService.recoIsListening) {//配置
            MainService.instance?.onCommand(MainService.ORDER_CANCEL_RECO)
//            MainService.instance?.onCommand(MainService.ORDER_STOP_RECO)
        } else
            MainService.instance?.onCommand(MainService.ORDER_START_RECO)
        finish()
    }
}