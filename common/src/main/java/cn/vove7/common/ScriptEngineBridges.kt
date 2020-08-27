package cn.vove7.common

import androidx.annotation.Keep

/**
 * @author 17719
 * 脚本Api
 *
 * 2018/8/6
 */
class ScriptEngineBridges(vararg apis: Pair<String, Any>) {

    val apis = mutableMapOf(*apis)

    fun add(k: String, v: Any) {
        apis[k] = v
    }

    @Keep
    fun get(k: String): Any? = apis[k]

}