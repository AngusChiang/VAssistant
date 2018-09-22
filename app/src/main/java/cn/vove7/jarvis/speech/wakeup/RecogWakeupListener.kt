package cn.vove7.jarvis.speech.wakeup

import android.os.Handler
import android.os.Message
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.RequestPermission
import cn.vove7.jarvis.R
import cn.vove7.jarvis.speech.recognition.model.IStatus
import cn.vove7.vtp.builder.BundleBuilder
import cn.vove7.vtp.sharedpreference.SpHelper


/**
 * Created by fujiayi on 2017/9/21.
 */

class RecogWakeupListener(private val handler: Handler) : SimpleWakeupListener(), IStatus {

    override fun onStart() {
        super.onStart()
        GlobalApp.toastShort("语音唤醒开启")
    }

    override fun onSuccess(word: String?, result: WakeUpResult) {
        super.onSuccess(word, result)
        val m = Message()
        m.what = IStatus.CODE_WAKEUP_SUCCESS
        m.data = BundleBuilder().put("data", word ?: "").build()
        handler.sendMessage(m)
    }

    override fun onError(errorCode: Int, errorMessage: String, result: WakeUpResult) {
        super.onError(errorCode, errorMessage, result)
        if (errorCode == 3)
            AppBus.post(RequestPermission("麦克风权限"))
        else
            GlobalApp.toastShort("语音唤醒错误")
        SpHelper(GlobalApp.APP).set(R.string.key_open_voice_wakeup, false)
    }

    override fun onStop() {
        super.onStop()
        GlobalApp.toastShort("语音唤醒已关闭")
    }
}
