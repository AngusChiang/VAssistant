package cn.vove7.jarvis.speech.baiduspeech.recognition.listener

import android.os.Handler
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.speech.SpeechEvent.Companion.CODE_ENGINE_BUSY
import cn.vove7.jarvis.speech.SpeechEvent.Companion.CODE_NET_ERROR
import cn.vove7.jarvis.speech.SpeechEvent.Companion.CODE_NO_RECORDER_PERMISSION
import cn.vove7.jarvis.speech.SpeechEvent.Companion.CODE_NO_RESULT
import cn.vove7.jarvis.speech.SpeechEvent.Companion.CODE_RECORDER_OPEN_FAIL
import cn.vove7.jarvis.speech.SpeechEvent.Companion.CODE_UNKNOWN
import cn.vove7.jarvis.speech.VoiceData
import cn.vove7.jarvis.speech.baiduspeech.recognition.message.SpeechMessage
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_ERR
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_READY
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_RESULT
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_TEMP
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_VOL
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.STATUS_FINISHED
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.RecogResult
import cn.vove7.vtp.log.Vog
import kotlin.math.absoluteValue

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
class SpeechStatusListener(private val handler: Handler) : StatusRecogListener() {

    var isSuccess = false
    override fun onAsrBegin() {
        super.onAsrBegin()
        isSuccess = false
    }

    override fun onAsrReady() {
        super.onAsrReady()
        Vog.d("ready")
        handler.sendMessage(SpeechMessage.buildMessage(CODE_VOICE_READY))
    }

    override fun onAsrPartialResult(results: Array<String>?, recogResult: RecogResult) {
        super.onAsrPartialResult(results, recogResult)
        val tmp = results?.get(0) ?: ""
        handler.sendMessage(SpeechMessage.buildMessage(CODE_VOICE_TEMP, tmp))
    }

    override fun onAsrFinalResult(results: Array<String>?, recogResult: RecogResult) {
        super.onAsrFinalResult(results, recogResult)
        isSuccess = true
        val tmp = results?.get(0) ?: ""
        handler.sendMessage(SpeechMessage.buildMessage(CODE_VOICE_RESULT, tmp))
    }

    override fun onAsrFinishError(errorCode: Int, subErrorCode: Int, errorMessage: String?, descMessage: String?,
                                  recogResult: RecogResult) {
        super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult)
        val message = "识别错误, 错误码：$errorCode,$subErrorCode,$descMessage,$errorMessage"
        GlobalLog.err(message)
        val errCode = when (errorCode.absoluteValue) {
            9 -> {
                CODE_NO_RECORDER_PERMISSION
            }
            3 -> {
                if (subErrorCode == 3001) CODE_RECORDER_OPEN_FAIL
                else CODE_NO_RESULT
            }
            7 -> CODE_NO_RESULT
            2 -> CODE_NET_ERROR
            8 -> CODE_ENGINE_BUSY //"引擎忙"
            1 -> CODE_NET_ERROR //"网络超时"
            else -> {
                CODE_UNKNOWN//"未知错误"
            }
        }

        handler.sendMessage(SpeechMessage.buildMessage(CODE_VOICE_ERR, errCode))
    }

    override fun onAsrVolume(volumePercent: Int, volume: Int) {
        Vog.v("音量百分比$volumePercent ; 音量$volume")
        handler.sendMessage(
                SpeechMessage.buildMessage(
                        CODE_VOICE_VOL,
                        VoiceData(CODE_VOICE_VOL, volumePercent = volumePercent)
                )
        )
    }

    override fun onAsrFinish(recogResult: RecogResult) {
        super.onAsrFinish(recogResult)
        handler.sendMessage(SpeechMessage.buildMessage(STATUS_FINISHED, ""))

    }

    override fun onAsrExit() {
        super.onAsrExit()
//        if (!isSuccess) {
//            Vog.d("识别失败")
//            handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_ERR, "无结果"))
//        }
    }
}