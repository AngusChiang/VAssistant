package cn.vove7.common.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED
import cn.vove7.common.app.GlobalApp
import java.util.*


/**
 * # ExpandedFuns
 * 扩展函数
 * @author Administrator
 * 2018/10/25
 */
/**
 * 代码块运行于UI线程
 * @param action () -> Unit
 */
fun runOnUi(action: () -> Unit) {
    val mainLoop = Looper.getMainLooper()
    if (mainLoop == Looper.myLooper()) {
        action.invoke()
    } else {
        Handler(mainLoop).post(action)
    }
}

fun View.boundsInScreen(): Rect {
    val rect = Rect()
    val arr = IntArray(2)
    getLocationOnScreen(arr)
    rect.left = arr[0]
    rect.right = arr[0] + width
    rect.top = arr[1]
    rect.bottom = arr[1] + height
    return rect
}

//todo --> VTP
/**
 * 是否为输入法
 * @receiver AppInfo
 * @param context Context
 * @return Boolean
 */
fun AppInfo.isInputMethod(context: Context): Boolean {

    val pm = context.packageManager
    val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES)
    pkgInfo.services?.forEach {
        if (it.permission == Manifest.permission.BIND_INPUT_METHOD) {
            Vog.d(this, "isInputMethod ---> 输入法：$packageName")
            return true
        }
    }
    return false
}

val homeAppPkgs = mutableListOf<String>()

fun AppInfo.isHomeApp(): Boolean {
    return getHomes(GlobalApp.APP).contains(packageName)
}

val appActivityCache = hashMapOf<String, Array<String>>()
fun AppInfo.activities(): Array<String> {
    synchronized(AppInfo::class) {
        appActivityCache[packageName]?.let {
            return it
        }

        val pm = GlobalApp.APP.packageManager
        val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        val acs = pkgInfo.activities
        val rList = mutableListOf<String>()
        acs?.forEach { ac ->
            ac.name?.also {
                rList.add(it)
            }
        }
        return rList.toTypedArray().also {
            appActivityCache[packageName] = it
        }
    }
}

fun AppInfo.isUserApp(): Boolean {
    return (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
}

fun AppInfo.hasGrantedPermission(p: String): Boolean {
    val pm = GlobalApp.APP.packageManager

    return (PackageManager.PERMISSION_GRANTED == pm.checkPermission(p, packageName)).also {
        Vog.d(this, "hasPermission ---> $p $it")
    }
}
//麦克风权限App缓存
val microPermissionCache = hashMapOf<String, Boolean>()
fun AppInfo.hasMicroPermission(): Boolean {
    if (packageName == GlobalApp.APP.packageName) return false//排除自身
    microPermissionCache[packageName]?.also {
        Vog.d(this, "hasMicroPermission ---> $name 麦克风权限 $it")
        //若无权限 再次检查（可能动态申请）
        if (it) return true
    }

    val pm = GlobalApp.APP.packageManager
    val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
    pkgInfo.requestedPermissions?.forEach {
        if (it.endsWith(".RECORD_AUDIO") && hasGrantedPermission(it)) {
            Vog.d(this, "hasMicroPermission ---> $name 授权麦克风权限")
            microPermissionCache[packageName] = true
            return true
        }
    }
    Vog.d(this, "hasMicroPermission ---> $name 无麦克风权限")
    microPermissionCache[packageName] = false
    return false
}

private fun getHomes(context: Context): List<String> {
    if (homeAppPkgs.isNotEmpty()) return homeAppPkgs
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = packageManager.queryIntentActivities(intent,
            PackageManager.MATCH_DEFAULT_ONLY)
    resolveInfo?.forEach {
        homeAppPkgs.add(it.activityInfo.packageName)
        Vog.d("", "桌面应用 ---> ${it.activityInfo.packageName}")
    }
    return homeAppPkgs
}