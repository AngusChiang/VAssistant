package cn.vove7.jarvis.speech.baiduspeech.wakeup

import android.content.Context
import android.content.pm.PackageManager
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONObject

/**
 * Created by fujiayi on 2017/6/20.
 *
 * 唤醒定时关闭，not 充电
 *
 */

object MyWakeup {

    private var appId: Int = 0
    private lateinit var appKey: String
    private lateinit var secretKey: String
    val context: Context
        get() = GlobalApp.APP

    private var wp: EventManager? = null
    private lateinit var eventListener: EventListener
    var opened = false

    private fun init(eventListener: EventListener) {
        if (isInited) {
//            Vog.e(this, "还未调用release()，请勿新建一个新类")
//            throw RuntimeException("还未调用release()，请勿新建一个新类")
            return
//            instances?.release()
        }
//        this.context = context
//        instances = this
        isInited = true
        this.eventListener = eventListener
        wp = EventManagerFactory.create(context, "wp")
        wp!!.registerListener(eventListener)

        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        if (BuildConfig.DEBUG) {
            appId = 11389525
            appKey = "ILdLUepG75UwwQVa0rqiEUVa"
            secretKey = "di6djKXGGELgnCCusiQUlCBYRxXVrr46"
        } else {
            appId = appInfo.metaData.getInt("com.baidu.speech.APP_ID")
            appKey = appInfo.metaData.getString("com.baidu.speech.API_KEY")!!
            secretKey = appInfo.metaData.getString("com.baidu.speech.SECRET_KEY")!!
        }
        LuaUtil.assetsToSD(context, "bd/WakeUp_xvtx.bin",
                context.filesDir.absolutePath + "/bd/WakeUp_xvtx.bin")
    }

    fun start(eventL: WakeupEventAdapter) {
        if (wp == null) {
            init(eventL)
        }

        val params = HashMap<String, Any?>()
        params[SpeechConstant.WP_WORDS_FILE] = AppConfig.wakeUpFilePath
        params[SpeechConstant.APP_ID] = appId
        params[SpeechConstant.APP_KEY] = appKey
        params[SpeechConstant.SECRET] = secretKey
        params[SpeechConstant.IN_FILE] = "#cn.vove7.jarvis.tools.MicrophoneInputStream.getInstance()"

        // "assets:///WakeUp_xvtx.bin" 表示WakeUp.bin文件定义在assets目录下
        // params.put(SpeechConstant.ACCEPT_AUDIO_DATA,true);
        // params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,true);
        // params.put(SpeechConstant.IN_FILE,"res:///com/baidu/android/voicedemo/wakeup.pcm");
        start(params)
    }

    private fun start(params: Map<String, Any?>) {
        val json = JSONObject(params).toString()
        Vog.i(this, "wakeup params(反馈请带上此行日志):$json")
        wp?.send(SpeechConstant.WAKEUP_START, json, null, 0, 0)
        GlobalApp.toastShort("语音唤醒开启")
        opened = true
    }

    fun stop() {
        if (wp == null) return
        GlobalApp.toastShort("语音唤醒关闭")
        wp?.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0)
        release()
        opened = false
    }

    fun release() {
//        stop()
        wp?.unregisterListener(eventListener)
        wp = null
        isInited = false
    }

    private var isInited = false
//    companion object {
//        var instances: MyWakeup? = null
//    }
}
