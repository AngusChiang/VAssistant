package cn.vove7.jarvis.speech

import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.support.annotation.CallSuper
import android.support.v4.app.ActivityCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.VoiceData
import cn.vove7.common.model.RequestPermission
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.statusbar.StatusAnimation
import cn.vove7.jarvis.view.statusbar.WakeupStatusAnimation
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils

/**
 * #SpeechRecoService
 * 语音识别接口
 *
 * Created by Administrator on 2018/11/4
 */
abstract class SpeechRecoService(val event: SpeechEvent) : SpeechRecoI {
    val context: Context
        get() = GlobalApp.APP
    //定时器关闭标志
    override var timerEnd: Boolean = false
    protected val wakeupStatusAni: StatusAnimation by lazy { WakeupStatusAnimation() }

    abstract var enableOffline: Boolean

    var isListening = false

    /**
     * 重写函数结尾处callSuper
     */
    override fun startRecog(byVoice: Boolean) {
        //检查权限
        if (!checkRecoderPermission()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.canDrawOverlays(context)) {
            Vog.d(this, "show ---> 无悬浮窗")
            AppBus.post(RequestPermission("悬浮窗权限"))
            return
        }
        Vog.d(this, "startRecog ---> 这里")
        Thread.sleep(80)
        if (!isListening) {
            isListening = true
            event.onStartRecog(byVoice)
            doStartRecog()
        } else {
            Vog.d(this, "启动失败，正在识别")
        }
    }

    /**
     * 开始识别
     */
    abstract fun doStartRecog()

    /**
     * 取消识别
     * @param notify Boolean
     */
    override fun cancelRecog(notify: Boolean) {
        isListening = false
        doCancelRecog()
        if (notify)
            event.onCancelRecog()
    }

    abstract fun doCancelRecog()


    override fun startWakeUp() {
        wakeupStatusAni.showAndHideDelay("语音唤醒开启", 5000)
    }

    override fun stopWakeUp() {
        if (wakeupI.opened)
            wakeupStatusAni.failedAndHideDelay("语音唤醒关闭", 5000)
    }

    abstract fun doStopRecog()


    @CallSuper
    override fun stopRecog() {
        isListening = false
        doStopRecog()
        event.onStopRecog()
    }

    /**
     * 定时关闭语音唤醒任务
     */
    private val stopWakeUpTimer = Runnable {
        timerEnd = true
        wakeupStatusAni.failed("语音唤醒已自动休眠")
        stopWakeUpSilently()//不通知
    }

    val timerHandler: Handler by lazy {
        val t = HandlerThread("auto_sleep")
        t.start()
        Handler(t.looper)
    }

    /**
     * 开启定时关闭
     * 重启定时器
     */
    fun startAutoSleepWakeup() {
        timerEnd = false
        if (PowerEventReceiver.isCharging) {
            Vog.d(this, "startAutoSleepWakeup ---> 充电中")
            return
        }
        stopAutoSleepWakeup()
        val sleepTime = /*if (BuildConfig.DEBUG) AppConfig.autoSleepWakeupMillis / 60
        else*/ AppConfig.autoSleepWakeupMillis
        Vog.d(this, "startAutoSleepWakeup ---> 开启自动休眠 $sleepTime")
        timerHandler.postDelayed(stopWakeUpTimer, sleepTime)
    }

    //关闭定时器
    fun stopAutoSleepWakeup() {
        Vog.d(this, "stopAutoSleepWakeup ---> 关闭自动休眠")
        timerHandler.removeCallbacks(stopWakeUpTimer)
    }

    fun checkRecoderPermission(jump: Boolean = true): Boolean {
        return (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                .also {
                    if (!it && jump)
                        AppBus.post(RequestPermission("麦克风权限"))
                }
    }

    /**
     * 语音事件分发[中枢]
     * @constructor
     */
    inner class RecogHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                IStatus.CODE_WAKEUP_SUCCESS -> {//唤醒
                    val word = msg.data.getString("data")
                    startAutoSleepWakeup()//重新倒计时
                    if (!isListening)
                        event.onWakeup(word)
//                    AppBus.postVoiceData(VoiceData(msg.what, word))
                }
                IStatus.CODE_VOICE_TEMP -> {//中间结果
                    val res = msg.data.getString("data")
                    if (res != null)
                        event.onTempResult(res)
//                    AppBus.postVoiceData(VoiceData(msg.what, res))
                }
                IStatus.CODE_VOICE_ERR -> {//出错
                    val res = msg.data.getString("data") ?: "null"
                    isListening = false
                    event.onRecogFailed(res)
//                    AppBus.postVoiceData(VoiceData(msg.what, res))
                }
                IStatus.CODE_VOICE_VOL -> {//音量反馈
                    val data = msg.data.getSerializable("data") as VoiceData
                    event.onVolume(data)
//                    AppBus.postVoiceData(data)
                }
                IStatus.CODE_VOICE_RESULT -> {//结果
                    val result = msg.data.getString("data") ?: "null"
                    event.onResult(result)
                    isListening = false
//                    AppBus.postVoiceData(VoiceData(msg.what, result))
                }
            }
        }
    }
}

interface SpeechRecoI {
    val wakeupI: WakeupI
    var timerEnd: Boolean
    fun startRecog(byVoice: Boolean = false)
    fun cancelRecog(notify: Boolean = true)
    /**
     * 重新计时
     */
    fun startWakeUp()

    /**
     * 不重新计时
     * 开启语音唤醒/暂时性
     * @param resetTimer Boolean
     */
    fun startWakeUpSilently(resetTimer: Boolean = true)

    /**
     * 关闭计时
     */
    fun stopWakeUp()

    /**
     * 不重新计时
     * 关闭语音唤醒/暂时性
     */
    fun stopWakeUpSilently()

    fun release()

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    fun stopRecog()
}