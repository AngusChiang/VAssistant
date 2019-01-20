package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.executorengine.parse.ParseEngine
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
        Vog.d(this, "VoiceAssist ---> $action")
        when (action) {
            Intent.ACTION_ASSIST, Intent.ACTION_VOICE_COMMAND, "android.intent.action.VOICE_ASSIST", "wakeup" -> {
                Vog.d(this, "onCreate ---> ASSIST wakeup")
                MainService.switchRecog()
            }
            SWITCH_VOICE_WAKEUP -> {
                if (AppConfig.voiceWakeup) {
                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP)
                } else {
                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP)
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
                            if (scope != null)//App内 启动
                                que.add(Action(-999,
                                        String.format(ParseEngine.PRE_OPEN, scope.packageName),
                                        Action.SCRIPT_TYPE_LUA))
                        }
                        que.add(node.action)
                        node.action.param = null
                        AppBus.post(que)
                    } else {
                        GlobalApp.toastShort("指令不存在")
                    }
                } catch (e: Exception) {
                }
            }
        }
        finishAndRemoveTask()
    }

    companion object {
        val SWITCH_VOICE_WAKEUP = "switch_voice_wakeup"
        val SET_ASSIST_APP = "set_assist_app"
    }
}