package cn.vove7.jarvis.speech.baiduspeech.wakeup

import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.log.Vog


/**
 * Created by fujiayi on 2017/6/21.
 */
open class SimpleWakeupListener : IWakeupListener {
    override fun onStart() {
        Vog.i("唤醒 onStart")
    }

    override fun onSuccess(word: String?, result: WakeUpResult) {
        Vog.i("唤醒成功，唤醒词：$word")
    }

    override fun onStop() {
        Vog.i("唤醒词识别结束：")
    }

    override fun onError(errorCode: Int, errorMessage: String, result: WakeUpResult) {
        val e = "唤醒错误：" + errorCode + ";错误消息：" + errorMessage + "; 原始返回" + result.origalJson
        GlobalLog.err(e)
    }

    override fun onASrAudio(data: ByteArray?, offset: Int, length: Int) {
        Vog.e("audio data： ${data?.size}")
    }

}
