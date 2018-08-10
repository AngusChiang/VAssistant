package cn.vove7.jarvis.utils

import android.content.Context
import android.content.Intent
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.services.SpeechRecoService
import cn.vove7.jarvis.services.SpeechSynService
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.service.ServiceHelper

/**
 * # ServiceChecker
 *
 * @author 17719
 * 2018/8/9
 */
class ServiceChecker(val context: Context) {

    fun checkService() {
        arrayOf(
                MainService::class.java,
                SpeechRecoService::class.java,
                SpeechSynService::class.java,
                MyAccessibilityService::class.java
        ).forEach {
            if (ServiceHelper.isServiceRunning(context, it)) {
                Vog.i(this, "checkService ${it::class.java.simpleName} not running")
                context.startService(Intent(context, it))
            }
        }
    }

}