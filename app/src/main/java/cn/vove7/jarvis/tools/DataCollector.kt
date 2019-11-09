package cn.vove7.jarvis.tools

import android.content.Context
import cn.jiguang.analytics.android.api.CountEvent
import cn.jiguang.analytics.android.api.JAnalyticsInterface
import cn.jiguang.analytics.android.api.LoginEvent
import cn.jiguang.analytics.android.api.RegisterEvent
import cn.vove7.common.app.GlobalApp


/**
 * # DataCollector
 * 埋点数据采集
 * @author Vove
 * 2019/5/13
 */
object DataCollector {

    private val context: Context get() = GlobalApp.APP

    /**
     * 埋点统计
     */
    fun buriedPoint(eventId: String) {
        val cEvent = CountEvent(eventId)
        JAnalyticsInterface.onEvent(context, cEvent)
    }

    fun onUserRegister() {
        val rEvent = RegisterEvent("raw_user", true)
        JAnalyticsInterface.onEvent(context, rEvent)
    }

    fun onProfileSignOff() {
        val cEvent = CountEvent("logout")
        JAnalyticsInterface.onEvent(context, cEvent)
    }

    fun onProfileSignIn(userId: String) {
        val cEvent = LoginEvent("raw_user", true)
        cEvent.extMap = mapOf(
                "user_id" to userId
        )
        JAnalyticsInterface.onEvent(context, cEvent)
    }

    fun onPageStart(act: Context, pageName: String) {
        JAnalyticsInterface.onPageStart(act, pageName)
    }

    fun onPageEnd(act: Context, pageName: String) {
        JAnalyticsInterface.onPageEnd(act, pageName)
    }

}