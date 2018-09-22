package cn.vove7.jarvis.speech.wakeup

import android.content.Context
import android.content.pm.PackageManager
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.vtp.log.Vog
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONObject

/**
 * Created by fujiayi on 2017/6/20.
 */

class MyWakeup {

    private var appId: Int
    private var appKey: String
    private var secretKey: String

    private var wp: EventManager? = null
    private val eventListener: EventListener

    constructor(context: Context, eventListener: EventListener) {
        if (isInited) {
            Vog.e(this, "还未调用release()，请勿新建一个新类")
//            throw RuntimeException("还未调用release()，请勿新建一个新类")
            instances?.release()
        }
        instances = this
        isInited = true
        this.eventListener = eventListener
        wp = EventManagerFactory.create(context, "wp")
        wp!!.registerListener(eventListener)

        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)

        appId = appInfo.metaData.getInt("com.baidu.speech.APP_ID")
        appKey = appInfo.metaData.getString("com.baidu.speech.API_KEY")
        secretKey = appInfo.metaData.getString("com.baidu.speech.SECRET_KEY")

        LuaUtil.assetsToSD(context, "bd/WakeUp.bin",
                context.filesDir.absolutePath + "/bd/WakeUp.bin")
    }

    constructor(context: Context, eventListener: IWakeupListener) :
            this(context, WakeupEventAdapter(eventListener))

    fun start() {
        //check recorder


        val params = HashMap<String, Any>()
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///bd/WakeUp.bin")
        params.put(SpeechConstant.APP_ID, appId)
        params.put(SpeechConstant.APP_KEY, appKey)
        params.put(SpeechConstant.SECRET, secretKey)
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        // params.put(SpeechConstant.ACCEPT_AUDIO_DATA,true);
        // params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,true);
        // params.put(SpeechConstant.IN_FILE,"res:///com/baidu/android/voicedemo/wakeup.pcm");
        start(params)
    }

    fun start(params: Map<String, Any>) {
        val json = JSONObject(params).toString()
        Vog.i(this, "wakeup params(反馈请带上此行日志):$json")
        wp!!.send(SpeechConstant.WAKEUP_START, json, null, 0, 0)
    }

    fun stop() {
        wp!!.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0)
    }

    fun release() {
        stop()
        wp!!.unregisterListener(eventListener)
        wp = null
        isInited = false
    }

    companion object {
        private var isInited = false
        var instances: MyWakeup? = null
    }
}
