package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.bottomdialog.BottomDialogActivity
import cn.vove7.jarvis.activities.PluginManagerActivity
import cn.vove7.jarvis.view.dialog.AppUpdateDialog.Companion.getBuildAction
import cn.vove7.vtp.log.Vog

/**
 * # UtilEventReceiver
 *
 * @author Administrator
 * 2018/11/24
 */
object UtilEventReceiver : DyBCReceiver() {
    override val intentFilter: IntentFilter = IntentFilter().apply {
        addAction(APP_HAS_UPDATE)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            APP_HAS_UPDATE -> {
                val ver = intent.getStringExtra("version")
                val log = intent.getStringExtra("log")
                BottomDialogActivity.builder(context!!, getBuildAction(ver, log))
            }
        }
    }

    const val APP_HAS_UPDATE = "app_has_update"
}