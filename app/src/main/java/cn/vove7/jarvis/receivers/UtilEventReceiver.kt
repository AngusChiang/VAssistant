package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.bottomdialog.BottomDialogActivity
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
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
    override fun start() {
        super.start()
        AppBus.reg(this)
    }

    override val intentFilter: IntentFilter = IntentFilter().apply {
        addAction(APP_HAS_UPDATE)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            APP_HAS_UPDATE -> {
                val ver = intent.getStringExtra("version")
                val log = intent.getStringExtra("log")
                BottomDialogActivity.builder(GlobalApp.APP, getBuildAction(ver, log))
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
                    MainService.instance?.loadSpeechService(0)
                }
            }
        }
    }

    const val APP_HAS_UPDATE = "app_has_update"
}