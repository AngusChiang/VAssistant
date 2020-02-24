package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.bottomdialog.BottomDialogActivity
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP
import cn.vove7.common.appbus.AppBus.ACTION_STOP_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.view.dialog.AppUpdateDialog.Companion.getBuildAction
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
                BottomDialogActivity.builder(GlobalApp.APP, getBuildAction(ver, log))
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
}