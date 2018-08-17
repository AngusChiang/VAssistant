package cn.vove7.jarvis.speech.wakeup

import android.os.Handler

import cn.vove7.jarvis.speech.recognition.model.IStatus
import cn.vove7.vtp.builder.BundleBuilder


/**
 * Created by fujiayi on 2017/9/21.
 */

class RecogWakeupListener(private val handler: Handler) : SimpleWakeupListener(), IStatus {

    override fun onSuccess(word: String?, result: WakeUpResult) {
        super.onSuccess(word, result)

        handler.sendEmptyMessage(IStatus.STATUS_WAKEUP_SUCCESS)
    }
}
