package cn.vove7.jarvis.services

import android.os.Build
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import androidx.annotation.RequiresApi

import cn.vove7.jarvis.assist.AssistSession
import cn.vove7.jarvis.assist.OnlyRecogAssistSession
import cn.vove7.common.app.AppConfig
import cn.vove7.vtp.log.Vog

@RequiresApi(api = Build.VERSION_CODES.M)
class AssistSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession? {
        Vog.d("onNewSession ---> 新会话")
        return if (AppConfig.useAssistService) {//开启
            AssistSession(this)
        } else {
            return OnlyRecogAssistSession(this)
        }
    }

}
