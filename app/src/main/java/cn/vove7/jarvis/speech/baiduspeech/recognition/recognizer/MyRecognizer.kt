package cn.vove7.jarvis.speech.baiduspeech.recognition.recognizer

import android.content.Context
import cn.vove7.jarvis.speech.baiduspeech.recognition.listener.IRecogListener
import cn.vove7.vtp.log.Vog
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONObject
import java.lang.Thread.sleep

/**
 * 初始化 提供 EventManagerFactory需要的Context和EventListener
 *
 * @param context
 * @param eventListener
 */
class MyRecognizer(
        context: Context,
        /**
         * SDK 内部核心 事件回调类， 用于开发者写自己的识别回调逻辑
         */
        private val eventListener: EventListener
) {
    /**
     * SDK 内部核心 EventManager 类
     */
    var asr: EventManager

    /**
     * 初始化
     *
     * @param context
     * @param recogListener 将EventListener结果做解析的DEMO回调。使用RecogEventAdapter 适配EventListener
     */
    constructor(context: Context, recogListener: IRecogListener) : this(context, RecogEventAdapter(recogListener)) {}

    init {
        if (isInited) {
            release()//释放，重新加载
        }
        asr = EventManagerFactory.create(context, "asr")
        asr.registerListener(eventListener)
        isInited = true
    }

    /**
     * @param params
     */
    fun loadOfflineEngine(params: Map<String, Any>) {
        val json = JSONObject(params).toString()
        Vog.i("loadOfflineEngine params:$json")
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, json, null, 0, 0)
        isOfflineEngineLoaded = true
        // 没有ASR_KWS_LOAD_ENGINE这个回调表试失败，如缺少第一次联网时下载的正式授权文件。
    }

    fun start(params: Map<String, Any>) {
        val json = JSONObject(params).toString()
        asr.send(SpeechConstant.ASR_START, json, null, 0, 0)
    }

    fun start(json: String) {
        Vog.i("asr params(反馈请带上此行日志):$json")
        asr.send(SpeechConstant.ASR_START, json, null, 0, 0)
    }

    /**
     * 加载离线词
     * @param param Map<String, Any>
     */
    fun loadOfWord(param: Map<String, Any>) {
        Vog.d("loadOfWord ---> 加载离线命令词 ${param}")
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, JSONObject(param).toString(), null, 0, 0)
    }

    /**
     * 提前结束录音等待识别结果。
     */
    fun stop() {
        Vog.i("停止录音")
        asr.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0)
    }

    /**
     * 取消本次识别，取消后将立即停止不会返回识别结果。
     * cancel 与stop的区别是 cancel在stop的基础上，完全停止整个识别流程，
     */
    fun cancel() {
        Vog.i("取消识别")
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0)
    }

    fun release() {
        cancel()
        if (isOfflineEngineLoaded) {
            asr.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0)
            isOfflineEngineLoaded = false
        }
        asr.unregisterListener(eventListener)
//        asr = null
        isInited = false
    }

    companion object {
        private var isOfflineEngineLoaded = false
        private var isInited = false
    }
}
