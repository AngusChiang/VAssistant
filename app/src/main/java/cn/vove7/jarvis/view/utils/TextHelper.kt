package cn.vove7.jarvis.view.utils

/**
 * # TextHelper
 *
 * @author 17719247306
 * 2018/9/12
 */
object TextHelper {

    private val emailValid = Regex("[^@]([\\S])*?@([\\S])*?\\.([\\S])+")

    fun isEmail(s: String?): Boolean {
        return s != null && emailValid.matches(s)
    }

    private val userRegex = Regex("[0-9a-zA-Z[\\u4e00-\\u9fa5]]+")
    fun checkUserName(s: String?): Boolean {
        if (s == null) return false
        return userRegex.matches(s)
    }

    fun arr2String(ss: Array<String>, separator: String = ","): String {
        return buildString {
            ss.withIndex().forEach {
                if (it.index == 0) append(ss[0])
                else
                    append(separator + ss[it.index])
            }
        }
    }
}