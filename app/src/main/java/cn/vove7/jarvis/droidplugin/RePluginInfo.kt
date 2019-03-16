package cn.vove7.jarvis.droidplugin

import cn.vove7.common.utils.ThreadPool
import cn.vove7.vtp.log.Vog
import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.model.PluginInfo

/**
 * # RePluginInfo
 *
 * @author Administrator
 * 2018/11/19
 */
class RePluginInfo(var reInfo: PluginInfo? = null) : VPluginInfo() {

    init {
        parseApk()
    }

    override val pluginManager: PluginManager by lazy { RePluginManager() }
    override val isInstalled: Boolean
        get() {
            if (reInfo == null)
                reInfo = RePlugin.getPluginInfo(packageName)
            return reInfo != null
        }
    override val installApkPath: String?
        get() = reInfo?.apkFile?.absolutePath

    override fun launch(): Boolean {
        return if (mainActivity != null)
            RePluginManager().launchPluginMainActivity(this)
        else {
            Vog.d("launch ---> 无主Activity")
            false
        }
    }

    override fun doUninstall(): Boolean {
        return RePluginManager().uninstallPlugin(this)
    }

    override fun startService(): Boolean {
        return if (mainService != null) {
            ThreadPool.runOnCachePool {
                RePluginManager().startPluginService(this)
            }
            true
        } else {
            Vog.d("launch ---> 无主Service")
            false
        }
    }

    override fun stopService(): Boolean {
        return if (mainService != null)
            RePluginManager().stopPluginService(this)
        else {
            Vog.d("launch ---> 无主Service")
            false
        }
    }

}