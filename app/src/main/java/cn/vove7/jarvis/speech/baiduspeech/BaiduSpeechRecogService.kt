package cn.vove7.jarvis.speech.baiduspeech

import android.media.MediaRecorder
import androidx.annotation.Keep
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.R
import cn.vove7.jarvis.speech.RecogEvent
import cn.vove7.jarvis.speech.SpeechRecogService
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.jarvis.speech.baiduspeech.recognition.listener.SpeechStatusListener
import cn.vove7.jarvis.speech.baiduspeech.recognition.recognizer.BaiduRecognizer
import cn.vove7.jarvis.speech.baiduspeech.wakeup.BaiduVoiceWakeup
import cn.vove7.jarvis.speech.baiduspeech.wakeup.RecogWakeupListener
import cn.vove7.jarvis.speech.baiduspeech.wakeup.WakeupEventAdapter
import cn.vove7.jarvis.tools.BaiduKey
import cn.vove7.vtp.log.Vog
import com.baidu.speech.asr.SpeechConstant
import com.baidu.speech.asr.SpeechConstant.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * 百度语音识别服务
 */
class BaiduSpeechRecogService(event: RecogEvent) : SpeechRecogService(event) {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    private val myRecognizer: BaiduRecognizer by lazy {
        BaiduRecognizer(GlobalApp.APP, SpeechStatusListener(handler))
    }

    override val wakeupI: WakeupI by lazy {
        BaiduVoiceWakeup(WakeupEventAdapter(RecogWakeupListener(handler)))
    }

    /**
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    override val enableOffline get() = AppConfig.enableOffline

    init {
        if (enableOffline) {
            runOnNewHandlerThread {
                runInCatch(true) {
                    myRecognizer.loadOfflineEngine()
                }
            }
        }
    }

    private fun recogParams(silent: Boolean) = mutableMapOf<String, Any>(
            ACCEPT_AUDIO_DATA to false,
            VAD to VAD_TOUCH,
            DISABLE_PUNCTUATION to false,//标点符号
            ACCEPT_AUDIO_VOLUME to true,
            PID to 1537,
            AUDIO_SOURCE to MediaRecorder.AudioSource.VOICE_RECOGNITION,
            NLU to "enable"
    ).also {
        if (enableOffline) {
            it[DECODER] = 2
            it[ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH] = "assets:///bd/baidu_speech_grammar.bsg"
        }
//        if (!AppConfig.recogCompatibleMode) {
//            it[IN_FILE] = "#cn.vove7.jarvis.speech.baiduspeech.MicInputStream.instance()"
//        }
        //从指定时间开始识别，可以 - 指定ms 识别之前的内容
        val voiceRecogFeedback = AppConfig.voiceRecogFeedback
        if (!AppConfig.openResponseWord && !voiceRecogFeedback)//唤醒即识别 音效和响应词关闭时开启
            it[AUDIO_MILLS] = System.currentTimeMillis() - 100
        //长语音，不再依赖百度语音内置
//        if (AppConfig.lastingVoiceCommand)
        //静音时长
        if (voiceRecogFeedback && !silent)
            it[SOUND_START] = R.raw.recog_start
        if (voiceRecogFeedback) {
            it[SOUND_END] = R.raw.recog_finish
            it[SOUND_SUCCESS] = R.raw.recog_finish
            it[SOUND_ERROR] = R.raw.recog_failed
            it[SOUND_CANCEL] = R.raw.recog_cancel
        }
        it[APP_ID] = BaiduKey.appId.toInt()
        it[APP_KEY] = BaiduKey.appKey
        it[SECRET] = BaiduKey.sKey

    }


    /**
     * 检查百度长语音
     * 然后定时关闭
     */
    override fun doStartRecog(silent: Boolean) {
        Vog.d("doStartRecog ---> 开始聆听")
        myRecognizer.start(recogParams(silent))
    }

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    override fun doStopRecog() {
        Vog.d("doStartRecog ---> 停止聆听")
        myRecognizer.stop()
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    override fun doCancelRecog() {
        isListening = false
        myRecognizer.cancel()
    }

    override fun doRelease() {
        myRecognizer.release()
        wakeupI.stop()
    }

    @Keep
    class OffWord(//离线词
            @SerializedName("contact_name")
            @Keep
            val contactName: Array<String>,
            @SerializedName("appname")
            @Keep
            val appName: Array<String>
    ) {
        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

}
