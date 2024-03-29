package cn.vove7.jarvis.speech.baiduspeech.wakeup

/**
 * Created by fujiayi on 2017/6/21.
 */

interface IWakeupListener {
    fun onStart()
    fun onSuccess(word: String?, result: WakeUpResult)
    fun onStop()
    fun onError(errorCode: Int, errorMessage: String, result: WakeUpResult)
    fun onASrAudio(data: ByteArray?, offset: Int, length: Int)
}
