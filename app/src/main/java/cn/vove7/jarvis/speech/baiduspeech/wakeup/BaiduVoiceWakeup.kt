package cn.vove7.jarvis.speech.baiduspeech.wakeup

import android.content.Context
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.tools.BaiduKey
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

class BaiduVoiceWakeup(private val eventListener: EventListener) : WakeupI() {

    private var appId: Int = 0
    private lateinit var appKey: String
    private lateinit var secretKey: String
    val context: Context
        get() = GlobalApp.APP

    private var wp: EventManager? = null


    init {
        initIfNeed()
    }

    private fun initIfNeed() {
        if (wp != null) return
        wp = EventManagerFactory.create(context, "wp")
        wp?.registerListener(eventListener)

        appId = BaiduKey.appId
        appKey = BaiduKey.appKey
        secretKey = BaiduKey.sKey
        LuaUtil.assetsToSD(context, "bd/WakeUp_xvtx.bin",
                context.filesDir.absolutePath + "/bd/WakeUp_xvtx.bin")
    }

    override fun doStart() {
        initIfNeed()
        val params = HashMap<String, Any?>()

        if (!AppConfig.recogCompatibleMode) {
            params[SpeechConstant.IN_FILE] = "#cn.vove7.jarvis.speech.baiduspeech.MicInputStream.instance()"
        }
        params[SpeechConstant.WP_WORDS_FILE] = AppConfig.wakeUpFilePath
        params[SpeechConstant.APP_ID] = appId
        params[SpeechConstant.APP_KEY] = appKey
        params[SpeechConstant.SECRET] = secretKey

        // params.put(SpeechConstant.ACCEPT_AUDIO_DATA,true);
        // params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,true);
        send(params)
    }

    private fun send(params: Map<String, Any?>) {
        val json = JSONObject(params).toString()
        Vog.i("wakeup params(反馈请带上此行日志):$json")
        wp?.send(SpeechConstant.WAKEUP_START, json, null, 0, 0)
    }

    /**
     * 停止即释放
     */
    override fun doStop() {
        if (wp == null) return
        wp?.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0)
        release()
    }

    override fun release() {
        wp?.unregisterListener(eventListener)
        wp = null
    }

//    companion object {
//        var instances: BaiduVoiceWakeup? = null
//    }
}
