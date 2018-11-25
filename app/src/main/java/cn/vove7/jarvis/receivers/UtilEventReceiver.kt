package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.jarvis.activities.PluginManagerActivity

import cn.vove7.vtp.log.Vog

/**
 * # UtilEventReceiver
 *
 * @author Administrator
 * 2018/11/24
 */
object UtilEventReceiver : DyBCReceiver() {
    override val intentFilter: IntentFilter = IntentFilter().apply {
        addAction(PLUGIN_DL_DONE)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PLUGIN_DL_DONE -> {
                Vog.d(this, "onReceive ---> PLUGIN_DL_DONE")
                intent.getStringExtra("path")?.apply {
                    PluginManagerActivity.installPlugin(this)
                }
            }
        }
    }

    /**
     * 插件下载完成，安装
     */
    const val PLUGIN_DL_DONE = "plugin_dl_done"

}