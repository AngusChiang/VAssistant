package cn.vove7.jarvis.speech.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import cn.vove7.jarvis.speech.listener.SpeechStatusListener
import cn.vove7.jarvis.speech.model.IStatus
import cn.vove7.jarvis.speech.model.IStatus.Companion.STATUS_WAKEUP_EXIT
import cn.vove7.jarvis.speech.recognizer.MyRecognizer
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.SpeechAction
import cn.vove7.appbus.SpeechAction.Companion.ACTION_CANCEL
import cn.vove7.appbus.SpeechAction.Companion.ACTION_START
import cn.vove7.appbus.SpeechAction.Companion.ACTION_STOP
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.math.LogicOperators
import cn.vove7.vtp.toast.Voast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SpeechService : Service() {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    private lateinit var myRecognizer: MyRecognizer

    lateinit var toast: Voast
    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    var enableOffline = false
    /**
     * 控制UI按钮的状态
     */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg != null) {
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
    fun onAction(sAction: SpeechAction) {
        when (sAction.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
            ACTION_CANCEL -> cancel()
            else -> {
                Vog.e(this, sAction)
            }
        }
    }


    override fun onCreate() {
        AppBus.reg(this)
        super.onCreate()
        instance = this
        toast = Voast.with(this@SpeechService, true).top()
        initRecog()
        Vog.v(this, "开启服务")
    }

    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    val listener = SpeechStatusListener(handler)

    protected fun initRecog() {
        myRecognizer = MyRecognizer(this, listener)
        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams())
        }
    }

    private val params = mapOf(
            Pair("accept-audio-data", false),
            Pair("disable-punctuation", false),
            Pair("accept-audio-volume", true),
            Pair("pid", 15361)
    )

    internal fun start() {
        if (!isListening()) {
            myRecognizer.start(params)
        } else {
            Vog.d(this, "启动失败，正在运行")
        }


    }

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    private fun stop() {
        myRecognizer.stop()
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    private fun cancel() {
        myRecognizer.cancel()
    }
    fun isListening():Boolean{
        return !LogicOperators.orEquals(listener.status,
                arrayOf(IStatus.STATUS_NONE, IStatus.STATUS_FINISHED, STATUS_WAKEUP_EXIT))
    }


    override fun onDestroy() {
        myRecognizer.release()
        AppBus.unreg(this)
        super.onDestroy()
    }

    companion object {
        lateinit var instance: SpeechService
    }

}

