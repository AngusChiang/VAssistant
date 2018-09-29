package cn.vove7.jarvis.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.VoiceData
import cn.vove7.jarvis.speech.recognition.OfflineRecogParams
import cn.vove7.jarvis.speech.recognition.listener.SpeechStatusListener
import cn.vove7.jarvis.speech.recognition.model.IStatus
import cn.vove7.jarvis.speech.recognition.model.IStatus.Companion.CODE_VOICE_ERR
import cn.vove7.jarvis.speech.recognition.model.IStatus.Companion.CODE_VOICE_RESULT
import cn.vove7.jarvis.speech.recognition.model.IStatus.Companion.CODE_VOICE_TEMP
import cn.vove7.jarvis.speech.recognition.model.IStatus.Companion.CODE_VOICE_VOL
import cn.vove7.jarvis.speech.recognition.model.IStatus.Companion.CODE_WAKEUP_SUCCESS
import cn.vove7.jarvis.speech.recognition.recognizer.MyRecognizer
import cn.vove7.jarvis.speech.wakeup.MyWakeup
import cn.vove7.jarvis.speech.wakeup.RecogWakeupListener
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.vtp.log.Vog
import com.baidu.speech.asr.SpeechConstant
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * 语音识别服务
 */
class SpeechRecoService(val event: SpeechEvent) {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    private lateinit var myRecognizer: MyRecognizer

    /**
     * 唤醒器
     */
    lateinit var wakeuper: MyWakeup

    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    private var enableOffline = false

    /**
     * 分发事件
     */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                CODE_WAKEUP_SUCCESS -> {//唤醒
                    val word = msg.data.getString("data")
                    event.onWakeup(word)
                    AppBus.postVoiceData(VoiceData(msg.what, word))
                    myRecognizer.cancel()
                    startRecog()
                    return
                }
                CODE_VOICE_TEMP -> {//中间结果
                    val res = msg.data.getString("data") ?: "null"
                    event.onTempResult(res)
                    AppBus.postVoiceData(VoiceData(msg.what, res))
                }
                CODE_VOICE_ERR -> {//出错
                    val res = msg.data.getString("data") ?: "null"
                    event.onFailed(res)
                    AppBus.postVoiceData(VoiceData(msg.what, res))
                }
                CODE_VOICE_VOL -> {//音量反馈
                    val data = msg.data.getSerializable("data") as VoiceData
                    event.onVolume(data)
                    AppBus.postVoiceData(data)
                }
                CODE_VOICE_RESULT -> {//结果
                    val result = msg.data.getString("data") ?: "null"
                    event.onResult(result)
                    AppBus.postVoiceData(VoiceData(msg.what, result))
                }
            }
        }
    }

    init {
        thread {
            initRecog()
            //初始化唤醒器
            if (AppConfig.voiceWakeup)
                wakeuper.start()
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

        val wakeLis = RecogWakeupListener(handler)
        wakeuper = MyWakeup(context, wakeLis)

        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams())
        }
    }

    private val params = mapOf(
            Pair(SpeechConstant.ACCEPT_AUDIO_DATA, false),
//            Pair(SpeechConstant.VAD_MODEL, "dnn"),
            Pair(SpeechConstant.DISABLE_PUNCTUATION, false),//标点符号
            Pair(SpeechConstant.ACCEPT_AUDIO_VOLUME, true),
            Pair(SpeechConstant.PID, 1536)
    )

    internal fun startRecog() {
        //震动 音效
        event.onStartRecog()
        sleep(100)
        if (!isListening()) {
            myRecognizer.start(params)
        } else {
            Vog.d(this, "启动失败，正在运行")
        }
    }

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    fun stopRecog() {
        myRecognizer.stop()
        event.onStop()
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    fun cancelRecog() {
        myRecognizer.cancel()
        event.onCancel()
    }

    fun isListening(): Boolean {
        return !arrayOf(IStatus.STATUS_NONE, IStatus.STATUS_FINISHED, IStatus.CODE_WAKEUP_EXIT)
                .contains(listener.status)
    }

    fun release() {
        myRecognizer.release()
    }
}

interface SpeechEvent {
    fun onWakeup(word: String?)
    fun onStartRecog()
    fun onResult(result: String)
    fun onTempResult(temp: String)
    fun onFailed(err: String)
    fun onVolume(data: VoiceData)
    fun onStop()
    fun onCancel()
}
