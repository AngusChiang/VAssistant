package cn.vove7.vtp.app

import android.graphics.drawable.Drawable

data class AppInfo(
        var name: String,
        var alias: String = name,
        var packageName: String,
        var icon: Drawable,
        var pid: Int = 0,
        var priority: Int = 0
) {
    override fun toString(): String {
        return name
    }
}