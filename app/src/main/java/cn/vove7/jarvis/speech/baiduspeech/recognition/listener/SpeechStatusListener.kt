package cn.vove7.jarvis.speech.baiduspeech.recognition.listener

import android.os.Handler
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.appbus.VoiceData
import cn.vove7.common.model.RequestPermission
import cn.vove7.jarvis.speech.baiduspeech.recognition.message.SpeechMessage
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_ERR
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_RESULT
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_TEMP
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.IStatus.Companion.CODE_VOICE_VOL
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.RecogResult
import cn.vove7.vtp.log.Vog

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

//    override fun onAsrEnd() {
//        super.onAsrEnd()
////        if (!AppConfig.lastingVoiceCommand) {//非长语音
////            //立即停止识别 ，检测结果
////            MainService.instance?.onCommand(AppBus.ORDER_STOP_RECOG)
////        }
//    }

    override fun onAsrFinishError(errorCode: Int, subErrorCode: Int, errorMessage: String?, descMessage: String?,
                                  recogResult: RecogResult) {
        super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult)
        val message = "识别错误, 错误码：$errorCode,$subErrorCode,$descMessage,$errorMessage"
        GlobalLog.err(message)
        val errMsg = when (errorCode) {
            9 -> {
                AppBus.post(RequestPermission("麦克风权限"))
                "需要麦克风权限"
            }
            3 -> "没有检测到用户说话"
            7 -> "无结果"
            2 -> "网络错误"
            8 -> "引擎忙"
            1 -> "网络超时"
            else -> {
                "未知错误"
            }
        }

        handler.sendMessage(SpeechMessage.buildMessage(CODE_VOICE_ERR, errMsg))
        when (subErrorCode) {
            3101 -> {
                GlobalApp.toastInfo("?")
            }
            7001 -> {
            }
            8001 -> {//引擎忙
                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_CANCEL_RECOG)
            }
            3001 -> {
                handler.sendMessage(SpeechMessage.buildMessage(CODE_VOICE_ERR, "麦克风打开失败"))
            }
            2004, 2100 -> {
                GlobalApp.toastError("网络错误")
            }
//            else -> {
//                GlobalApp.toastInfo("未知错误")
//            }
        }
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

    override fun onAsrExit() {
        super.onAsrExit()
//        if (!isSuccess) {
//            Vog.d("识别失败")
//            handler.sendMessage(SpeechMessage.buildMessage(WHAT_VOICE_ERR, "无结果"))
//        }
    }
}