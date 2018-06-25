package cn.vove7.appbus

import org.greenrobot.eventbus.EventBus
import java.io.Serializable

object Bus {
    fun post(data: Any) {
        EventBus.getDefault().post(data)
    }

    /**
     *
     */
    fun postInfo(ev: MessageEvent) {
        EventBus.getDefault().post(ev)
    }

    /**
     * 控制语音
     */
    fun postSpeechAction(ac: SpeechAction) {
        EventBus.getDefault().post(ac)
    }

    /**
     *
     */
    fun postLog(log: LogMessage) {
        EventBus.getDefault().post(log)
    }

    fun postVoiceData(data: VoiceData) {
        EventBus.getDefault().post(data)
    }

    fun reg(a: Any) {
        if (!EventBus.getDefault().isRegistered(a))
            EventBus.getDefault().register(a)
    }

    fun unreg(a: Any) {
        EventBus.getDefault().unregister(a)
    }
}

open class LogMessage(val level: Int, val msg: String) {
    companion object {
        const val LEV_1 = 1
        const val LEV_2 = 2
        const val LEV_3 = 3
    }
}

open class SpeechAction(val action: Int) {
    companion object {
        const val ACTION_START = 1
        const val ACTION_STOP = 2
        const val ACTION_CANCEL = 3
    }

    override fun toString(): String {
        return "SpeechAction(action=$action)"
    }
}

open class VoiceData(val what: Int = 0, val tempResult: String? = null, val volumePercent: Int = 0)
    : Serializable


open class MessageEvent(val msg: String, val what: Int) {
    override fun toString(): String {
        return "MessageEvent(msg='$msg', what=$what)"
    }

    companion object {
        const val WHAT_MSG_INFO = 1
        const val WHAT_MSG_ERR = 2
    }
}


