package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.service.quicksettings.TileService
import android.speech.RecognizerIntent
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.AppPermission
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.RootHelper
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.startActivity
import cn.vove7.executorengine.parse.OpenAppAction
import cn.vove7.jarvis.activities.screenassistant.*
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.TileLongClickable
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.tools.setAssistantApp
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

        val uri = intent.data
        if (uri?.scheme == "vassistant") {
            if (parseUri(uri)) {
                finishAndRemoveTask()
                return
            }
        }
        when (action) {
            Intent.ACTION_VOICE_COMMAND,
            RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE,
            RecognizerIntent.ACTION_WEB_SEARCH, WAKE_UP -> {
                Vog.d("onCreate ---> ASSIST wakeup")
                MainService.switchRecog()
            }
            Intent.ACTION_ASSIST, "android.intent.action.VOICE_ASSIST" -> {//一加长按HOME键
                when (AppConfig.homeFun) {
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
                    if (AppPermission.canWriteSecureSettings || RootHelper.hasRoot(100)) {
                        try {
                            setAssistantApp()
                            GlobalApp.toastSuccess("设置完成")
                        } catch (e: Throwable) {
                            e.log()
                            GlobalApp.toastSuccess("设置失败\n${e.message}")
                        }
                    } else {
                        MainService.parseCommand("设为默认助手", false)
                    }
                    sleep(5000)
                }
            WAKEUP_SCREEN_ASSIST -> {
                startActivity(ScreenAssistActivity.createIntent())
            }
            SCREEN_ASSIST_TEXT_PICKER -> {
                AppBus.post(AppBus.ACTION_BEGIN_SCREEN_PICKER)
            }
            SCREEN_ASSIST_QR -> {
                startActivity<QrCodeActivity>()
            }
            SCREEN_ASSIST_SPOT_SCREEN -> {
                startActivity<SpotScreenActivity>()
            }
            SCREEN_ASSIST_SCREEN_SHARE -> {
                startActivity<ScreenShareActivity>()
            }
            SCREEN_ASSIST_SCREEN_OCR -> {
                startActivity<ScreenOcrActivity>()
            }
            SWITCH_DEBUG_MODE -> {
                if (RemoteDebugServer.stopped) {
                    RemoteDebugServer.start()
                } else {
                    RemoteDebugServer.stop()
                }
            }
            TileService.ACTION_QS_TILE_PREFERENCES -> {
                val cn = (intent.extras?.get(Intent.EXTRA_COMPONENT_NAME) as ComponentName?)
                    ?: return
                runInCatch {
                    val tileService = Class.forName(cn.className)
                    val ins = tileService.newInstance()
                    if (ins is TileLongClickable) {
                        ins.onLongClick()
                    }
                }
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

    private fun parseUri(uri: Uri): Boolean {
        when (uri.host) {
            "run" -> {
                val cmd = uri.getQueryParameter("cmd") ?: return false
                MainService.parseCommand(cmd)
                return true
            }
        }
        return false
    }

    companion object {
        const val SWITCH_VOICE_WAKEUP = "switch_voice_wakeup"
        const val SWITCH_DEBUG_MODE = "switch_debug_mode"
        const val SET_ASSIST_APP = "set_assist_app"
        const val WAKEUP_SCREEN_ASSIST = "wakeup_screen_assist"
        const val SCREEN_ASSIST_TEXT_PICKER = "screen_assist_text_picker"
        const val SCREEN_ASSIST_QR = "screen_assist_qr"
        const val SCREEN_ASSIST_SPOT_SCREEN = "screen_assist_spot_screen"
        const val SCREEN_ASSIST_SCREEN_OCR = "screen_assist_screen_ocr"

        const val SCREEN_ASSIST_SCREEN_SHARE = "screen_assist_screen_share"

        const val WAKE_UP = "wakeup"
    }
}