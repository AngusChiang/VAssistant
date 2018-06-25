package cn.vove7.vtp.app

import android.graphics.drawable.Drawable

/**
 * App信息类
 */
open class AppInfo(
        var name: String = "",
        var alias: String = name,
        var packageName: String = "",
        var icon: Drawable? = null,
        var pid: Int = 0,
        var priority: Int = 0
) {
    override fun toString(): String {
        return name
    }
}