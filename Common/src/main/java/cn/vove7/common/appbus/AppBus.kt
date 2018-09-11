package cn.vove7.common.appbus

import org.greenrobot.eventbus.EventBus
import java.io.Serializable

object AppBus {
    fun post(data: Any) {
        EventBus.getDefault().post(data)
    }

    /**
     * 控制语音
     */
    fun postSpeechAction(actionCode: SpeechAction.ActionCode) {
        EventBus.getDefault().post(SpeechAction(actionCode))
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


/**
 * 语音识别控制消息
 */
data class SpeechAction(val action: ActionCode) {

    override fun toString(): String {
        return "SpeechAction(action=$action)"
    }
    enum class ActionCode{
        ACTION_START_RECO,
        ACTION_START_WAKEUP,
        ACTION_STOP_WAKEUP,
        ACTION_STOP_RECO,
        ACTION_RELOAD_SYN_CONF,
        ACTION_CANCEL_RECO
    }
}

/**
 * 语音识别数据
 */
data class VoiceData(val what: Int = 0, val data: String? = null, val volumePercent: Int = 0)
    : Serializable


/**
 * Log消息
 */
open class MessageEvent(val msg: String, val what: Int) {
    override fun toString(): String {
        return "MessageEvent(errMsg='$msg', what=$what)"
    }

    companion object {
        const val WHAT_MSG_INFO = 1
        const val WHAT_MSG_ERR = 2
    }
}


