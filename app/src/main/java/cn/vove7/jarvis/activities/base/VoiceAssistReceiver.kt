package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.executorengine.parse.OpenAppAction
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread

/**
 * # VoiceAssistReceiver
 *
 * 助手唤醒 与 桌面Shortcut快捷执行
 * @author 17719247306
 * 2018/9/10
 */
class VoiceAssistActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.action
        Vog.d("VoiceAssist ---> $action")
        when (action) {
            Intent.ACTION_ASSIST, Intent.ACTION_VOICE_COMMAND,
            RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE,
            RecognizerIntent.ACTION_WEB_SEARCH, WAKE_UP -> {
                Vog.d("onCreate ---> ASSIST wakeup")
                MainService.switchRecog()
            }
            "android.intent.action.VOICE_ASSIST" -> {//一加长按HOME键
                val arr = resources.getStringArray(R.array.list_home_funs)
                when (arr.indexOf(AppConfig.homeFun)) {
                    0 -> startActivity(ScreenAssistActivity.createIntent())
                    1 -> MainService.switchRecog()
                    else -> startActivity(ScreenAssistActivity.createIntent())
                }
            }
            SWITCH_VOICE_WAKEUP -> {
                if (AppConfig.voiceWakeup) {
                    AppBus.post(AppBus.ACTION_STOP_WAKEUP)
                } else {
                    AppBus.post(AppBus.ACTION_START_WAKEUP)
                }
            }
            SET_ASSIST_APP ->
                thread(isDaemon = true) {
                    MainService.parseCommand("设为默认助手", false)
                    sleep(5000)
                }
            else -> {
                try {
                    val id = action!!.toLong()
                    val node = DAO.daoSession.actionNodeDao.load(id)
                    if (node != null) {
                        val que = PriorityQueue<Action>()
                        if (node.belongInApp()) {
                            val scope = node.actionScope
                            if (scope != null) {//App内 启动
                                val openAction by OpenAppAction(scope.packageName)
                                que.add(openAction)
                            }
                        }
                        que.add(node.action)
                        node.action.param = null
                        AppBus.post(que)
                    } else {
                        GlobalApp.toastError("指令不存在")
                    }
                } catch (e: Exception) {
                }
            }
        }
        finishAndRemoveTask()
    }

    companion object {
        const val SWITCH_VOICE_WAKEUP = "switch_voice_wakeup"
        const val SET_ASSIST_APP = "set_assist_app"
        const val WAKE_UP = "wakeup"
    }
}