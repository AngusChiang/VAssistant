package cn.vove7.common.utils

import cn.vove7.common.app.GlobalLog
import cn.vove7.paramregex.toParamRegex
import java.io.File

/**
 * # TextHelper
 *
 * @author Vove
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

    fun arr2String(ss: Array<*>?, separator: String = ","): String? {
        if (ss == null) return null
        return buildString {
            ss.withIndex().forEach {
                if (it.index == 0) append(ss[0])
                else
                    append(separator + ss[it.index])
            }
        }
    }

    /**
     * 包含比例
     * 长度小于3 不比较
     */
    fun containsRatio(s1: String, s2: String): Float {
        val shorter: String
        val lener = if (s1.length > s2.length) {
            shorter = s2
            s1
        } else {
            shorter = s1
            s2
        }
        if (lener.length <= 3) return 0f
        return if (lener.contains(shorter, ignoreCase = true)) {
            shorter.length.toFloat() / lener.length
        } else 0f
    }

    fun matches(text: String?, regexStr: String?): Boolean {
        if (text == null || regexStr == null) return false
        return regexStr.toParamRegex().match(text) != null
    }

    /**
     * 根据正则字符串，匹配出占位符对应的值
     * @param text String
     * @param regexStr String
     * @return Array<String>
     */
    fun matchValues(text: String?, regexStr: String?): Array<String>? {
        if (text == null || regexStr == null) return null
        val reg = RegUtils.dealRawReg(regexStr)
        val r = reg.matchEntire(text)
        return r?.groupValues?.subList(1, r.groupValues.size)?.toTypedArray()
    }

    /**
     *
     * @param text String?
     * @param regexStr String?
     * @return Map<String,Any>?
     */
    fun matchParam(text: String?, regexStr: String?): Map<String, Any>? {
        if (text == null || regexStr == null) return null

        return regexStr.toParamRegex().match(text)
    }

    fun readFile(path: String): String? {
        val f = File(path)
        return try {
            f.readText()
        } catch (e: Exception) {
            GlobalLog.err(e)
            null
        }
    }

    fun writeFile(path: String, content: String): Boolean {
        return try {
            val f = File(path)
            f.writeText(content)
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }
}