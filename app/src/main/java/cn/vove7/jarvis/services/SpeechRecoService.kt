package cn.vove7.jarvis.services

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.SpeechRecoAction
import cn.vove7.jarvis.speech.recognition.OfflineRecogParams
import cn.vove7.jarvis.speech.recognition.listener.SpeechStatusListener
import cn.vove7.jarvis.speech.recognition.model.IStatus
import cn.vove7.jarvis.speech.recognition.model.IStatus.Companion.STATUS_WAKEUP_SUCCESS
import cn.vove7.jarvis.speech.recognition.recognizer.MyRecognizer
import cn.vove7.jarvis.speech.wakeup.MyWakeup
import cn.vove7.jarvis.speech.wakeup.RecogWakeupListener
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.maths.LogicOperators
import cn.vove7.vtp.toast.Voast
import com.baidu.speech.asr.SpeechConstant
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 语音识别服务
 */
class SpeechRecoService : BusService() {

    override val serviceId: Int
        get() = 527
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    private lateinit var myRecognizer: MyRecognizer
    /**
     * 唤醒器
     */
    lateinit var wakeuper: MyWakeup

    lateinit var toast: Voast
    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    var enableOffline = false

    private val backTrackInMs = 1500

    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == STATUS_WAKEUP_SUCCESS) {//唤醒
                // 此处 开始正常识别流程
//                val params = LinkedHashMap<String, Any>()
//                params[SpeechConstant.ACCEPT_AUDIO_VOLUME] = false
//                params[SpeechConstant.VAD] = SpeechConstant.VAD_DNN
//                val pid = PidBuilder.create().model(PidBuilder.SEARCH).toPId()
//                // 如识别短句，不需要需要逗号，将PidBuilder.INPUT改为搜索模型PidBuilder.SEARCH
//                params[SpeechConstant.PID] = pid
//                if (backTrackInMs > 0) { // 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
//                    params[SpeechConstant.AUDIO_MILLS] = System.currentTimeMillis() - backTrackInMs
//                }
                myRecognizer.cancel()
                startRecog()
//                myRecognizer.start(params)
            }

            if (msg != null) {// -> MainService
                AppBus.post(msg)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {

        }
    }

    /**
     * onAction
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(sAction: SpeechRecoAction) {
        when (sAction.action) {
            SpeechRecoAction.ActionCode.ACTION_START_RECO -> startRecog()
            SpeechRecoAction.ActionCode.ACTION_STOP_RECO -> stopRecog()
            SpeechRecoAction.ActionCode.ACTION_CANCEL_RECO -> cancelRecog()
            SpeechRecoAction.ActionCode.ACTION_START_WAKEUP -> wakeuper.start()
            SpeechRecoAction.ActionCode.ACTION_STOP_WAKEUP -> wakeuper.stop()
            else -> {
                Vog.e(this, sAction)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        toast = Voast.with(this@SpeechRecoService, true).top()
        initRecog()
        wakeuper.start()
    }


    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    val listener = SpeechStatusListener(handler)

    protected fun initRecog() {
        myRecognizer = MyRecognizer(this, listener)

        val wakeLis = RecogWakeupListener(handler)
        wakeuper = MyWakeup(this, wakeLis)

        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams())
        }
    }

    private val params = mapOf(
            Pair(SpeechConstant.ACCEPT_AUDIO_DATA, false),
            Pair(SpeechConstant.DISABLE_PUNCTUATION, false),//标点符号
            Pair(SpeechConstant.ACCEPT_AUDIO_VOLUME, true),
            Pair(SpeechConstant.PID, 15361)
    )

    internal fun startRecog() {
        if (!isListening()) {
            myRecognizer.start(params)
        } else {
            Vog.d(this, "启动失败，正在运行")
        }
    }

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    private fun stopRecog() {
        myRecognizer.stop()
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    private fun cancelRecog() {
        myRecognizer.cancel()
    }

    fun isListening(): Boolean {
        return !LogicOperators.orEquals(listener.status,
                arrayOf(IStatus.STATUS_NONE, IStatus.STATUS_FINISHED, IStatus.STATUS_WAKEUP_EXIT))
    }


    override fun onDestroy() {
        myRecognizer.release()
        super.onDestroy()
    }

    companion object {
        var instance: SpeechRecoService? = null
    }

}

