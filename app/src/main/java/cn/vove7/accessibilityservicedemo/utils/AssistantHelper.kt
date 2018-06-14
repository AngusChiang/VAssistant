package cn.vove7.accessibilityservicedemo.utils

import android.content.Intent
import cn.vove7.accessibilityservicedemo.App
import cn.vove7.accessibilityservicedemo.utils.MessageEvent.Companion.WHAT_ERR
import cn.vove7.parseengine.utils.app.AppHelper
import cn.vove7.vtp.log.Vog


object AssistantHelper : AssistantCapacity {

    override fun openApp(pkg: String): Boolean {
        return try {
            val launchIntent = App.instance.packageManager
                    .getLaunchIntentForPackage(pkg)
            if (launchIntent == null) {
                Bus.postInfo(MessageEvent("启动失败(未找到此App[pkg:]):$pkg ", WHAT_ERR))
                false
            } else {
                App.instance.startActivity(launchIntent)
                true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Vog.wtf(this, "${e.message}")
            Bus.postInfo(MessageEvent("启动失败:$pkg  msg:${e.message}", WHAT_ERR))
            false
        }
    }

    override fun openAppByWord(appWord: String): Boolean {
        val list = AppHelper(App.instance).matchAppName(appWord)
        if (list.isNotEmpty()) {
            val info = list[0].app
            Vog.i(this, "打开应用：$appWord -> ${info.name}")
            return openApp(info.packageName)
        }
        Bus.postInfo(MessageEvent("未找到应用:$appWord", WHAT_ERR))
        return false
    }


    fun startActivity(pkg: String, activityName: String): Boolean {
        return try {
            val launchIntent = Intent()
            launchIntent.setClassName(pkg, pkg + activityName)
            App.instance.startActivity(launchIntent)
            true
        } catch (e: Exception) {
            Vog.wtf(this, "${e.message}")
            false
        }
    }

}

internal interface AssistantCapacity {
    /**
     * 通过包名打开App
     */
    fun openApp(pkg: String): Boolean

    /**
     * 通过通过关键字匹配
     */
    fun openAppByWord(appWord: String): Boolean
}