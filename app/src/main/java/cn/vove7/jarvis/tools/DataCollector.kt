package cn.vove7.jarvis.tools

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.model.UserInfo
import com.umeng.analytics.MobclickAgent

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
        MobclickAgent.onEvent(context, eventId)
    }


}