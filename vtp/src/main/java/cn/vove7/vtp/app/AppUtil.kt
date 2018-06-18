package cn.vove7.vtp.app

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
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

    fun getAppInfo(pkg: String): ApplicationInfo? {

        return null
    }

    fun getAllInstallApp(context: Context): List<ApplicationInfo> {
        val man = context.packageManager
        return man.getInstalledApplications(0)
    }
}