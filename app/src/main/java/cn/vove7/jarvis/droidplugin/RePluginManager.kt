package cn.vove7.jarvis.droidplugin

import android.content.Context
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vassistant.plugininterface.app.GlobalLog
import cn.vove7.vtp.log.Vog
import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.component.service.PluginServiceClient

/**
 * # RePluginManager
 *
 * @author Administrator
 * 2018/11/19
 */
class RePluginManager : PluginManager {

    override fun installPlugin(path: String): Boolean {
        return try {
            RePlugin.install(path).let {
                if (it == null) {
                    GlobalApp.toastShort("插件安装失败，详情见日志")
                    GlobalLog.err("插件安装失败 $path")
                    false
                } else {
                    Vog.d(this, "installPlugin ---> 安装成功 $path")
                    true
                }
            }
        } catch (e: Throwable) {
            GlobalApp.toastShort("插件安装失败，详情见日志")
            GlobalLog.err(e, "ip54")
            false
        }
    }

    override fun uninstallPlugin(pluginInfo: VPluginInfo): Boolean {
        Vog.d(this, "uninstallPlugin ---> 卸载${pluginInfo.name}")
        return try {
            RePlugin.uninstall(pluginInfo.packageName)
        } catch (e: Exception) {
            GlobalLog.err(e, "rup41")
            false
        }
    }

    override fun launchPluginMainActivity(context: Context, pluginInfo: VPluginInfo): Boolean {
        return try {
            RePlugin.startActivity(context, RePlugin
                    .createIntent(pluginInfo.packageName, pluginInfo.mainActivity))
        } catch (e: Exception) {
            GlobalLog.err(e, "lp41")
            false
        }
    }

    override fun stopPluginService(context: Context, pluginInfo: VPluginInfo): Boolean {
        if (pluginInfo.mainService == null) {
            Vog.d(this, "startPluginService ---> ${pluginInfo.name} 无主服务")
            return false
        }
        return try {
            PluginServiceClient.stopService(context, RePlugin
                    .createIntent(pluginInfo.packageName, pluginInfo.mainService))
            true
        } catch (e: Exception) {
            GlobalLog.err(e, "lp41")
            false
        }
    }

    override fun startPluginService(context: Context, pluginInfo: VPluginInfo): Boolean {
        if (pluginInfo.mainService == null) {
            Vog.d(this, "startPluginService ---> ${pluginInfo.name} 无主服务")
            return false
        }
        return try {
            PluginServiceClient.startService(context, RePlugin
                    .createIntent(pluginInfo.packageName, pluginInfo.mainService))
            true
        } catch (e: Exception) {
            GlobalLog.err(e, "lp41")
            false
        }
    }

    override var cacheList: MutableList<VPluginInfo> = mutableListOf()

    override fun installList(refresh: Boolean): List<VPluginInfo> {
        if (cacheList.isNotEmpty() && !refresh) {
            return cacheList
        }
        cacheList.clear()
        RePlugin.getPluginInfoList().forEach {
            cacheList.add(RePluginInfo(reInfo = it).also { t -> t.parseApk() })
        }
        return cacheList
    }
}
