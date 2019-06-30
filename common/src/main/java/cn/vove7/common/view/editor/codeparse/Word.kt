package cn.vove7.common.view.editor.codeparse

/**
 * 单词
 */
class Word constructor(
        val word: String,
        /**
         * wordType：
         * 0：空格
         * 1：关键字
         * 2：分界符
         * 3：算数运算符
         * 4：关系运算符
         * 5：常数
         * 6：标识符
         * 7：字符串
         */
        val wordType: Int//单词种别
        , val wordAttr: String//单词属性
        , val row: Int//行
        , val col: Int//列,
        , val start: Int
        , val end: Int
        , val error: Boolean = false
) {
    private var type: String? = null//类型

    override fun toString(): String {

        return String.format("%-10s \t (%d, %d-%d) \t\t%s",
                type, row, start, end, word)
    }

    init {
        this.type = types[wordType]
        if (error) {
            type = "ERROR\t"
        }
    }

    companion object {

        private val types = arrayOf("", "关键字", "分界符", "算数运算符", "关系运算符", "常数", "标识符", "字符串")
    }

}
