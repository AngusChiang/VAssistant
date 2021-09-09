package cn.vove7.jarvis.speech.sys

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import cn.vove7.android.common.loge
import cn.vove7.android.common.logi
import java.lang.Thread.sleep
import java.util.*

class SystemTTS(val context: Context, val listener: UtteranceProgressListener) {
    //核心播放对象
    private val initLock = Object()

    private var textToSpeech = TextToSpeech(context.applicationContext, ::init)


    fun reInit() {
        textToSpeech = TextToSpeech(context.applicationContext, ::init)
    }

    //是否支持
    var isSupport = false
        private set

    //textToSpeech的配置
    private fun init(i: Int) {
        if (i == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.CHINESE)
            textToSpeech.setPitch(1.0f)
            textToSpeech.setSpeechRate(1.0f)
            textToSpeech.setOnUtteranceProgressListener(listener)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //系统不支持该语言播报
                "SysTTS 系统不支持该语言播报".loge()
            } else {
                "SysTTS 初始化成功".logi()
            }
            isSupport = true
        } else {
            "SysTTS 初始化失败 $i".loge()
            isSupport = false
        }
        synchronized(initLock) {
            initLock.notify()
        }
    }

    fun speak(text: String?): Boolean {
        if (!isSupport) {
            return false
        }
        fun doSpeak() = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)
        var code = doSpeak()
        if (code == TextToSpeech.SUCCESS) {
            return true
        } else {
            "tts speak err: $code".loge()
            reInit()
            synchronized(initLock) {
                initLock.wait(3000)
            }
            code = doSpeak()
        }
        return code == TextToSpeech.SUCCESS
    }

    fun stop() {
        textToSpeech.stop()
    }

    fun destroy() {
        stop()
        textToSpeech.shutdown()
    }

}