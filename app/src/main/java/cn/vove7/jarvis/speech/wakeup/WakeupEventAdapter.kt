package cn.vove7.jarvis.speech.wakeup

import com.baidu.speech.EventListener
import com.baidu.speech.asr.SpeechConstant

import cn.vove7.jarvis.speech.recognition.model.ErrorTranslation
import cn.vove7.vtp.log.Vog

/**
 * Created by fujiayi on 2017/6/20.
 */

class WakeupEventAdapter(private val listener: IWakeupListener) : EventListener {

    override fun onEvent(name: String?, params: String?, data: ByteArray?, offset: Int, length: Int) {
        // android studio日志Monitor 中搜索 WakeupEventAdapter即可看见下面一行的日志
        Vog.i(this, "wakeup name:$name; params:$params")
        if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS == name) { // 识别唤醒词成功
        val result = WakeUpResult.parseJson(name, params!!)
            val errorCode = result.errorCode
            if (result.hasError()) { // error不为0依旧有可能是异常情况
                listener.onError(errorCode, ErrorTranslation.wakeupError(errorCode), result)
            } else {
                val word = result.word
                listener.onSuccess(word, result)
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR == name) { // 识别唤醒词报错
            val result = WakeUpResult.parseJson(name, params!!)
            val errorCode = result.errorCode
            if (result.hasError()) {
                listener.onError(errorCode, ErrorTranslation.wakeupError(errorCode), result)
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED == name) { // 关闭唤醒词
            listener.onStop()
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_AUDIO == name) { // 音频回调
            listener.onASrAudio(data, offset, length)
        }
    }


}
