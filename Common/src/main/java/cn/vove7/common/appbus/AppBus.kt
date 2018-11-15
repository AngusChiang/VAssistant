package cn.vove7.common.appbus

import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.EventBus
import java.io.Serializable
import java.lang.Thread.sleep
import kotlin.concurrent.thread

object AppBus {
    const val EVENT_LOGOUT = "e_logout"
    const val EVENT_FORCE_OFFLINE = "e_force_offline"
    const val EVENT_START_DEBUG_SERVER = "start_debug_server"
    const val EVENT_STOP_DEBUG_SERVER = "stop_debug_server"
    const val EVENT_INST_SAVE_COMPLETE = "inst_settings_save_complete"
    const val EVENT_BEGIN_RECO = "e_begin_reco"
    const val EVENT_FINISH_RECO = "e_finish_reco"
    const val EVENT_ERROR_RECO = "e_error_reco"
    const val EVENT_HIDE_FLOAT = "e_hide_float"

    const val ORDER_BEGIN_SCREEN_PICKER = "begin_screen_picker"
    const val ORDER_BEGIN_SCREEN_PICKER_TRANSLATE = "begin_screen_picker_translate"
    const val ORDER_STOP_EXEC = "stop_exec"
    const val ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY = "stop_voice_wp_without_notify"
    const val ORDER_START_VOICE_WAKEUP_WITHOUT_NOTIFY = "start_voice_wp_without_notify"
    const val ORDER_STOP_RECO = "stop_reco"
    const val ORDER_CANCEL_RECO = "cancel_reco"
    const val ORDER_START_RECO = "start_reco"
//    const val ORDER_STOP_DEBUG = "stop_debug"

    fun post(data: Any) {
        Vog.d(this, "post ---> $data")
        EventBus.getDefault().post(data)
    }

    private val threadList = mutableListOf<Thread>()
    private fun removeByName(name: String) {
        threadList.removeAll {
            it.name == name
        }
    }

    fun postDelay(tag: String, data: Any, delay: Long) {
        synchronized(threadList) {
            threadList.add(thread(name = tag) {
                try {
                    sleep(delay)
                } catch (e: InterruptedException) {
                    return@thread
                }
                post(data)
                removeByName(tag)
            })
        }
    }

    fun remove(tag: String) {
        synchronized(threadList) {
            threadList.filter { it.name == tag }.forEach {
                it.interrupt()
            }
        }
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
        ACTION_START_WAKEUP_WITHOUT_SWITCH,//不打开设置开关 @see[AppConfig.voiceWakeup]
        ACTION_STOP_WAKEUP,
        ACTION_STOP_WAKEUP_WITHOUT_SWITCH,
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


