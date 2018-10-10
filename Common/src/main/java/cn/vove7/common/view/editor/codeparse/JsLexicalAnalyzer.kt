package cn.vove7.common.view.editor.codeparse


/**
 * Created by Administrator.
 * Date: 2018/10/9
 */
class JsLexicalAnalyzer : AbsLexicalAnalyzer() {

    //分界符
    override var delimiters: Map<Int, String> = hashMapOf(
            Pair(0, ",")
            , Pair(1, ";")
            , Pair(2, "(")
            , Pair(3, ")")
            , Pair(4, "[")
            , Pair(5, "]")
            , Pair(6, "{")
            , Pair(7, "}")
            , Pair(8, ".")
    )
    //关键字
    override val keyWords: HashMap<Int, String> by lazy {
        val m = hashMapOf<Int, String>()
        mutableListOf(
                "break", "else",
                "new", "var",
                "case", "finally",
                "return", "void", "catch",
                "for", "switch", "while",
                "continue", "function", "this",
                "with", "default",
                "if", "throw", "delete",
                "in", "try",
                "do", "instranceof", "typeof"
        ).withIndex().forEach {
            m[it.index] = it.value
        }
        m

    }

    //算数运算符
    override var arithmeticWords: Map<Int, String> =
        hashMapOf(
                Pair(0x10, "+")
                , Pair(0x11, "++")
                , Pair(0x12, "-")
                , Pair(0x13, "--")
                , Pair(0x20, "*")
                , Pair(0x21, "/")
        )
    //关系运算符
    override var relationOperators: Map<Int, String> =
        hashMapOf(Pair(0x0, "<")
                , Pair(0x1, "<=")
                , Pair(0x2, "=")
                , Pair(0x3, ">")
                , Pair(0x4, ">+")
                , Pair(0x5, "<>")
                , Pair(0x6, "==")
                , Pair(0x7, "===")
                , Pair(0x8, "!=")
                , Pair(0x9, "!==")
        )
}
