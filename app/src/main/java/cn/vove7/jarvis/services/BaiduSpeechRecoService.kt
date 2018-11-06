package cn.vove7.jarvis.services

import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.support.v4.app.ActivityCompat
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.RequestPermission
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.executorengine.helper.AdvanContactHelper
import cn.vove7.jarvis.speech.SpeechEvent
import cn.vove7.jarvis.speech.SpeechRecoService
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.jarvis.speech.baiduspeech.recognition.OfflineRecogParams
import cn.vove7.jarvis.speech.baiduspeech.recognition.listener.SpeechStatusListener
import cn.vove7.jarvis.speech.baiduspeech.recognition.recognizer.MyRecognizer
import cn.vove7.jarvis.speech.baiduspeech.wakeup.BaiduVoiceWakeup
import cn.vove7.jarvis.speech.baiduspeech.wakeup.RecogWakeupListener
import cn.vove7.jarvis.speech.baiduspeech.wakeup.WakeupEventAdapter
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.statusbar.MicroToggleAnimation
import cn.vove7.jarvis.view.statusbar.StatusAnimation
import cn.vove7.jarvis.view.statusbar.WakeupStatusAnimation
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.baidu.speech.asr.SpeechConstant
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.lang.Thread.sleep

/**
 * 百度语音识别服务
 */
class BaiduSpeechRecoService(event: SpeechEvent) : SpeechRecoService(event) {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    private lateinit var myRecognizer: MyRecognizer
    override val wakeupI: WakeupI by lazy { BaiduVoiceWakeup(WakeupEventAdapter(RecogWakeupListener(handler))) }
    //    /**
//     * 唤醒器
//     */
//    var wakeuper= BaiduVoiceWakeup()
    private val wakeupStatusAni: StatusAnimation by lazy { WakeupStatusAnimation() }

    /*
 * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
 */
    override var enableOffline = false

//    /**
//     * 唤醒词无间断标志，用于去掉命令头部
//     * 0 -> 无
//     * 1 -> 你好小V
//     * 2 -> 小v同学
//     */
//    var wakeupNoInterruptType = 0
//        get() {//获取即恢复false
//            val r = field
//            field = 0
//            return r
//        }

    /**
     * 分发事件
     */
    private val handler: RecoHandler

    init {
        val t = HandlerThread("reco")
        t.start()
        handler = RecoHandler(t.looper)
        HandlerThread("load_reco").apply {
            start()
            Handler(looper).post {
                initRecog()
                initOfflineWord()
                //初始化唤醒器
                if (AppConfig.voiceWakeup) {
                    startWakeUp()
                }
                quitSafely()
            }
        }
    }

    override fun startWakeUp() {
        wakeupStatusAni.showAndHideDelay("语音唤醒开启")
        startWakeUpSilently()
    }

    override fun startWakeUpSilently(resetTimer: Boolean) {
        wakeupI.start()
        if (resetTimer || timerEnd)//定时器结束
            startAutoSleepWakeup()
    }

    override fun stopWakeUp() {
        wakeupStatusAni.failed("语音唤醒关闭")
        stopWakeUpSilently()
        stopAutoSleepWakeup()
    }

    override fun stopWakeUpSilently() {
        wakeupI.stop()
    }

    lateinit var listener: SpeechStatusListener
    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    private fun initRecog() {
        listener = SpeechStatusListener(handler)
        val context: Context = GlobalApp.APP
        myRecognizer = MyRecognizer(context, listener)

//        wakeuper = BaiduVoiceWakeup(context, wakeLis)

        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams())
        }
    }

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

    private val recoParams
        get() = mutableMapOf(
                Pair(SpeechConstant.ACCEPT_AUDIO_DATA, false),
//            Pair(SpeechConstant.VAD_MODEL, "dnn"),
                Pair(SpeechConstant.DISABLE_PUNCTUATION, false),//标点符号
                Pair(SpeechConstant.ACCEPT_AUDIO_VOLUME, true),
                Pair(SpeechConstant.IN_FILE, "#cn.vove7.jarvis.speech.baiduspeech.MicrophoneInputStream.getInstance()"),
                Pair(SpeechConstant.PID, 1536)
        ).also {
            if (!AppConfig.openResponseWord)//响应词打开则无效
                it[SpeechConstant.AUDIO_MILLS] = System.currentTimeMillis() - 500
            //从指定时间开始识别，可以 - 指定ms 识别之前的内容
        }

//    private val backTrackInMs = 1500

    override fun startRecog() {//检查权限
        if (ActivityCompat.checkSelfPermission(AdvanContactHelper.context,
                        android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            AppBus.post(RequestPermission("麦克风权限"))
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.canDrawOverlays(context)) {
            Vog.d(this, "show ---> 无悬浮窗")
            AppBus.post(RequestPermission("悬浮窗权限"))
            return
        }
        sleep(80)
        if (!isListening) {
            //震动 音效
            event.onStartRecog()
            myRecognizer.start(recoParams)
        } else {
            Vog.d(this, "启动失败，正在运行")
        }
        super.startRecog()
    }

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    override fun stopRecog() {
        super.stopRecog()
        myRecognizer.stop()
        event.onStop()
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     * @param notify 通知事件
     */
    override fun cancelRecog(notify: Boolean) {
        super.cancelRecog(notify)
        myRecognizer.cancel()
        if (notify)
            event.onCancel()
    }

//    fun isListening(): Boolean {
//        return !arrayOf(IStatus.STATUS_NONE, IStatus.STATUS_FINISHED, IStatus.CODE_WAKEUP_EXIT)
//                .contains(listener.status)
//    }

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

//    /**
//     * 无间断唤醒，命令会带着唤醒词 '你好小V'
//     * @param resultWithWakeupWord String
//     * @return String
//     */
//    fun trimWakeupWord(resultWithWakeupWord: String): String {
//        return when (wakeupNoInterruptType) {//减去头部唤醒词
//            1 -> {
//                var f = false
//                resultWithWakeupWord.substring(resultWithWakeupWord.indexOfFirst {
//                    when (it) {
//                        '小' -> f = true
//                        'v', 'V', '薇' -> if (f) return@indexOfFirst true
//                    }
//                    return@indexOfFirst false
//                } + 1)
//            }
//            2 -> {
//                var f = false
//                resultWithWakeupWord.substring(resultWithWakeupWord.indexOfFirst {
//                    when (it) {
//                        '同' -> f = true
//                        '学' -> if (f) return@indexOfFirst true
//                    }
//                    return@indexOfFirst false
//                } + 1)
//            }
//            else -> resultWithWakeupWord
//        }.also { Vog.d(this, "trimWakeupWord ---> $it") }
//    }