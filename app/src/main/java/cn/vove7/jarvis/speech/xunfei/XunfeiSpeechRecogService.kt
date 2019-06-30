package cn.vove7.jarvis.speech.xunfei

import android.os.Bundle
import android.os.Handler
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.runInCatch
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.speech.RecogEvent
import cn.vove7.jarvis.speech.RecogEvent.Companion.CODE_NO_RESULT
import cn.vove7.jarvis.speech.RecogEvent.Companion.CODE_UNKNOWN
import cn.vove7.jarvis.speech.SpeechConst
import cn.vove7.jarvis.speech.SpeechRecogService
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.jarvis.speech.baiduspeech.recognition.message.SpeechMessage
import cn.vove7.vtp.log.Vog
import com.iflytek.cloud.*
import org.json.JSONObject
import org.json.JSONTokener

/**
 * # XunfeiSpeechRecogService
 * 讯飞语音识别服务
 *
 * @author Vove
 * 2019/6/23
 */
class XunfeiSpeechRecogService(event: RecogEvent) : SpeechRecogService(event) {


    private val mAsr by lazy {
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=5d0f2ed4")
        SpeechRecognizer.createRecognizer(context) {
            GlobalLog.err("讯飞初语音识别始化失败：$it")
        }.apply {
            setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            setParameter(SpeechConstant.ASR_PTT, "0")

        }
    }


    val listener by lazy { XunfeiRecogListener(this, handler) }
    override var enableOffline = false

    override val wakeupI: WakeupI by lazy { XunfeiVoiceWakeup() }

    override fun doStartRecog(silent: Boolean) {
        val ret = mAsr.startListening(listener)
        if (ret != ErrorCode.SUCCESS) {
            GlobalLog.err("识别开启失败：${SpeechError(ret).errorDescription}")
            event.onRecogFailed(xunfeiCode2Normal(ret))
        }
    }

    override fun doCancelRecog() {
        mAsr.cancel()
    }

    override fun doStopRecog() {
        mAsr.stopListening()
    }

    override fun doRelease() {
        runInCatch {
            mAsr.destroy()
        }
    }
}

class XunfeiRecogListener(val service: SpeechRecogService, val handler: Handler) : RecognizerListener {
    val buffer = StringBuffer()
    override fun onVolumeChanged(p0: Int, p1: ByteArray?) {
//        event.onVolume(VoiceData(volumePercent = p0))
    }

    override fun onResult(p0: RecognizerResult?, isLast: Boolean) {

        if (p0 == null || p0.resultString == null) {
            handler.sendMessage(SpeechMessage.buildMessage(SpeechConst.CODE_VOICE_ERR, CODE_NO_RESULT))
        } else {
            if (BuildConfig.DEBUG) {
                val s = JsonParser.parseGrammarResult(p0.resultString)
                Vog.d(s)
            }
            val result = p0.parseBestResult()

            if (result == null && isLast) {//解析失败
                handler.sendMessage(SpeechMessage.buildMessage(SpeechConst.CODE_VOICE_ERR, CODE_NO_RESULT))
                return
            }

            if (result?.contains("nomatch") == true && isLast) {
                handler.sendMessage(SpeechMessage.buildMessage(SpeechConst.CODE_VOICE_ERR, CODE_NO_RESULT))
                return
            }
            result ?: return
            buffer.append(result)
            val tmp = buffer.toString()
            if (isLast) {
                handler.sendMessage(SpeechMessage.buildMessage(SpeechConst.CODE_VOICE_RESULT, tmp))
                buffer.delete(0, buffer.length)
            } else {
                handler.sendMessage(SpeechMessage.buildMessage(SpeechConst.CODE_VOICE_TEMP, tmp))
            }
        }
    }

    override fun onBeginOfSpeech() {
        handler.sendMessage(SpeechMessage.buildMessage(SpeechConst.CODE_VOICE_READY))
    }

    override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {
        Vog.d("onEvent: ${p3?.toString()}")
    }

    override fun onEndOfSpeech() {
    }

    override fun onError(p0: SpeechError?) {
        GlobalLog.err(p0?.errorDescription)
        handler.sendMessage(SpeechMessage.buildMessage(SpeechConst.CODE_VOICE_ERR,
                xunfeiCode2Normal(p0?.errorCode ?: CODE_UNKNOWN)))
        buffer.delete(0, buffer.length)
    }

}

/**
 * 讯飞错误代码 转 APP内部代码
 * TODO
 * @param eCode Int
 */
fun xunfeiCode2Normal(eCode: Int): Int {
    return CODE_UNKNOWN

}

fun RecognizerResult.parseBestResult(): String? {
    return try {
        val joResult = JSONObject(JSONTokener(resultString))

        buildString {
            val words = joResult.getJSONArray("ws")

            for (i in 0 until words.length()) {
                val t= words.getJSONObject(i).getJSONArray("cw")
                        .getJSONObject(0).getString("w")
                append(t)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

}

/**
//设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
mIat.setParameter( SpeechConstant.CLOUD_GRAMMAR, null );
mIat.setParameter( SpeechConstant.SUBJECT, null );
//设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
//此处engineType为“cloud”
mIat.setParameter( SpeechConstant.ENGINE_TYPE, engineType );
//设置语音输入语言，zh_cn为简体中文
mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//设置结果返回语言
mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
// 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
//取值范围{1000～10000}
mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
//设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
//自动停止录音，范围{0~10000}
mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
//设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
mIat.setParameter(SpeechConstant.ASR_PTT,"1");

//开始识别，并设置监听器
mIat.startListening(mRecogListener);
 */