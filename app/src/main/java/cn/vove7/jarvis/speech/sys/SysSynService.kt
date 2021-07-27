package cn.vove7.jarvis.speech.sys

import android.speech.tts.UtteranceProgressListener
import cn.vove7.android.common.ext.delayRun
import cn.vove7.android.common.logi
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.speech.SpeechSynService
import cn.vove7.jarvis.speech.SyntheEvent

/**
 * # SysSynService
 *
 * @author Vove
 * @date 2021/7/26
 */
class SysSynService(event: SyntheEvent) : SpeechSynService(event) {

    private lateinit var tts: SystemTTS

    override fun release() {
        tts.destroy()
    }

    override fun setAudioStream(type: Int) {
    }

    override fun doSpeak(text: String) {
        val a = tts.speak(text)
        "SysSynService doSpeak: $a".logi()
        if (!a) {
            GlobalApp.toastError("系统TTS合成失败 isSupport: ${tts.isSupport}")
            delayRun(1000) {
                event.onFinish(text)
                tts.reInit()
            }
        }
    }

    override fun doStop() {
        tts.stop()
    }

    override fun init() {
        tts = SystemTTS(GlobalApp.APP, object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
            }

            override fun onDone(utteranceId: String?) {
                event.onFinish(utteranceId)
            }

            override fun onError(utteranceId: String?) {
                event.onError(utteranceId, "None")
            }
        })
    }

    override fun doPause() {
    }

    override fun doResume() {
    }
}