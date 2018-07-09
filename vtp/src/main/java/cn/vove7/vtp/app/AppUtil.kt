package cn.vove7.vtp.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 *
 *
 * Created by Vove on 2018/6/14
 */
object AppUtil {

    /**
     * 跳转App详情页
     */
    fun showPackageDetail(context: Context, packageName: String) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun getAppInfo(context: Context, name: String, pkg: String): AppInfo? {
        val man = context.packageManager
        val list = man.getInstalledApplications(0)
        for (app in list) {
            val appName = app.loadLabel(man).toString()
            if (name == appName || pkg == app.packageName) {
                return AppInfo(
                        name = appName,
                        packageName = app.packageName,
                        icon = app.loadIcon(man)
                )
            }
        }
        return null
    }

    /**
     * 获取所有已安装
     */
    fun getAllInstallApp(context: Context): List<AppInfo> {
        val man = context.packageManager
        val list = man.getInstalledApplications(0)
        val appList = mutableListOf<AppInfo>()
        for (app in list) {
            val name = app.loadLabel(man).toString()
            appList.add(AppInfo(
                    name = name,
                    packageName = app.packageName,
                    icon = app.loadIcon(man)
            ))
        }
        return appList
    }
}