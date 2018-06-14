package cn.vove7.parseengine.engine

/**
 * # ParseEngine
 *
 * Created by Vove on 2018/6/15
 */
object ParseEngine {

    fun parse(word: String) {

    }

    const val TYPE_ = 1
    private val words = mapOf(
            Pair("*[打开|启动]*{p1}", TYPE_),
            Pair("*点击*{p1}", TYPE_),
            Pair("*长按*{p1}", TYPE_),
            Pair("*选择*{p1}", TYPE_),
            Pair("返回", TYPE_),
            Pair("[跳[回]?|返回]?[到]?主页", TYPE_),
            Pair("[打开]?最近应用", TYPE_)
    )

    init {
        words.iterator().forEach {
            it.key.replace("*", "[\\S]?")
        }
    }


}