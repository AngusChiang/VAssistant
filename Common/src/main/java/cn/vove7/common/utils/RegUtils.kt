package cn.vove7.common.utils

/**
 *
 *
 * Created by Vove on 2018/7/2
 */
object RegUtils {
    public val REG_ALL_CHAR = "([\\S\\s]*)"
    private val confirmWords = arrayOf(
            "确([定认])".toRegex()
            , "好(的)?".toRegex()
            , "可以".toRegex()
            , "嗯$REG_ALL_CHAR".toRegex()
            , "继续$REG_ALL_CHAR".toRegex()
            , "yes".toRegex()
            , "ok".toRegex()
    )
    private val cancelWords = arrayOf(
            "取消".toRegex()
            , "停止".toRegex()
            , "stop".toRegex()
            , "cancel".toRegex()
    )

    fun checkConfirm(word: String): Boolean {
        val word = word.toLowerCase()
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

    /**
     *
     * @param r String regex with %
     * @return Regex
     */
    fun dealRawReg(r: String): Regex = r.replace("%", REG_ALL_CHAR).toRegex()

}