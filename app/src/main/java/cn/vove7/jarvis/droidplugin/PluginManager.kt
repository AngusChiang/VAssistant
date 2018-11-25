package cn.vove7.jarvis.droidplugin

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.vtp.log.Vog

/**
 * # PluginManager
 * 插件管理
 * @author Administrator
 * 2018/11/18
 */
interface PluginManager {

    val context: Context get() = GlobalApp.APP

    fun installPlugin(path: String): VPluginInfo?
    fun uninstallPlugin(pluginInfo: VPluginInfo): Boolean

    fun launchPluginMainActivity(pluginInfo: VPluginInfo): Boolean
    fun startPluginService(pluginInfo: VPluginInfo): Boolean
    fun stopPluginService(pluginInfo: VPluginInfo): Boolean

    fun getInfo(pkg: String?): VPluginInfo?
//    fun killAll()
//    fun kill(pluginInfo: VPluginInfo)

    /**
     * 自启
     * @param context Context
     */
    fun launchWithApp() {
        runOnNewHandlerThread(delay = 5000) {
            Vog.d(this, "launchWithApp ---> 自启插件服务")
            installList(true).forEach {
                if (it.launchWithApp && it.enabled) {
                    startPluginService(it)
                }
            }
        }
    }

    var cacheList: MutableList<VPluginInfo>
    /**
     * 安装列表
     * @param refresh Boolean
     * @return List<VPluginInfo>
     */
    fun installList(refresh: Boolean = false): List<VPluginInfo>

}