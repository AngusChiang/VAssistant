package cn.vove7.jarvis.droidplugin

import android.content.Context

/**
 * # PluginManager
 * 插件管理
 * @author Administrator
 * 2018/11/18
 */
interface PluginManager {

    fun installPlugin(path: String): Boolean
    fun uninstallPlugin(pluginInfo: VPluginInfo): Boolean

    fun launchPluginMainActivity(context: Context, pluginInfo: VPluginInfo): Boolean
    fun startPluginService(context: Context, pluginInfo: VPluginInfo): Boolean
    fun stopPluginService(context: Context, pluginInfo: VPluginInfo): Boolean
    fun launchWithApp(context: Context) {
        installList(true).forEach {
            if (it.launchWithApp) {
                startPluginService(context, it)
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