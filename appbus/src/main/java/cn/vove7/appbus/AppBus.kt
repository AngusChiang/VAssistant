package cn.vove7.appbus

import org.greenrobot.eventbus.EventBus
import java.io.Serializable

object AppBus {
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
    fun postSpeechRecoAction(action: Int) {
        EventBus.getDefault().post(SpeechRecoAction(action))
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

interface BaseAction {
    companion object {
        const val ACTION_START = 1
        const val ACTION_STOP = 2
        const val ACTION_CANCEL = 3
    }
}

/**
 * 语音识别控制消息
 */
data class SpeechRecoAction(val action: Int) : BaseAction {

    override fun toString(): String {
        return "SpeechRecoAction(action=$action)"
    }
}

/**
 * 语音识别数据
 */
data class VoiceData(val what: Int = 0, val tempResult: String? = null, val volumePercent: Int = 0)
    : Serializable

/**
 * 语音合成数据
 */
class SpeechSynData {
    var status: Int = SYN_STATUS_START
    var errMsg: String? = null


    constructor(msg: String?) {
        this.status = SYN_STATUS_ERROR
        this.errMsg = msg
    }

    constructor(status: Int) {
        this.status = status
    }

    companion object {
        const val SYN_STATUS_PREPARE = 0
        const val SYN_STATUS_START = 1
        const val SYN_STATUS_PROCESS = 2
        const val SYN_STATUS_FINISH = 3
        const val SYN_STATUS_ERROR = -1
    }
}

data class SpeechSynAction(
        val action: Int, val text: String? = null
) : Serializable, BaseAction {
    companion object {
        const val ACTION_PAUSE = 100
        const val ACTION_RESUME = 101
    }
}


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


