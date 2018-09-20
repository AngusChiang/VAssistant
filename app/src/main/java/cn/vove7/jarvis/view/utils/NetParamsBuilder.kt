package cn.vove7.jarvis.view.utils

import cn.vove7.common.netacc.tool.SecureHelper
import java.util.*

/**
 * # NetParamsBuilder
 *
 * @author Administrator
 * 2018/9/14
 */
class NetParamsBuilder {
    val map = TreeMap<String, String>()

    fun add(p: Pair<String, String>) {
        map[p.first] = p.second
    }

    fun sign(): TreeMap<String, String> {
        map["timestamp"] = (System.currentTimeMillis() / 1000).toString()
        SecureHelper.signParam(map)
        return map
    }

    fun addAll(pairs: Array<Pair<String, String>>) {
        pairs.forEach {
            add(it)
        }
    }

    companion object {
        fun of(pairs: Array<Pair<String, String>>): NetParamsBuilder {
            val bu = NetParamsBuilder()
            bu.addAll(pairs)
            return bu
        }

        fun of(p: Pair<String, String>): NetParamsBuilder {
            val bu = NetParamsBuilder()
            bu.add(p)
            return bu
        }
    }
}