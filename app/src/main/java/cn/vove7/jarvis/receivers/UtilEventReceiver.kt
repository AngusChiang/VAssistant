package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.bottomdialog.BottomDialogActivity
import cn.vove7.bottomdialog.builder.oneButton
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP
import cn.vove7.common.appbus.AppBus.ACTION_STOP_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.view.dialog.AppUpdateDialog
import cn.vove7.jarvis.view.dialog.contentbuilder.WrappedTextContentBuilder
import org.greenrobot.eventbus.Subscribe

/**
 * # UtilEventReceiver
 *
 * @author Administrator
 * 2018/11/24
 */
object UtilEventReceiver : DyBCReceiver() {

    override fun onStart() {
        AppBus.reg(this)
    }

    override val intentFilter: IntentFilter
        get() = IntentFilter().apply {
            addAction(APP_HAS_UPDATE)
            addAction(INST_DATA_SYNC_FINISH)
            addAction(ACTION_START_WAKEUP)
            addAction(ACTION_STOP_WAKEUP)
            addAction(ACTION_STOP_DEBUG_SERVER)
        }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return

        when (action) {
            APP_HAS_UPDATE -> {
                val ver = intent.getStringExtra("version")
                val log = intent.getStringExtra("log")
                BottomDialogActivity.builder(GlobalApp.APP, AppUpdateDialog.getBuildAction(ver, log))
            }
            INST_DATA_SYNC_FINISH -> {
                val content = intent.getCharSequenceExtra("content")
                BottomDialogActivity.builder(GlobalApp.APP) {
                    awesomeHeader("指令数据更新日志")
                    content(WrappedTextContentBuilder(content))
                    oneButton("确定")
                }
            }
            else -> {
                //广播转EventBus
                AppBus.post(action)
            }
        }
    }

    @Subscribe
    fun onBusEvent(event: String) {
        when (event) {
            AppBus.EVENT_LOGOUT, AppBus.EVENT_FORCE_OFFLINE -> {
                AppLogic.onLogout()
                if (AppConfig.speechEngineType == 1) {
                    AppConfig.speechEngineType = 0
                    MainService.loadSpeechService(0)
                }
            }
            AppBus.ACTION_RELOAD_HOME_SYSTEM -> {
                MainService.loadHomeSystem()
            }
        }
    }

    const val APP_HAS_UPDATE = "app_has_update"
    const val INST_DATA_SYNC_FINISH = "inst_data_sync_finish"
}