package cn.vove7.jarvis.assist

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.service.voice.VoiceInteractionSession
import android.support.annotation.RequiresApi
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity
import cn.vove7.vtp.log.Vog
import java.util.*


/**
 * # AssistSession
 * 会话界面
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class AssistSession(context: Context) : VoiceInteractionSession(context) {
    override fun onHandleScreenshot(screenshot: Bitmap?) {
        Vog.d("onHandleScreenshot ---> $screenshot")
        if (screenshot == null) {
            context.startActivity(Intent(context, ScreenAssistActivity::class.java))
            finish()
            return
        }
        val screenPath = UtilBridge.bitmap2File(screenshot, context.cacheDir
                .absolutePath + "/screen-${Random().nextInt()}.png")?.absolutePath

        context.startActivity(ScreenAssistActivity.createIntent(screenPath))
        finish()
    }

    override fun onHide() {
        System.gc()
    }

}