package cn.vove7.jarvis.speech.baiduspeech.wakeup

import cn.vove7.jarvis.speech.baiduspeech.recognition.model.ErrorTranslation
import cn.vove7.vtp.log.Vog
import com.baidu.speech.EventListener
import com.baidu.speech.asr.SpeechConstant
import com.baidu.speech.asr.SpeechConstant.CALLBACK_EVENT_WAKEUP_STARTED

/**
 * 唤醒
 * @property listener IWakeupListener
 * @constructor
 */
class WakeupEventAdapter(private val listener: IWakeupListener) : EventListener {

    override fun onEvent(name: String?, params: String?, data: ByteArray?, offset: Int, length: Int) {
        // android studio日志Monitor 中搜索 WakeupEventAdapter即可看见下面一行的日志
        Vog.i(this, "wakeup name:$name; params:$params")
        when (name) {
            SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS -> {
                // 识别唤醒词成功
                val result = WakeUpResult.parseJson(name, params!!)
                val errorCode = result.errorCode
                if (result.hasError()) { // error不为0依旧有可能是异常情况
                    listener.onError(errorCode, ErrorTranslation.wakeupError(errorCode), result)
                } else {
                    val word = result.word
                    listener.onSuccess(word, result)
                }
            }
            SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR -> {
                // 识别唤醒词报错
                val result = WakeUpResult.parseJson(name, params!!)
                val errorCode = result.errorCode
                if (result.hasError()) {
                    listener.onError(errorCode, ErrorTranslation.wakeupError(errorCode), result)
                }
            }
            SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED -> {
                // 关闭唤醒词
                listener.onStop()
            }
            SpeechConstant.CALLBACK_EVENT_WAKEUP_AUDIO -> {
                // 音频回调
                Vog.d(this,"onEvent ---> $params")
                listener.onASrAudio(data, offset, length)
            }
            CALLBACK_EVENT_WAKEUP_STARTED ->{
                listener.onStart()
            }
        }
    }


}
