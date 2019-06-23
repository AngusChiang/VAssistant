package cn.vove7.jarvis.speech.baiduspeech.wakeup

import android.os.Handler
import android.os.Message
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R
import cn.vove7.jarvis.speech.SpeechConst
import cn.vove7.common.app.AppConfig
import cn.vove7.vtp.builder.BundleBuilder
import cn.vove7.vtp.sharedpreference.SpHelper


/**
 * Created by fujiayi on 2017/9/21.
 */

class RecogWakeupListener(private val handler: Handler) : SimpleWakeupListener(), SpeechConst {

    override fun onStart() {
        super.onStart()
    }

    override fun onSuccess(word: String?, result: WakeUpResult) {
        super.onSuccess(word, result)
        val m = Message()
        m.what = SpeechConst.CODE_WAKEUP_SUCCESS
        m.data = BundleBuilder().put("data", word ?: "").build()
        handler.sendMessage(m)
    }

    override fun onError(errorCode: Int, errorMessage: String, result: WakeUpResult) {
        super.onError(errorCode, errorMessage, result)
        if (errorCode == 3) {
            GlobalApp.toastError("麦克风打开失败")
//            AppBus.post(RequestPermission("麦克风权限"))
        }
        else
            GlobalApp.toastError("语音唤醒错误")
        SpHelper(GlobalApp.APP).set(R.string.key_open_voice_wakeup, false)
        AppConfig.reload()
    }

    override fun onStop() {
        super.onStop()
    }
}
