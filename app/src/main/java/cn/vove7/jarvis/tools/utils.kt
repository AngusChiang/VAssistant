package cn.vove7.jarvis.tools

import android.app.Activity
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.activityShot
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