package cn.vove7.jarvis.speech.baiduspeech.recognition.listener

/**
 * Created by fujiayi on 2017/6/14.
 */

import cn.vove7.jarvis.speech.SpeechConst
import cn.vove7.jarvis.speech.baiduspeech.recognition.model.RecogResult
import cn.vove7.vtp.log.Vog

open class StatusRecogListener : IRecogListener, SpeechConst {

    /**
     * 识别的引擎当前的状态
     */
    var status = SpeechConst.STATUS_NONE

    override fun onAsrReady() {
        status = SpeechConst.STATUS_READY
    }

    override fun onAsrBegin() {
        status = SpeechConst.STATUS_SPEAKING
    }

    override fun onAsrEnd() {
        status = SpeechConst.STATUS_RECOGNITION
        Vog.i("说话结束")
    }

    override fun onAsrPartialResult(results: Array<String>?, recogResult: RecogResult) {
    }

    override fun onAsrFinalResult(results: Array<String>?, recogResult: RecogResult) {
        status = SpeechConst.STATUS_FINISHED
    }

    override fun onAsrFinish(recogResult: RecogResult) {
        status = SpeechConst.STATUS_FINISHED
    }


    override fun onAsrFinishError(errorCode: Int, subErrorCode: Int, errorMessage: String?, descMessage: String?,
                                  recogResult: RecogResult) {
        status = SpeechConst.STATUS_FINISHED
    }

    /**
     * 长语音识别结束
     */
    override fun onAsrLongFinish() {
        status = SpeechConst.STATUS_FINISHED
    }

    override fun onAsrVolume(volumePercent: Int, volume: Int) {
        Vog.i("音量百分比$volumePercent ; 音量$volume")
    }

    override fun onAsrAudio(data: ByteArray?, offset: Int, length: Int) {
        var data = data
        if (offset != 0 || data?.size != length) {
            val actualData = ByteArray(length)
            System.arraycopy(data, 0, actualData, 0, length)
            data = actualData
        }
        Vog.i("音频数据回调, length:" + data.size)
    }

    override fun onAsrExit() {
        status = SpeechConst.STATUS_NONE
    }

    override fun onAsrOnlineNluResult(nluResult: String) {
        status = SpeechConst.STATUS_FINISHED
    }

    override fun onOfflineLoaded() {

    }

    override fun onOfflineUnLoaded() {

    }

}
