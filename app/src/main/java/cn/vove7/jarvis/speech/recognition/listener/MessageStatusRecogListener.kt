package cn.vove7.jarvis.speech.recognition.listener

import android.os.Handler
import android.os.Message
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_RESULT
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_TEMP
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_VOL
import cn.vove7.jarvis.speech.recognition.model.IStatus

import cn.vove7.jarvis.speech.recognition.model.RecogResult
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.VoiceData


class MessageStatusRecogListener(private val handler: Handler) : StatusRecogListener() {

    private var speechEndTime: Long = 0

    private val needTime = true


    override fun onAsrReady() {
        super.onAsrReady()
        sendStatusMessage("引擎就绪，可以开始说话。")
    }

    override fun onAsrBegin() {
        super.onAsrBegin()
        sendStatusMessage("检测到用户说话")
    }

    override fun onAsrEnd() {
        super.onAsrEnd()
        speechEndTime = System.currentTimeMillis()
        sendMessage("检测到用户说话结束")
    }

    override fun onAsrVolume(volumePercent: Int, volume: Int) {
        super.onAsrVolume(volumePercent, volume = volume)
        AppBus.postVoiceData(VoiceData(WHAT_VOICE_VOL, volumePercent = volumePercent))
    }

    override fun onAsrPartialResult(results: Array<String>?, recogResult: RecogResult) {
        sendStatusMessage("临时识别结果，结果是“" + results!![0] + "”；原始json：" + recogResult.originalJson)
        super.onAsrPartialResult(results, recogResult)

        AppBus.postVoiceData(VoiceData(WHAT_VOICE_TEMP, results[0]))
    }

    override fun onAsrFinalResult(results: Array<String>?, recogResult: RecogResult) {
        super.onAsrFinalResult(results, recogResult)
        var message = "识别结束，结果是”" + results!![0] + "”"
        sendStatusMessage(message + "“；原始json：" + recogResult.originalJson)
        if (speechEndTime > 0) {
            val diffTime = System.currentTimeMillis() - speechEndTime
            message += "；说话结束到识别结束耗时【" + diffTime + "ms】"
        }
        AppBus.postVoiceData(VoiceData(WHAT_VOICE_RESULT, results[0]))
        speechEndTime = 0
        sendMessage(message, status, true)
    }

    override fun onAsrFinishError(errorCode: Int, subErrorCode: Int, errorMessage: String?, descMessage: String?,
                                  recogResult: RecogResult) {
        super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult)
        var message = "识别错误, 错误码：$errorCode,$subErrorCode"
        sendStatusMessage("$message；错误消息:$errorMessage；描述信息：$descMessage")
        if (speechEndTime > 0) {
            val diffTime = System.currentTimeMillis() - speechEndTime
            message += "。说话结束到识别结束耗时【" + diffTime + "ms】"
        }
        speechEndTime = 0
        sendMessage(message, status, true)
        speechEndTime = 0
    }

    override fun onAsrOnlineNluResult(nluResult: String) {
        super.onAsrOnlineNluResult(nluResult)
        if (!nluResult.isEmpty()) {
            sendStatusMessage("原始语义识别结果json：$nluResult")
        }
    }

    override fun onAsrFinish(recogResult: RecogResult) {
        super.onAsrFinish(recogResult)
        sendStatusMessage("识别一段话结束。如果是长语音的情况会继续识别下段话。")

    }

    /**
     * 长语音识别结束
     */
    override fun onAsrLongFinish() {
        super.onAsrLongFinish()
        sendStatusMessage("长语音识别结束。")
    }


    /**
     * 使用离线命令词时，有该回调说明离线语法资源加载成功
     */
    override fun onOfflineLoaded() {
        sendStatusMessage("【重要】asr.loaded：离线资源加载成功。没有此回调可能离线语法功能不能使用。")
    }

    /**
     * 使用离线命令词时，有该回调说明离线语法资源加载成功
     */
    override fun onOfflineUnLoaded() {
        sendStatusMessage(" 离线资源卸载成功。")
    }

    override fun onAsrExit() {
        super.onAsrExit()
        sendStatusMessage("识别引擎结束并空闲中")
    }

    private fun sendStatusMessage(message: String) {
        sendMessage(message, status)
    }


    private fun sendMessage(message: String, what: Int = IStatus.WHAT_MESSAGE_STATUS, highlight: Boolean = false) {
        var message = message
        if (needTime && what != IStatus.STATUS_FINISHED) {
            message += "  ;time=" + System.currentTimeMillis()
        }
        val msg = Message.obtain()
        msg.what = what
        msg.arg1 = status
        if (highlight) {
            msg.arg2 = 1
        }
        msg.obj = message + "\n"
        handler.sendMessage(msg)
    }
}
