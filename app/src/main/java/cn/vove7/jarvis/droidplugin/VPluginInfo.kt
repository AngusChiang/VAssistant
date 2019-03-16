package cn.vove7.jarvis.droidplugin

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.fragments.AwesomeItem
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
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
abstract class VPluginInfo : AwesomeItem {
    /**
     * 是否安装
     */
    abstract val isInstalled: Boolean

    abstract val pluginManager: PluginManager

    var name: String? = null
    val sp get() = SpHelper(GlobalApp.APP, "disable_plugins")

    var packageName: String? = null
    var description: String? = null
    var updateLog: String? = null
    var versionName: String? = null
    var versionCode: Long = 0
    var author: String? = null
    var authorEmail: String? = null
    var fileName: String? = null

    var mainActivity: String? = null
    var mainService: String? = null

    var icon: Drawable? = null
    /**
     * 是否启用
     */
    var enabled: Boolean
        get() {
            return packageName?.let {
                sp.getBoolean(it, false)
            } ?: false
        }
        set(value) {
            packageName?.let {
                if (value) sp.set(it, true)
                else sp.removeKey(it)
            }
        }

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
            GlobalLog.err(e)
            return
        }
        val meta = apkFile.apkMeta
        name = meta.label
        packageName = meta.packageName
        versionCode = meta.versionCode
        versionName = meta.versionName

        mainActivity = meta.mainActivity?.name
        Vog.d("$name mainActivity $mainActivity")

//        val metas = info.applicationInfo.metaData ?: return
        description = meta.metaDatas["plugin_desc"] ?: ""
        author = meta.metaDatas["author"] ?: ""
        authorEmail = meta.metaDatas["author_email"] ?: ""
        updateLog = meta.metaDatas["update_log"] ?: ""
        mainService = getPluginMainService()

        val pm = GlobalApp.APP.packageManager
        val info = pm.getPackageArchiveInfo(installApkPath,
                PackageManager.GET_META_DATA)

        icon = info?.applicationInfo?.loadIcon(pm)
    }

    fun uninstall(): Boolean {
        enabled = false
        return doUninstall()
    }

    protected abstract fun doUninstall(): Boolean

    /**
     * 是否跟随App自启
     */
    var launchWithApp: Boolean = false

    abstract val installApkPath: String?


    /**
     * 启动Activity 若有
     */
    abstract fun launch(): Boolean

    abstract fun startService(): Boolean
    abstract fun stopService(): Boolean

    /**
     * 约定 主服务名：PluginMainService
     * @return String?
     */
    private fun getPluginMainService(): String? {
        if (installApkPath == null) return null

        val pm = GlobalApp.APP.packageManager
        val info = pm.getPackageArchiveInfo(installApkPath,
                PackageManager.GET_SERVICES)
        info.services?.filter {
            it != null
        }?.forEach {
            if (it.name.endsWith(".PluginMainService")) {
                if (it.permission == "LAUNCH_WITH_APP") {
                    Vog.d("${it.name} 服务自启")
                    launchWithApp = true
                }
                Vog.d(it.name)
                return it.name
            }
        }
        return null
    }

    override val viewIcon: Drawable? get() = icon
    override val title: String? get() = name
    override val subTitle: String?
        get() = "$description\n作者：$author\n版本：$versionName"
    override val isChecked: Boolean? get() = isInstalled && enabled

    /**
     * 是否有更新
     */
    fun hasUpdate(): Boolean {
        pluginManager.getInfo(packageName)?.let { localVersion ->
            return localVersion.versionCode < versionCode//可更新
        }
//        GlobalLog.err("未安装：$name")
        return false
    }

    override fun isShow(code: Int?): Boolean {
        return when {
            code == null -> true
            isInstalled -> hasUpdate()
            else -> true
        }
    }

}
