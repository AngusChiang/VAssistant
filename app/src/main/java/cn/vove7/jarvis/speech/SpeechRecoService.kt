package cn.vove7.jarvis.speech

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.*
import android.support.annotation.CallSuper
import android.support.v4.app.ActivityCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.VoiceData
import cn.vove7.common.model.RequestPermission
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
abstract class SpeechRecoService(val event: SpeechEvent) : SpeechRecogI {
    val context: Context
        get() = GlobalApp.APP
    //定时器关闭标志
    override var timerEnd: Boolean = false
    protected val wakeupStatusAni: StatusAnimation by lazy { WakeupStatusAnimation() }
    val audioManager: AudioManager get() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    abstract var enableOffline: Boolean

    var isListening = false

    /**
     * 重写函数结尾处callSuper
     * TODO 监听蓝牙
     */
    override fun startRecog(byVoice: Boolean) {
        //检查权限
        if (!checkRecoderPermission() || !checkFloat()) return
        Thread.sleep(80)
        if (!isListening) {
            isListening = true
            event.onPreStartRecog(byVoice)
            startSCO()
            checkAudioSource()
        } else {
            Vog.d("启动失败，正在识别")
        }
    }

    private fun checkAudioSource() {
        doStartRecog()
    }

    private fun startSCO() {
        Vog.d("startSCO")

        audioManager.apply {
            isBluetoothScoOn = true
            mode = AudioManager.MODE_IN_CALL
            startBluetoothSco()
        }
    }

    fun closeSCO() {
        Vog.d("关闭SCO")
        audioManager.isBluetoothScoOn = false
        audioManager.stopBluetoothSco()
    }


    /**
     * 静默开启
     * 一般在speak完后再次开启
     */
    override fun startRecogSilent() {
        if (!checkRecoderPermission() || !checkFloat()) return
        isListening = true
        checkAudioSource()
    }

    private fun checkFloat(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.canDrawOverlays(context)) {
            Vog.d("show ---> 无悬浮窗")
            AppBus.post(RequestPermission("悬浮窗权限"))
            return false
        }
        return true
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
        closeSCO()
        doCancelRecog()
        if (notify)
            event.onCancelRecog()
    }

    abstract fun doCancelRecog()

    @CallSuper
    override fun startWakeUp() {
        if (!wakeupI.opened)
            wakeupStatusAni.showAndHideDelay("语音唤醒开启", 5000)
    }

    @CallSuper
    override fun stopWakeUp() {
        if (wakeupI.opened)
            wakeupStatusAni.failedAndHideDelay("语音唤醒关闭", 5000)
    }

    abstract fun doStopRecog()


    @CallSuper
    override fun stopRecog() {
        isListening = false
        closeSCO()
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

    protected val timerHandler: Handler by lazy {
        val t = HandlerThread("auto_sleep")
        t.start()
        Handler(t.looper)
    }

    /**
     * 长语音定时关闭  若支持
     * 在识别成功后，重新定时
     * speak后doStartRecog，操作后？？？
     */
    open fun restartLastingUpTimer() {}

    /**
     * 长语音关闭定时器  若支持
     */
    open fun stopLastingUpTimer() {}

    /**
     * 开启定时关闭
     * 重启定时器
     */
    fun startAutoSleepWakeup() {
        timerEnd = false
        if (PowerEventReceiver.isCharging) {
            Vog.d("startAutoSleepWakeup ---> 充电中")
            return
        }
        stopAutoSleepWakeup()
        val sleepTime = /*if (BuildConfig.DEBUG) AppConfig.autoSleepWakeupMillis / 60
        else*/ AppConfig.autoSleepWakeupMillis
        Vog.d("startAutoSleepWakeup ---> 开启唤醒自动休眠 $sleepTime")
        if (sleepTime < 0) return
        timerHandler.postDelayed(stopWakeUpTimer, sleepTime)
    }

    //关闭定时器
    fun stopAutoSleepWakeup() {
        Vog.d("stopAutoSleepWakeup ---> 关闭唤醒自动休眠")
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
                    Vog.d("handleMessage ---> 唤醒 -> $word")
                    startAutoSleepWakeup()//重新倒计时
                    if (!isListening)
                        event.onWakeup(word)
                    else {
                        Vog.e("正在聆听,暂停唤醒")
                    }
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
                    closeSCO()
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
                    event.onTempResult(result)
                    closeSCO()//关闭SCO
                    event.onResult(result)
                    if (!AppConfig.lastingVoiceCommand)
                        isListening = false
//                    AppBus.postVoiceData(VoiceData(msg.what, result))
                }
                IStatus.STATUS_FINISHED -> {
                    closeSCO()//关闭SCO
                    event.onFinish()
                }
            }
        }
    }
}

interface SpeechRecogI {
    val wakeupI: WakeupI
    var timerEnd: Boolean
    fun startRecog(byVoice: Boolean = false)
    fun startRecogSilent()
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