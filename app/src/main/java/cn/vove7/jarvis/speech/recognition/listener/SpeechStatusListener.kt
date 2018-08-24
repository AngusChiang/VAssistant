package cn.vove7.jarvis.speech.recognition.listener

import android.os.Handler
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.SpeechRecoAction
import cn.vove7.appbus.VoiceData
import cn.vove7.common.model.RequestPermission
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_ERR
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_RESULT
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_TEMP
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_VOL
import cn.vove7.jarvis.speech.recognition.message.SpeechMessage
import cn.vove7.jarvis.speech.recognition.model.RecogResult
import cn.vove7.vtp.log.Vog

/**
 *
 *运算器
 * Created by Vove on 2018/6/18
 */
class SpeechStatusListener(private val handler: Handler) : StatusRecogListener() {

    var isSuccess = false
    override fun onAsrBegin() {
        super.onAsrBegin()
        isSuccess = false
    }

    override fun onAsrPartialResult(results: Array<String>?, recogResult: RecogResult) {
        super.onAsrPartialResult(results, recogResult)
        val tmp = results?.get(0) ?: ""
        handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_TEMP, tmp))
    }

    override fun onAsrFinalResult(results: Array<String>?, recogResult: RecogResult) {
        super.onAsrFinalResult(results, recogResult)
        isSuccess = true
        val tmp = results?.get(0) ?: ""
        handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_RESULT, tmp))
    }

    var endFinish = true
    override fun onAsrEnd() {
        super.onAsrEnd()
        //立即停止识别 ，检测结果
        AppBus.postSpeechRecoAction(SpeechRecoAction.ActionCode.ACTION_STOP_RECO)

    }

    override fun onAsrFinishError(errorCode: Int, subErrorCode: Int, errorMessage: String?, descMessage: String?,
                                  recogResult: RecogResult) {
        super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult)
        val message = "识别错误, 错误码：$errorCode,$subErrorCode"
        when (errorCode) {
            9 -> {
                AppBus.post(RequestPermission("麦克风权限"))
                handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_ERR, "需要麦克风权限"))
            }
            3 -> {
                handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_ERR, "没有检测到用户说话"))
            }
            else ->
                handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_ERR, message))
        }
    }

    override fun onAsrVolume(volumePercent: Int, volume: Int) {
        Vog.v(this, "音量百分比$volumePercent ; 音量$volume")
        handler.sendMessage(
                SpeechMessage.buildMessage(
                        WHAT_VOICE_VOL,
                        VoiceData(WHAT_VOICE_VOL, volumePercent = volumePercent)
                )
        )
    }

    override fun onAsrExit() {
        super.onAsrExit()
//        if (!isSuccess) {
//            Vog.d(this, "识别失败")
//            handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_ERR, "无结果"))
//        }
    }
}