package cn.vove7.common.appbus

import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.EventBus
import java.io.Serializable

object AppBus {
    const val EVENT_LOGOUT = "e_logout"
    const val EVENT_FORCE_OFFLINE = "e_force_offline"
    const val EVENT_START_DEBUG_SERVER = "start_debug_server"
    const val EVENT_STOP_DEBUG_SERVER = "stop_debug_server"
    const val EVENT_INST_SAVE_COMPLETE = "inst_settings_save_complete"
    const val EVENT_BEGIN_SCREEN_PICKER = "begin_screen_picker"

    const val ORDER_STOP_EXEC = "stop_exec"
    const val ORDER_STOP_RECO = "stop_reco"
    const val ORDER_CANCEL_RECO = "cancel_reco"
    const val ORDER_START_RECO = "start_reco"
//    const val ORDER_STOP_DEBUG = "stop_debug"

    fun post(data: Any) {
        Vog.d(this,"post ---> $data")
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

    enum class ActionCode {
        ACTION_START_RECO,
        ACTION_START_WAKEUP,
        ACTION_STOP_WAKEUP,
        ACTION_STOP_RECO,
        ACTION_RELOAD_SYN_CONF,
        ACTION_CANCEL_RECO,
        ACTION_START_WAKEUP_TIMER,
        ACTION_STOP_WAKEUP_TIMER
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


