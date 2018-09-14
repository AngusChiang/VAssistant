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
        Vog.d(this,"onCreate ---> ASSIST wakeup")
        MainService.instance?.onCommand(MainService.ORDER_START_RECO)
        finish()
    }
}