package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.bottomdialog.BottomDialogActivity
import cn.vove7.bottomdialog.builder.oneButton
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP
import cn.vove7.common.appbus.AppBus.ACTION_STOP_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.ACTION_STOP_EXEC
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP
import cn.vove7.common.utils.contains
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.shs.RokidHomeSystem
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.tools.timedtask.TimedTaskManager
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

    override val receiverType: Int
        get() = TYPE_GLOBAL

    override fun onStart() {
        AppBus.reg(this)
    }

    override val intentFilter: IntentFilter
        get() = IntentFilter().apply {
            addAction(APP_HAS_UPDATE)
            addAction(INST_DATA_SYNC_FINISH)
            addAction(ROKID_SEND_LOC)
            addAction(TIMED_TASK)
            addAction(ACTION_START_WAKEUP)
            addAction(ACTION_STOP_WAKEUP)
            addAction(ACTION_STOP_DEBUG_SERVER)
            addAction(ACTION_STOP_EXEC)
        }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return

        if (intent.contains("from")) {
            if (intent.getStringExtra("from") != GlobalApp.APP.packageName) {
                return
            }
        }

        when (action) {
            APP_HAS_UPDATE -> {
                val ver = intent.getStringExtra("version") ?: return
                val log = intent.getStringExtra("log") ?: return

                BottomDialogActivity.builder(GlobalApp.APP,
                        AppUpdateDialog.getBuildAction(ver, log))
            }
            INST_DATA_SYNC_FINISH -> {
                val content = intent.getCharSequenceExtra("content")
                BottomDialogActivity.builder(GlobalApp.APP) {
                    awesomeHeader("指令数据更新日志")
                    content(WrappedTextContentBuilder(content))
                    oneButton("确定")
                }
            }
            ROKID_SEND_LOC -> {
                val hc = MainService.homeControlSystem
                if (hc is RokidHomeSystem) {
                    hc.callSendLocTask()
                }
            }
            TIMED_TASK -> {
                val taskId = intent.getStringExtra("task_id")
                if (taskId != null) {
                    TimedTaskManager.runTask(taskId)
                } else {
                    GlobalLog.err("获取任务失败")
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
            }
            AppBus.ACTION_RELOAD_HOME_SYSTEM -> {
                MainService.loadHomeSystem()
            }
        }
    }

    fun getIntent(action: String): Intent {
        val intent = Intent(action)
        intent.putExtra("from", GlobalApp.APP.packageName)
        return intent
    }

    const val APP_HAS_UPDATE = "app_has_update"
    const val INST_DATA_SYNC_FINISH = "inst_data_sync_finish"
    const val ROKID_SEND_LOC = "ROKID_SEND_LOC"
    const val TIMED_TASK = "TIMED_TASK"
}