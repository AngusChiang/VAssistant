package cn.vove7.jarvis.speech.recognition.recognizer

import cn.vove7.jarvis.speech.recognition.model.ErrorTranslation
import cn.vove7.jarvis.speech.recognition.listener.IRecogListener
import cn.vove7.jarvis.speech.recognition.model.RecogResult
import cn.vove7.vtp.log.Vog
import com.baidu.speech.EventListener
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONException
import org.json.JSONObject


class RecogEventAdapter(private val listener: IRecogListener) : EventListener {

    private var currentJson: String? = null

    override fun onEvent(name: String?, params: String?, data: ByteArray?, offset: Int, length: Int) {
        currentJson = params
        val logMessage = "name:$name; params:$params"

        Vog.v(this, logMessage)

        if (name == SpeechConstant.CALLBACK_EVENT_ASR_LOADED) {
            listener.onOfflineLoaded()
        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_UNLOADED) {
            listener.onOfflineUnLoaded()
        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_READY) {
            // 引擎准备就绪，可以开始说话
            listener.onAsrReady()

        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_BEGIN) {
            // 检测到用户的已经开始说话
            listener.onAsrBegin()

        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_END) {
            // 检测到用户的已经停止说话
            listener.onAsrEnd()

        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL) {
            val recogResult = RecogResult.parseJson(params)
            // 临时识别结果, 长语音模式需要从此消息中取出结果
            val results = recogResult.resultsRecognition
            when {
                recogResult.isFinalResult -> listener.onAsrFinalResult(results, recogResult)
                recogResult.isPartialResult -> listener.onAsrPartialResult(results, recogResult)
                recogResult.isNluResult -> listener.onAsrOnlineNluResult(String(data!!, offset, length))
            }

        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_FINISH) {
            // 识别结束， 最终识别结果或可能的错误
            val recogResult = RecogResult.parseJson(params)
            if (recogResult.hasError()) {
                val errorCode = recogResult.error
                val subErrorCode = recogResult.subError
                Vog.e(this, "asr error:$params")
                listener.onAsrFinishError(errorCode, subErrorCode, ErrorTranslation.recogError(errorCode), recogResult.desc, recogResult)
            } else {
                listener.onAsrFinish(recogResult)
            }

        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_LONG_SPEECH) { //长语音
            listener.onAsrLongFinish()// 长语音
        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_EXIT) {
            listener.onAsrExit()
        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_VOLUME) {
            val vol = parseVolumeJson(params)
            listener.onAsrVolume(vol.volumePercent, vol.volume)
        } else if (name == SpeechConstant.CALLBACK_EVENT_ASR_AUDIO) {
            if (data?.size != length) {
                Vog.e(this, "internal error: asr.audio callback data length is not equal to length param")
            }
            listener.onAsrAudio(data, offset, length)
        }
    }

    private fun parseVolumeJson(jsonStr: String?): Volume {
        val vol = Volume()
        vol.origalJson = jsonStr
        try {
            val json = JSONObject(jsonStr)
            vol.volumePercent = json.getInt("volume-percent")
            vol.volume = json.getInt("volume")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return vol
    }

    private inner class Volume {
        var volumePercent = -1
        var volume = -1
        var origalJson: String? = null
    }
}
