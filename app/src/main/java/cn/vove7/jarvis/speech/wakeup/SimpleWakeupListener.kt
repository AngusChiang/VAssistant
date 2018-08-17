package cn.vove7.jarvis.speech.wakeup

import cn.vove7.vtp.log.Vog


/**
 * Created by fujiayi on 2017/6/21.
 */

open class SimpleWakeupListener : IWakeupListener {

    override fun onSuccess(word: String?, result: WakeUpResult) {

        Vog.i(this, "唤醒成功，唤醒词：$word")
    }

    override fun onStop() {
        Vog.i(this, "唤醒词识别结束：")
    }

    override fun onError(errorCode: Int, errorMessage: String, result: WakeUpResult) {
        Vog.i(this, "唤醒错误：" + errorCode + ";错误消息：" + errorMessage + "; 原始返回" + result.origalJson)
    }

    override fun onASrAudio(data: ByteArray?, offset: Int, length: Int) {
        Vog.e(this, "audio data： ${data?.size}")
    }

}
