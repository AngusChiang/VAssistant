package cn.vove7.jarvis.tools

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.AppPermission
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.ShellHelper
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.activityShot
import cn.vove7.common.utils.newTask
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.services.AssistSessionService
import cn.vove7.vtp.log.Vog
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * # utils
 *
 * @author 11324
 * 2019/3/22
 */

/**
 *
 * @param activity Activity
 * @return String?
 */
@Deprecated("无法截取到Activity后面内容")
fun captureActivity2Cache(activity: Activity): String? {
    val cachePath = activity.cacheDir.absolutePath +
            "/screen-${Random().nextInt(999)}.png"

    return activityShot(activity)?.let {
        val a = UtilBridge.bitmap2File(it,
                cachePath)?.absolutePath
        it.recycle()
        a
    }

}

fun openQQChat(qq: String) {
    val qqIntent = Intent(Intent.ACTION_VIEW,
            Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=$qq&version=1"))
    try {
        GlobalApp.APP.startActivity(qqIntent.newTask())
    } catch (e: Exception) {
        GlobalLog.err(e)
        GlobalApp.toastError("唤起QQ失败")
    }
}

/**
 * root自动开启 or 系统应用
 */
fun openAccessibilityServiceAuto() {
    Vog.d("打开无障碍")
    if (AccessibilityApi.isBaseServiceOn) return
    if (AppConfig.autoOpenAS) {
        GlobalLog.log("自启打开无障碍服务")
        AccessibilityApi.openServiceSelf(AccessibilityApi.WHICH_SERVICE_BASE)
    }
    if (AppConfig.autoOpenAAS) {
        GlobalLog.log("自启打开高级无障碍服务")
        AccessibilityApi.openServiceSelf(AccessibilityApi.WHICH_SERVICE_GESTURE)
    }
}

/**
 * 设为默认助手应用
 */
fun setAssistantAppAuto() {
    if (AppConfig.autoSetAssistantApp) {
        try {
            setAssistantApp()
        } catch (e: Throwable) {
            GlobalLog.err("设置助手应用失败[by Settings.Secure]：" + e.message)
        }
    }
}

@Throws
fun setAssistantApp() {
    val app = GlobalApp.APP
    val name = "${app.packageName}/${AssistSessionService::class.java.name}"

    if (AppPermission.canWriteSecureSettings) {
        GlobalLog.log("设为助手应用[WriteSecureSettings]")
        val cr = app.contentResolver
        Settings.Secure.putString(cr, "assistant", name)
        Settings.Secure.putString(cr, "voice_interaction_service", name)

    } else if (ShellHelper.hasRootOrAdb()) {
        GlobalLog.log("设为助手应用[ROOT]")
        ShellHelper.execAuto("settings put secure assistant $name")
        ShellHelper.execAuto("settings put secure voice_interaction_service $name")
    }
}

fun wirelessDebug(en: Boolean) {
    try {
        ShellHelper.execWithSu("setprop service.adb.tcp.port ${if (en) "5555" else "-1"}\n" +
                "stop adbd\n" +
                "start adbd")
    } catch (e: Exception) {
        e.printStackTrace()
        GlobalApp.toastError(e.message ?: "")
    }
}

fun isWirelessDebugEnable(): Boolean {
    try {
        val proc = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(proc.outputStream)
        os.writeBytes("getprop service.adb.tcp.port\n")
        os.flush()
        os.close()
        val reader = InputStreamReader(proc.inputStream)
        val chars = CharArray(5)
        reader.read(chars)
        reader.close()
        proc.destroy()
        val result = String(chars)
        return result.matches("[0-9]+\\n".toRegex()) && !result.contains("-1")
    } catch (e: IOException) {
        return false
    }
}

inline fun onDebug(action: () -> Unit) {
    if (BuildConfig.DEBUG) action()
}


/**
 * ContextWrapper 转 Activity
 */
val Context.asActivity: Activity?
    get() =
        when (this) {
            is Activity -> this
            is ContextWrapper -> this.baseContext.asActivity
            else -> null
        }
