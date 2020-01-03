package cn.vove7.jarvis.assist

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.jarvis.services.MainService

/**
 * # OnlyRecogAssistSession
 *
 * @author Administrator
 * 2018/11/2
 */
class OnlyRecogAssistSession(context: Context) : VoiceInteractionSession(context) {
    override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
        launch {
            MainService.switchRecog()
        }
        finish()
    }
}