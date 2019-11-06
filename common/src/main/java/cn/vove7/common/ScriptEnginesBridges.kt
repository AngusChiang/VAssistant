package cn.vove7.common

/**
 * @author 17719
 * 脚本Api
 *
 * 2018/8/6
 */
class ScriptEnginesBridges(vararg apis: Pair<String, Any>) {

    val apis = mutableMapOf(*apis)

    fun add(k: String, v: Any) {
        apis[k] = v
    }

    fun get(k: String): Any? = apis[k]

}