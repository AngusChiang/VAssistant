package cn.vove7.jarvis.app

import cn.vove7.common.bridges.HttpBridge
import cn.vove7.quantumclock.Syncher
import org.json.JSONObject

/**
 * # MyTimeSyncher
 *
 * @author Vove
 * 2021/6/5
 */
object MyTimeSyncher : Syncher {
    override val priority: Int = 1

    override val name: String get() = "淘宝"

    override suspend fun getMillisTime(): Long {
        val data = HttpBridge.get("http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp")
        val obj = JSONObject(data)
        return (obj["data"] as JSONObject).getString("t").toLong()
    }
}