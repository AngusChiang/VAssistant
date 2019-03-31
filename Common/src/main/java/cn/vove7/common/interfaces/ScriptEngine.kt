package cn.vove7.common.interfaces


/**
 * # ScriptEngine
 *
 * @author 11324
 * 2019/4/1
 */
interface ScriptEngine {
    @Throws(Exception::class)
    fun evalString(script: String, args: Array<*>? = null)

    @Throws(Exception::class)
    fun evalString(script: String, argMap: Map<String, Any>? = null)

    @Throws(Exception::class)
    fun evalFile(file: String, args: Array<*>? = null)

    @Throws(Exception::class)
    fun evalFile(file: String, argMap: Map<String, Any>? = null)

    fun stop()

}