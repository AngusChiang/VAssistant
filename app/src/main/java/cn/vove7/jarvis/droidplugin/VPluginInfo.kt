package cn.vove7.jarvis.droidplugin

import android.content.pm.PackageManager
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.log.Vog
import net.dongliu.apk.parser.ApkFile

/**
 * # VPluginInfo
 * 插件信息基类
 * 约定：入口Activity：MainActivity
 * 服务：MainService 自启服务 permission="LAUNCH_WITH_APP"
 *
 * @author Administrator
 * 2018/11/18
 */
abstract class VPluginInfo {
    /**
     * 是否安装
     */
    abstract val isInstalled: Boolean

    var name: String? = null

    var packageName: String? = null
    var description: String? = null
    var versionName: String? = null
    var versionCode: Long = 0
    var author: String? = null
    var authorEmail: String? = null

    val mainActivity: String? by lazy { getPluginMainActivity() }
    var mainService: String? = null

    /**
     * 从Apk文件解析内容
     *
     * meta:
     * description:plugin_desc
     * author:author
     * authorEmail:author_email
     */
    fun parseApk() {
        val p = installApkPath ?: return
        val apkFile: ApkFile
        try {
            apkFile = ApkFile(p)
        } catch (e: Exception) {
            GlobalLog.err(e, "vp39")
            return
        }
        val meta = apkFile.apkMeta
        name = meta.label
        packageName = meta.packageName
        versionCode = meta.versionCode
        versionName = meta.versionName

        val pm = GlobalApp.APP.packageManager
        val info = pm.getPackageArchiveInfo(installApkPath,
                PackageManager.GET_META_DATA)
        val metas = info.applicationInfo.metaData ?: return
        description = metas.getString("plugin_desc")
        author = metas.getString("author") ?: ""
        authorEmail = metas.getString("author_email")
        mainService = getPluginMainService()
//        description=meta
    }

    /**
     * 是否跟随App自启
     */
    var launchWithApp: Boolean = false

    abstract val installApkPath: String?

    private fun getPluginMainActivity(): String? {
        if (installApkPath == null) return null
        val pm = GlobalApp.APP.packageManager
        val info = pm.getPackageArchiveInfo(installApkPath,
                PackageManager.GET_ACTIVITIES)
        info.activities.filter {
            it != null
        }.forEach {
            if (it.name.endsWith(".MainActivity")) {
                Vog.d(this, "getPluginMainActivity ---> ${it.name}")
                return it.name
            }
        }
        return null
    }

    private fun getPluginMainService(): String? {
        if (installApkPath == null) return null

        val pm = GlobalApp.APP.packageManager
        val info = pm.getPackageArchiveInfo(installApkPath,
                PackageManager.GET_SERVICES)
        info.services.filter {
            it != null
        }.forEach {
            if (it.name.endsWith(".MainService")) {
                if (it.permission == "LAUNCH_WITH_APP") {
                    Vog.d(this, "getPluginMainService ---> ${it.name} 服务自启")
                    launchWithApp = true
                }
                Vog.d(this, "getPluginMainService ---> ${it.name}")
                return it.name
            }
        }
        return null
    }
}