package cn.vove7.common.interfaces


/**
 * # ScriptEngine
 *
 * @author 11324
 * 2019/4/1
 */
interface ScriptEngine : Comparable<Any?> {
    @Throws(Exception::class)
    fun evalString(script: String, args: Array<*>? = null)

    @Throws(Exception::class)
    fun evalString(script: String, argMap: Map<String, *>? = null)

    @Throws(Exception::class)
    fun evalFile(file: String, args: Array<*>? = null)

    @Throws(Exception::class)
    fun evalFile(file: String, argMap: Map<String, *>? = null)

    fun stop()

    fun release()

    override fun compareTo(other: Any?): Int {
        return hashCode() - (other?.hashCode() ?: 0)
    }
}