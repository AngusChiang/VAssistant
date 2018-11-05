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
    appActivityCache[packageName].let {
        if (it != null) return it
    }

    val pm = GlobalApp.APP.packageManager
    val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    val acs = pkgInfo.activities
    val rList = mutableListOf<String>()
    acs.forEach { ac ->
        ac.name.also {
            if (it != null)
                rList.add(it)
        }
    }
    return rList.toTypedArray().also {
        Vog.d(this, "activities ---> ${Arrays.toString(it)}")
        appActivityCache[packageName] = it
    }
}

fun AppInfo.isUserApp(): Boolean {
    return (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
}

private fun getHomes(context: Context): List<String> {
    if (homeAppPkgs.isNotEmpty()) return homeAppPkgs
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = packageManager.queryIntentActivities(intent,
            PackageManager.MATCH_DEFAULT_ONLY)
    for (ri in resolveInfo) {
        homeAppPkgs.add(ri.activityInfo.packageName)
        Vog.d("", "桌面应用 ---> ${ri.activityInfo.packageName}")
    }
    return homeAppPkgs
}