package cn.vove7.jarvis.tools

import android.app.Activity
import android.content.Intent
import android.net.Uri
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.activityShot
import cn.vove7.common.utils.newTask
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
