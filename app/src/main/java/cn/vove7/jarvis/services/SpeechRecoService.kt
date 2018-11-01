package cn.vove7.jarvis.services

import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.support.v4.app.ActivityCompat
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.VoiceData
import cn.vove7.common.model.RequestPermission
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.executorengine.helper.AdvanContactHelper
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.receivers.PowerEventReceiver
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
import cn.vove7.jarvis.speech.wakeup.WakeupEventAdapter
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.baidu.speech.asr.SpeechConstant
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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

//    /**
//     * 唤醒器
//     */
//    var wakeuper= MyWakeup()

    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    private var enableOffline = false

    /**
     * 分发事件
     */
    private val handler: RecoHandler

    init {
        val t = HandlerThread("reco")
        t.start()
        handler = RecoHandler(t.looper)
        thread {
            initRecog()
            initOfflineWord()
            //初始化唤醒器
            if (AppConfig.voiceWakeup) {
                startWakeUp()
            }
        }
    }

    private val timerHandler: Handler by lazy {
        val t = HandlerThread("auto_sleep")
        t.start()
        Handler(t.looper)
    }

    /**
     * 开启定时关闭
     * 重启定时器
     */
    fun startAutoSleepWakeUp() {
        if (PowerEventReceiver.isCharging) return
        stopAutoSleepWakeup()
        Vog.d(this, "startAutoSleepWakeUp ---> 开启自动休眠")
        timerHandler.postDelayed(stopWakeUpTimer,
                if (BuildConfig.DEBUG) 10000
                else AppConfig.autoSleepWakeupMillis)
    }

    //关闭定时器
    fun stopAutoSleepWakeup() {
        Vog.d(this, "stopAutoSleepWakeup ---> 关闭自动休眠")
        timerHandler.removeCallbacks(stopWakeUpTimer)
    }

    private val stopWakeUpTimer = Runnable {
        stopWakeUp()
    }

    fun startWakeUp() {
        val wakeLis = RecogWakeupListener(handler)
        MyWakeup.start(WakeupEventAdapter(wakeLis))
        startAutoSleepWakeUp()
    }

    fun stopWakeUp() {
        stopAutoSleepWakeup()
        MyWakeup.stop()
    }

    lateinit var listener: SpeechStatusListener
    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    private fun initRecog() {
        listener = SpeechStatusListener(handler)
        val context: Context = GlobalApp.APP
        myRecognizer = MyRecognizer(context, listener)

//        wakeuper = MyWakeup(context, wakeLis)

        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams())
        }
    }

    val context: Context
        get() = GlobalApp.APP

    private val offSpeechGrammarPath = context.filesDir.absolutePath + "/bd/baidu_speech_grammar.bsg"
    private fun initOfflineWord() {
        LuaUtil.assetsToSD(context, "bd/baidu_speech_grammar.bsg",
                offSpeechGrammarPath)
        myRecognizer.loadOfWord(offWordParams)
    }

    private val offWordParams: Map<String, Any>
        get() = mapOf(
                Pair(SpeechConstant.DECODER, 0),
                Pair(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, offSpeechGrammarPath),
                Pair(SpeechConstant.SLOT_DATA, OffWord(
                        if (ActivityCompat.checkSelfPermission(AdvanContactHelper.context,
                                        android.Manifest.permission.READ_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) //首次启动无权限 不做
                            arrayOf() else AdvanContactHelper.getContactName()
                        , AdvanAppHelper.getAppName()
                ).toString())
        )

    private val recoParams = mapOf(
            Pair(SpeechConstant.ACCEPT_AUDIO_DATA, false),
//            Pair(SpeechConstant.VAD_MODEL, "dnn"),
            Pair(SpeechConstant.DISABLE_PUNCTUATION, false),//标点符号
            Pair(SpeechConstant.ACCEPT_AUDIO_VOLUME, true),
            Pair(SpeechConstant.IN_FILE, "#cn.vove7.jarvis.tools.MicrophoneInputStream.getInstance()"),
            Pair(SpeechConstant.PID, 1536)
    )

    class OffWord(
            @SerializedName("contact_name")
            val contactName: Array<String>
            , @SerializedName("appname")
            val appName: Array<String>
    ) {
        override fun toString(): String {
            Vog.d(this, "OffWord ---> $contactName \n $appName")
            return Gson().toJson(this)
        }
    }

    internal fun startRecog() {//检查权限
        if (ActivityCompat.checkSelfPermission(AdvanContactHelper.context,
                        android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            AppBus.post(RequestPermission("麦克风权限"))
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.canDrawOverlays(context)) {
            Vog.d(this, "show ---> 无悬浮窗")
            AppBus.post(RequestPermission("悬浮窗权限"))
            return
        }
        //震动 音效
        event.onStartRecog()
        sleep(100)
        if (!isListening()) {
            myRecognizer.start(recoParams)
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

    inner class RecoHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                CODE_WAKEUP_SUCCESS -> {//唤醒
                    val word = msg.data.getString("data")
                    startAutoSleepWakeUp()
                    if (!event.onWakeup(word))
                        return
//                    AppBus.postVoiceData(VoiceData(msg.what, word))
                    myRecognizer.cancel()
                    startRecog()
                    return
                }
                CODE_VOICE_TEMP -> {//中间结果
                    val res = msg.data.getString("data") ?: "null"
                    event.onTempResult(res)
//                    AppBus.postVoiceData(VoiceData(msg.what, res))
                }
                CODE_VOICE_ERR -> {//出错
                    val res = msg.data.getString("data") ?: "null"
                    event.onFailed(res)
//                    AppBus.postVoiceData(VoiceData(msg.what, res))
                }
                CODE_VOICE_VOL -> {//音量反馈
                    val data = msg.data.getSerializable("data") as VoiceData
                    event.onVolume(data)
//                    AppBus.postVoiceData(data)
                }
                CODE_VOICE_RESULT -> {//结果
                    val result = msg.data.getString("data") ?: "null"
                    event.onResult(result)
//                    AppBus.postVoiceData(VoiceData(msg.what, result))
                }
            }
        }
    }
}

interface SpeechEvent {
    /**
     *
     * @param word String?
     * @return Boolean 是否需要继续唤醒识别
     */
    fun onWakeup(word: String?): Boolean

    fun onStartRecog()
    fun onResult(result: String)
    fun onTempResult(temp: String)
    fun onFailed(err: String)
    fun onVolume(data: VoiceData)
    fun onStop()
    fun onCancel()
}
