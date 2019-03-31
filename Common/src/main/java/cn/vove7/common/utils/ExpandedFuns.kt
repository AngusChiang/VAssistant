package cn.vove7.common.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.annotation.ColorRes
import android.support.v4.app.ActivityCompat
import android.util.DisplayMetrics
import android.view.View
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import java.text.SimpleDateFormat
import java.util.*


/**
 * # ExpandedFuns
 * 扩展函数集合
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
        try {
            Handler(mainLoop).post(action)
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
    }
}

fun runOnNewHandlerThread(name: String = "anonymous", autoQuit: Boolean = true,
                          delay: Long = 0, run: () -> Unit): HandlerThread {
    return HandlerThread(name).apply {
        start()
        Vog.d("runOnNewHandlerThread ---> $name")
        Handler(looper).postDelayed({
            run.invoke()
            if (autoQuit)
                quitSafely()
        }, delay)
    }
}

/**
 * 循环执行等待结果；超时返回空
 * eg用于视图搜索
 * @param waitMillis Long
 * @param run () -> T 返回空时，重新执行，直到超时
 * @return T
 */
fun <T> whileWaitTime(waitMillis: Long, run: () -> T?): T? {
    val begin = System.currentTimeMillis()
    var now: Long
    val ct = Thread.currentThread()
    do {
        run.invoke()?.also {
            //if 耗时操作
            return it
        }
        now = System.currentTimeMillis()
    } while (now - begin < waitMillis && !ct.isInterrupted)
    return null
}

/**
 * 循环执行等待run结果；超过次数返回空
 *
 * @param waitCount Int
 * @param run () -> T?
 * @return T? 执行结果
 */
fun <T> whileWaitCount(waitCount: Int, run: () -> T?): T? {
    var count = 0
    val ct = Thread.currentThread()
    while (count++ < waitCount && !ct.isInterrupted) {
        run.invoke()?.also {
            //if 耗时操作
            return it
        }
    }
    return null
}

fun prints(vararg msgs: Any?) {
    msgs.forEach {
        print(it)
        print(" ")
    }
}

fun formatNow(pat: String): String = SimpleDateFormat(pat, Locale.getDefault()).format(Date())

fun Context.startActivityOnNewTask(intent: Intent) {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.startActivityOnNewTask(actCls: Class<*>) {
    val intent = Intent(this, actCls)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
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
val inputMethodCache = hashMapOf<String, Boolean>()

fun AppInfo.isInputMethod(context: Context): Boolean {
    inputMethodCache[packageName]?.also {
        return it
    }
    val pm = context.packageManager
    val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES)
    pkgInfo.services?.forEach {
        if (it.permission == Manifest.permission.BIND_INPUT_METHOD) {
            Vog.d("isInputMethod ---> 输入法：$packageName")
            inputMethodCache[packageName] = true
            return true
        }
    }
    inputMethodCache[packageName] = false
    return false
}


fun AppInfo.isHomeApp(): Boolean {
    return AppInfo.getHomes(GlobalApp.APP).contains(packageName)
}

val appActivityCache = hashMapOf<String, Array<String>>()

//fixme packageManager died
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
        return rList.toTypedArray()
    }
}


//麦克风权限App缓存
val microPermissionCache = hashMapOf<String, Boolean>()

fun AppInfo.hasMicroPermission(): Boolean {
    if (packageName == GlobalApp.APP.packageName) return false//排除自身
    microPermissionCache[packageName]?.also {
        Vog.d("hasMicroPermission ---> $name 麦克风权限 $it")
//        //若无权限 再次检查（可能动态申请）
//        if (it) return true
        return it
    }

    try {
        val pm = GlobalApp.APP.packageManager
        val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        pkgInfo?.requestedPermissions?.forEach {
            if (it.endsWith(".RECORD_AUDIO") && hasGrantedPermission(it)) {
                Vog.d("hasMicroPermission ---> $name 授权麦克风权限")
                microPermissionCache[packageName] = true
                return true
            }
        }
    } catch (e: Exception) {
        GlobalLog.err(e)
    }
    Vog.d("hasMicroPermission ---> $name 无麦克风权限")
    microPermissionCache[packageName] = false
    return false
}


fun View.toggleVisibility(toggleVisibility: Int = View.GONE) {
    runOnUi {
        visibility = if (visibility == toggleVisibility) {
            View.VISIBLE
        } else {
            toggleVisibility
        }
    }
}

fun View.isVisibility(): Boolean = visibility == View.VISIBLE

fun View.gone() {
    runOnUi {
        visibility = View.GONE
    }
}

fun View.show() {
    runOnUi {
        visibility = View.VISIBLE
    }
}

fun View.inVisibility() {
    runOnUi {
        visibility = View.INVISIBLE
    }
}


fun View.setPadding(pad: Int) {
    setPadding(pad, pad, pad, pad)
}

fun Intent.newTask(): Intent {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return this
}

/**
 * 当没有key时,设置map[k] = v 并返回v
 * @receiver Map<K, V>
 * @param k K
 * @param v V
 * @return V
 */
inline fun <reified K, reified V> HashMap<K, V>.getOrSetDefault(k: K, v: V): V {
    return if (containsKey(k)) {
        get(k)!!
    } else {
        this[k] = v
        v
    }
}

/**
 * 兼容检测权限
 * @receiver Context
 * @param p String
 */
fun Context.checkPermission(p: String): Boolean {
    val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        checkSelfPermission(p)
    else ActivityCompat.checkSelfPermission(this, p)

    return result == PackageManager.PERMISSION_GRANTED
}

fun Context.color(@ColorRes id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(id)
    } else {
        resources.getColor(id)
    }
}

/**
 * 截取Activity
 * @param activity Activity
 * @param removeStatusBar Boolean
 * @return Bitmap?
 */
fun activityShot(activity: Activity, removeStatusBar: Boolean = false): Bitmap? {
    Vog.d("$activity")
    val view = activity.window.decorView
    view.setWillNotCacheDrawing(false)
    view.buildDrawingCache()
    val rect = Rect()

    view.getWindowVisibleDisplayFrame(rect)
    val statusbarHeight = rect.top
    val winManager = activity.windowManager

    val outMetrics = DisplayMetrics()
    winManager.defaultDisplay.getMetrics(outMetrics)

    val w = outMetrics.widthPixels
    val h = outMetrics.heightPixels

    //去除状态栏
    val bm = Bitmap.createBitmap(view.drawingCache, 0,
            if (removeStatusBar) statusbarHeight else 0, w, h - statusbarHeight)

    Vog.d("${bm?.width} x ${bm?.height}")

    view.destroyDrawingCache()
    view.setWillNotCacheDrawing(true)
    return bm
}


operator fun String.times(number: Int): String {
    return buildString {
        for (i in 1..number) {
            append(this@times)
        }
    }
}
