package cn.vove7.jarvis.speech

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.*
import android.support.annotation.CallSuper
import android.support.v4.app.ActivityCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.statusbar.StatusAnimation
import cn.vove7.jarvis.view.statusbar.WakeupStatusAnimation
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils


/**
 * #SpeechRecoService
 * APP语音识别基类
 * 负责识别|唤醒 逻辑
 * Created by Administrator on 2018/11/4
 */
abstract class SpeechRecogService(val event: SpeechEvent) : SpeechRecogI {
    val context: Context
        get() = GlobalApp.APP

    //语音唤醒定时器关闭标志
    var wakeupTimerEnd: Boolean = false
        set(v) {
            Vog.d("语音唤醒定时器关闭标志：$v")
            field = v
        }
    private val wakeupStatusAni: StatusAnimation
            by lazy { WakeupStatusAnimation() }
    private val audioManager: AudioManager
        get() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    abstract var enableOffline: Boolean

    var isListening = false

    var isSilent = false

    //长语音
    var lastingStopped = true

    override fun startRecog(byVoice: Boolean, notify: Boolean) {
        //检查权限
        if (!checkRecorderPermission() || !checkFloat()) return
        Thread.sleep(80)
        if (!isListening) {
            lastingStopped = false
            isListening = true
            isSilent = !notify
            if (notify) {
                event.onPreStartRecog(byVoice)
            } else {
                Vog.d("静默识别")
            }
            doStartRecog(!notify)
        } else {
            Vog.d("启动失败，正在识别")
        }
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
     * 取消识别
     * @param notify Boolean
     */
    override fun cancelRecog(notify: Boolean) {
        lastingStopped = true
        isListening = false
        closeSCO()
        doCancelRecog()
        if (notify)
            event.onCancelRecog()
    }

    final override fun startWakeUp(notify: Boolean, resetTimer: Boolean) {
        if (!wakeupI.opened) {
            if (notify)
                wakeupStatusAni.showAndHideDelay("语音唤醒开启", 5000)
            doStartWakeup()

            if (resetTimer)//定时器结束
                startAutoSleepWakeup()
        }
    }


    override fun doStartWakeup() {
        wakeupI.start()
    }

    override fun doStopWakeUp() {
        wakeupI.stop()
    }

    final override fun stopWakeUp(notify: Boolean, stopTimer: Boolean) {
        if (notify && (ScreenStatusListener.screenOn || AppConfig.notifyWpOnScreenOff)) //亮屏，或开启息屏通知
            if (wakeupI.opened)
                wakeupStatusAni.failedAndHideDelay("语音唤醒关闭", 5000)

        if (wakeupI.opened) {
            if (stopTimer)
                stopAutoSleepWakeup()
            doStopWakeUp()
        }
    }


    @CallSuper
    override fun stopRecog(byUser: Boolean) {
        //用户停止识别，关闭长语音
        isListening = false
        if (byUser) lastingStopped = true
        closeSCO()
        doStopRecog()
        event.onStopRecog()
    }

    /**
     * 定时关闭语音唤醒任务
     */
    private val stopWakeUpAction = Runnable {
        wakeupTimerEnd = true
        if (wakeupI.opened) {
            if(AppConfig.voiceWakeup) {
                wakeupStatusAni.failed("语音唤醒已自动休眠")
            }
            doStopWakeUp()//不通知
        }
    }

    private val wpTimerHandler: Handler by lazy {
        val t = HandlerThread("auto_sleep")
        t.start()
        Handler(t.looper)
    }

    /**
     * 0.8s后无消息自动停止识别
     */
    private val stopRecogHandler: Handler by lazy {
        val t = HandlerThread("auto_stop_recog")
        t.start()
        Handler(t.looper)
    }
    private val stopRecogAction = Runnable {
        Vog.e("定时停止识别")
        if (isListening) {
            stopRecog(false)//不通知
        }
    }

    private fun restartStopTimer() {
        stopRecogHandler.removeCallbacks(stopRecogAction)
        if (isListening) {
            stopRecogHandler.postDelayed(stopRecogAction, 800)
        }

    }


    /**
     * 开启定时关闭
     * 重启定时器
     */
    fun startAutoSleepWakeup() {
        stopAutoSleepWakeup()
        if (PowerEventReceiver.isCharging && AppConfig.isAutoVoiceWakeupCharging) {
            Vog.d("startAutoSleepWakeup ---> 充电中 不自动休眠")
            return
        }
        val sleepTime = if (BuildConfig.DEBUG) AppConfig.autoSleepWakeupMillis / 60
        else AppConfig.autoSleepWakeupMillis
        wakeupTimerEnd = false
        Vog.d("休眠定时${sleepTime / 1000}s")
        if (sleepTime <= 0) return //不自动休眠
        wpTimerHandler.postDelayed(stopWakeUpAction, sleepTime)
    }

    //关闭定时器
    fun stopAutoSleepWakeup() {
        Vog.d("stopAutoSleepWakeup ---> 关闭唤醒自动休眠")
        wpTimerHandler.removeCallbacks(stopWakeUpAction)
    }

    private fun checkRecorderPermission(jump: Boolean = true): Boolean {
        return (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                .also {
                    if (!it && jump)
                        AppBus.post(RequestPermission("麦克风权限"))
                }
    }

    fun startIfLastingVoice() {//长语音
        runOnNewHandlerThread(delay = 500) {
            //            if (SystemBridge.isMediaPlaying()) {
//                Vog.d("重启长语音： 正在播放音乐")
//                return@runOnNewHandlerThread
//            }
            if (MainService.speaking) {
                Vog.d("重启长语音：speaking")
                return@runOnNewHandlerThread
            }
            if (isListening) {
                Vog.d("重启长语音： 正在识别")
                return@runOnNewHandlerThread
            }
            if (AppConfig.lastingVoiceCommand && !lastingStopped) {
                startRecog(notify = false)
            } else {
                Vog.d("重启长语音： 长语音关闭或已停止")
            }
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
                }
                IStatus.CODE_VOICE_READY -> {
                    event.onRecogReady(isSilent)
                    startSCO()
                }
                IStatus.CODE_VOICE_TEMP -> {//中间结果
                    restartStopTimer()
                    val res = msg.data.getString("data")
                    if (res != null)
                        event.onTempResult(res)
                }
                IStatus.CODE_VOICE_ERR -> {//出错
                    val code = msg.data.getInt("data")
                    isListening = false
                    lastingStopped = true
                    closeSCO()
                    event.onRecogFailed(code)
                }
                IStatus.CODE_VOICE_VOL -> {//音量反馈
                    val data = msg.data.getSerializable("data") as VoiceData
                    event.onVolume(data)
                }
                IStatus.CODE_VOICE_RESULT -> {//结果
                    stopRecogHandler.removeCallbacks(stopRecogAction)
                    val result = msg.data.getString("data") ?: "null"
                    isListening = false
                    event.onTempResult(result)
                    event.onResult(result)
                    closeSCO()//关闭SCO
                }
                IStatus.STATUS_FINISHED -> {
                    closeSCO()//关闭SCO
                    event.onFinish()
                }
            }
        }
    }


    private fun startSCO() {
        Vog.d("startSCO")
        runInCatch {
            audioManager.apply {
                isBluetoothScoOn = true
                mode = AudioManager.MODE_IN_CALL
                startBluetoothSco()
            }
        }
    }

    fun closeSCO() {
        Vog.d("关闭SCO")
        runInCatch {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        }
    }

}

interface SpeechRecogI {
    val wakeupI: WakeupI


    fun startRecog(byVoice: Boolean = false, notify: Boolean = true)
    fun cancelRecog(notify: Boolean = true)


    /**
     * 开始识别
     */
    fun doStartRecog(silent: Boolean = false)

    fun doCancelRecog()

    fun doStopRecog()


    /**
     * 重新计时
     */
    fun startWakeUp(notify: Boolean = true, resetTimer: Boolean = true)

    /**
     * 开启语音唤醒/暂时性
     * @param resetTimer Boolean
     */
    fun doStartWakeup()

    /**
     * 关闭计时
     */
    fun stopWakeUp(notify: Boolean = true, stopTimer: Boolean = true)

    /**
     * 不重新计时
     * 关闭语音唤醒/暂时性
     */
    fun doStopWakeUp()

    fun release()

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    fun stopRecog(byUser: Boolean = true)
}