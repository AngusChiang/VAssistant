package cn.vove7.jarvis.utils

/**
 *
 *
 * Created by Vove on 2018/7/2
 */
object Utils {
    private val atWillChars = "([\\S]*)"
    private val confirmWords = arrayOf(
            "确([定认])".toRegex()
            , "好(的)?".toRegex()
            , "可以".toRegex()
            , "嗯$atWillChars".toRegex()
            , "继续$atWillChars".toRegex()
    )
    private val cancelWords = arrayOf(
            "取消".toRegex()
            , "停止".toRegex()
    )

    fun checkConfirm(word: String): Boolean {
        confirmWords.forEach {
            if (it.matches(word))
                return true
        }
        return false
    }

    fun checkCancel(word: String): Boolean {
        cancelWords.forEach {
            if (it.matches(word))
                return true
        }
        return false
    }
}