package cn.vove7.jarvis.speech.recognition.listener

import cn.vove7.jarvis.speech.recognition.model.RecogResult

/**
 * Created by fujiayi on 2017/6/14.
 */

interface IRecogListener {

    /**
     * ASR_START 输入事件调用后，引擎准备完毕
     */
    fun onAsrReady()

    /**
     * onAsrReady后检查到用户开始说话
     */
    fun onAsrBegin()

    /**
     * 检查到用户开始说话停止，或者ASR_STOP 输入事件调用后，
     */
    fun onAsrEnd()

    /**
     * onAsrBegin 后 随着用户的说话，返回的临时结果
     *
     * @param results     可能返回多个结果，请取第一个结果
     * @param recogResult 完整的结果
     */
    fun onAsrPartialResult(results: Array<String>?, recogResult: RecogResult)

    /**
     * 最终的识别结果
     *
     * @param results     可能返回多个结果，请取第一个结果
     * @param recogResult 完整的结果
     */
    fun onAsrFinalResult(results: Array<String>?, recogResult: RecogResult)

    fun onAsrFinish(recogResult: RecogResult)

    fun onAsrFinishError(errorCode: Int, subErrorCode: Int, errorMessage: String?, descMessage: String?,
                         recogResult: RecogResult)

    /**
     * 长语音识别结束
     */
    fun onAsrLongFinish()

    fun onAsrVolume(volumePercent: Int, volume: Int)

    fun onAsrAudio(data: ByteArray?, offset: Int, length: Int)

    fun onAsrExit()

    fun onAsrOnlineNluResult(nluResult: String)

    fun onOfflineLoaded()

    fun onOfflineUnLoaded()
}
