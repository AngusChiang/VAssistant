package cn.vove7.jarvis.tools

import android.content.pm.PackageManager
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vove7.jarvis.BuildConfig

object BaiduKey {
    val appId: Int
    val appKey: String
    val sKey: String

    init {
        val appInfo = GlobalApp.APP.let {
            it.packageManager.getApplicationInfo(it.packageName,
                    PackageManager.GET_META_DATA)
        }
        if (!BuildConfig.DEBUG) {
            appId = appInfo.metaData.getInt("com.baidu.speech.APP_ID")
            appKey = appInfo.metaData.getString("com.baidu.speech.API_KEY")!!
            sKey = appInfo.metaData.getString("com.baidu.speech.SECRET_KEY")!!
        } else {
            appId = 11389525
            appKey = "ILdLUepG75UwwQVa0rqiEUVa"
            sKey = "di6djKXGGELgnCCusiQUlCBYRxXVrr46"
        }
    }
}