package cn.vove7.jarvis.assist

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.service.voice.VoiceInteractionSession
import androidx.annotation.RequiresApi
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.newTask
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity
import cn.vove7.jarvis.activities.screenassistant.statusBarIsLight
import cn.vove7.vtp.log.Vog
import java.util.*


/**
 * # AssistSession
 * 会话界面
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class AssistSession(context: Context) : VoiceInteractionSession(context) {
    override fun onHandleScreenshot(screenshot: Bitmap?) {
        Vog.d("onHandleScreenshot ---> screenshot: ${screenshot != null}")
        if (screenshot == null) {
            context.startActivity(Intent(context, ScreenAssistActivity::class.java).newTask())
            finish()
            return
        }
        val p = context.cacheDir.absolutePath +
                "/screen-${Random().nextInt()}.png"
        context.startActivity(ScreenAssistActivity.createIntent(p, light = screenshot.statusBarIsLight))

        runOnNewHandlerThread {
            UtilBridge.bitmap2File(screenshot, p)
        }
        finish()
    }

    override fun onHide() {
        System.gc()
    }

}