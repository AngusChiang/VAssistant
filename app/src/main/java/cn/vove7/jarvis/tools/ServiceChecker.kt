package cn.vove7.jarvis.tools

import android.content.Context
import android.content.Intent
import cn.vove7.jarvis.services.MainService
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.service.ServiceHelper
import java.lang.Thread.sleep

/**
 * # ServiceChecker
 *
 * @author 17719
 * 2018/8/9
 */
class ServiceChecker(val context: Context) {

    fun checkService() {
        var b = false
        arrayOf(
                MainService::class.java
        ).forEach {
            Vog.i("checkService ${it::class.java.simpleName}")
            if (ServiceHelper.isServiceRunning(context, it)) {
                Vog.i("checkService ${it::class.java.simpleName} not running")
                context.startService(Intent(context, it))
                b = true
            }
        }
        if (b) {
            sleep(500)
        }
    }

}