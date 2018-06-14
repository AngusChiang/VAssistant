package cn.vove7.accessibilityservicedemo.speech.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import cn.vove7.accessibilityservicedemo.speech.CommonRecogParams
import cn.vove7.accessibilityservicedemo.speech.recognizer.MyRecognizer
import cn.vove7.accessibilityservicedemo.speech.OnlineRecogParams
import cn.vove7.accessibilityservicedemo.speech.listener.MessageStatusRecogListener
import cn.vove7.accessibilityservicedemo.speech.model.IStatus.Companion.STATUS_NONE
import cn.vove7.accessibilityservicedemo.utils.Bus
import cn.vove7.accessibilityservicedemo.utils.LogMessage
import cn.vove7.accessibilityservicedemo.utils.SpeechAction
import cn.vove7.accessibilityservicedemo.utils.SpeechAction.Companion.ACTION_CANCEL
import cn.vove7.accessibilityservicedemo.utils.SpeechAction.Companion.ACTION_START
import cn.vove7.accessibilityservicedemo.utils.SpeechAction.Companion.ACTION_STOP
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SpeechService : Service() {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    lateinit var myRecognizer: MyRecognizer

    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    var enableOffline = false
    /**
     * 控制UI按钮的状态
     */

    var handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        /*
         * @param msg
         */
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            handleMsg(msg)
        }
    }

    fun handleMsg(msg: Message) {
        Vog.i(this, msg)
        Bus.postLog(LogMessage(LogMessage.LEV_2, msg.obj.toString()))
    }

    var status: Int = 0
    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {

        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(sAction: SpeechAction) {
        when (sAction.action) {
            ACTION_START -> {
                start()
            }
            ACTION_STOP -> {
                stop()
            }
            ACTION_CANCEL -> {
                cancel()
            }
            else -> {
                Vog.e(this, sAction)
            }
        }
    }

    override fun onCreate() {
        Bus.reg(this)
        super.onCreate()
        initRecog()
        Vog.v(this, "开启服务")
    }

    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    protected fun initRecog() {
        val listener = MessageStatusRecogListener(handler)
        myRecognizer = MyRecognizer(this, listener)

        status = STATUS_NONE
        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams())
        }
    }

    internal fun start() {
        val pa = mapOf(
                Pair("accept-audio-data", false),
                Pair("disable-punctuation", false),
                Pair("accept-audio-volume", true),
                Pair("pid", 15361)
        )
        myRecognizer.start(pa)
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

    override fun onDestroy() {
        myRecognizer.release()
        Bus.unreg(this)
        super.onDestroy()
    }

}
