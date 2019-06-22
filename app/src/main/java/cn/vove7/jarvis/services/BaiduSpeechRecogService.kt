package cn.vove7.jarvis.services

import android.content.Context
import android.os.HandlerThread
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.R
import cn.vove7.jarvis.speech.SpeechEvent
import cn.vove7.jarvis.speech.SpeechRecogService
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.jarvis.speech.baiduspeech.recognition.listener.SpeechStatusListener
import cn.vove7.jarvis.speech.baiduspeech.recognition.recognizer.MyRecognizer
import cn.vove7.jarvis.speech.baiduspeech.wakeup.BaiduVoiceWakeup
import cn.vove7.jarvis.speech.baiduspeech.wakeup.RecogWakeupListener
import cn.vove7.jarvis.speech.baiduspeech.wakeup.WakeupEventAdapter
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog
import com.baidu.speech.asr.SpeechConstant
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.qihoo360.replugin.utils.AssetsUtils
import java.io.File

/**
 * 百度语音识别服务
 */
class BaiduSpeechRecogService(event: SpeechEvent) : SpeechRecogService(event) {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    private lateinit var myRecognizer: MyRecognizer
    override val wakeupI: WakeupI by lazy { BaiduVoiceWakeup(WakeupEventAdapter(RecogWakeupListener(handler))) }

    /*
 * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
 */
    override var enableOffline = true

    /**
     * 分发事件
     */
    private val handler: RecogHandler

    init {
        val t = HandlerThread("recog")
        t.start()
        handler = RecogHandler(t.looper)
        initRecog()
        //初始化唤醒器
        if (AppConfig.voiceWakeup) {
            startWakeUp()
        }
    }

    lateinit var listener: SpeechStatusListener

    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    private fun initRecog() {
        listener = SpeechStatusListener(handler)
        val context: Context = GlobalApp.APP
        myRecognizer = MyRecognizer(context, listener)

        if (enableOffline) {
            runOnNewHandlerThread {
                try {
                    copyFile()
                    myRecognizer.loadOfflineEngine()
                } catch (e: Exception) {
                    GlobalLog.err(e)
                }
            }
        }
    }

    private fun copyFile() {
        val filesDir = context.filesDir.absolutePath + "/bd/"
        val fName = "baidu_speech_grammar.bsg"
        if (!File(filesDir, fName).exists()) {
            AssetsUtils.extractTo(context, "bd/baidu_speech_grammar.bsg",
                    filesDir, fName)
        }
    }


    private fun recogParams(silent: Boolean) = mutableMapOf<String, Any>(
            SpeechConstant.ACCEPT_AUDIO_DATA to false,
//          SpeechConstant.VAD_MODEL to "dnn",
            SpeechConstant.DISABLE_PUNCTUATION to false,//标点符号
            SpeechConstant.ACCEPT_AUDIO_VOLUME to true,
            SpeechConstant.PID to 1536,
            SpeechConstant.NLU to "enable"
    ).also {
        it[SpeechConstant.IN_FILE] = "#cn.vove7.jarvis.speech.baiduspeech.MicInputStream.instance()"
        //从指定时间开始识别，可以 - 指定ms 识别之前的内容
        if (!AppConfig.openResponseWord && !AppConfig.voiceRecogFeedback)//唤醒即识别 音效和响应词关闭时开启
            it[SpeechConstant.AUDIO_MILLS] = System.currentTimeMillis() - 100
        //长语音，不再依赖百度语音内置
//        if (AppConfig.lastingVoiceCommand)
        //静音时长
        if (AppConfig.voiceRecogFeedback && !silent)
            it[SpeechConstant.SOUND_START] = R.raw.recog_start
        if (AppConfig.voiceRecogFeedback) {
            it[SpeechConstant.SOUND_END] = R.raw.recog_finish
            it[SpeechConstant.SOUND_SUCCESS] = R.raw.recog_finish
            it[SpeechConstant.SOUND_ERROR] = R.raw.recog_failed
            it[SpeechConstant.SOUND_CANCEL] = R.raw.recog_cancel
        }

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


    override fun release() {
        myRecognizer.release()
        wakeupI.stop()
    }

    class OffWord(//离线词
            @SerializedName("contact_name")
            val contactName: Array<String>
            , @SerializedName("appname")
            val appName: Array<String>
    ) {
        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

}
