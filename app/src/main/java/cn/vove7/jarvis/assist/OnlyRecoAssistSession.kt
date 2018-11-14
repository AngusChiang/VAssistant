package cn.vove7.jarvis.assist

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.jarvis.services.MainService
import kotlin.concurrent.thread

/**
 * # OnlyRecoAssistSession
 *
 * @author Administrator
 * 2018/11/2
 */
class OnlyRecoAssistSession(context: Context) : VoiceInteractionSession(context) {
    override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
        runOnPool {
            MainService.switchReco()
        }
        finish()
    }
}