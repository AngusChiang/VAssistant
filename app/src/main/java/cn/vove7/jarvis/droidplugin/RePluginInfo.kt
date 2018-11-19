package cn.vove7.jarvis.droidplugin

import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.model.PluginInfo

/**
 * # RePluginInfo
 *
 * @author Administrator
 * 2018/11/19
 */
class RePluginInfo(var reInfo: PluginInfo? = null)
    : VPluginInfo() {

    override val isInstalled: Boolean
        get() {
            if (reInfo == null)
                reInfo = RePlugin.getPluginInfo(name)
            return reInfo != null
        }
    override val installApkPath: String?
        get() = reInfo?.apkFile?.absolutePath
}