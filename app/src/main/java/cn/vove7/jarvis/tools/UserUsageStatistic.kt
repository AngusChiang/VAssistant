package cn.vove7.jarvis.tools

import cn.vassistant.plugininterface.app.GlobalApp
import cn.vove7.vtp.sharedpreference.SpHelper

/**
 * # UserUsageStatistic
 * 使用数据统计
 * todo
 * @author Administrator
 * 2018/11/6
 */
object UserUsageStatistic {
    val sp get() = SpHelper(GlobalApp.APP, "uus")
    var commandCount: Int
        set(value) = sp.set("cc", value)
        get() = sp.getInt("cc").let { if (it < 0) 0 else it }


}