package cn.vove7.parseengine.model

import android.graphics.drawable.Drawable

data class AppInfo(
        var name: String,
        var packageName: String,
        var icon: Drawable,
        var pid: Int = 0,
        var priority: Int = 0
)