package cn.vove7.common.appbus

import cn.vove7.common.BuildConfig
import cn.vove7.common.app.GlobalLog
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
    const val EVENT_HIDE_FLOAT = "e_hide_float"
    const val EVENT_PLUGIN_INSTALLED = "e_plugin_installed"
    const val EVENT_PLUGIN_UNINSTALLED = "e_plugin_uninstalled"

    const val ORDER_BEGIN_SCREEN_PICKER = "begin_screen_picker"
    const val ORDER_BEGIN_SCREEN_PICKER_TRANSLATE = "begin_screen_picker_translate"
    const val ORDER_STOP_EXEC = "stop_exec"
    const val ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY = "stop_voice_wp_without_notify"
    const val ORDER_START_VOICE_WAKEUP_WITHOUT_NOTIFY = "start_voice_wp_without_notify"
    const val ORDER_STOP_RECOG = "stop_recog"
    const val ORDER_CANCEL_RECOG = "cancel_recog"
    const val ORDER_START_RECOG = "start_recog"
    const val ORDER_START_RECOG_SILENT = "start_recog_silent" //静默开启
//    const val ORDER_STOP_DEBUG = "stop_debug"

    fun post(data: Any) {
        if (BuildConfig.DEBUG) {//打印函数栈
            val st = Thread.currentThread().stackTrace[3]
            val m = "(${st.fileName}:${st.lineNumber}) ${st.methodName}"

            Vog.d("post ---> $data on $m")
        }
        EventBus.getDefault().post(data)
    }

    private val threadList = mutableListOf<Thread>()
    private fun removeByName(name: String) {
        try {
            threadList.removeAll {
                it.name == name
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
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
        ACTION_START_RECOG,
        ACTION_START_WAKEUP,
        ACTION_START_WAKEUP_WITHOUT_SWITCH,//不打开设置开关 @see[AppConfig.voiceWakeup]
        ACTION_STOP_WAKEUP,
        ACTION_STOP_WAKEUP_WITHOUT_SWITCH,
        ACTION_STOP_RECOG,
        ACTION_RELOAD_SYN_CONF,
        ACTION_CANCEL_RECOG,
        ACTION_START_WAKEUP_TIMER,
        ACTION_STOP_WAKEUP_TIMER
    }
}

