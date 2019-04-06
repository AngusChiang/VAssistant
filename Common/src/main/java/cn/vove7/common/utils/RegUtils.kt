package cn.vove7.common.utils

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.model.InstSettingInfo
import cn.vove7.paramregexengine.toParamRegex
import cn.vove7.vtp.log.Vog

/**
 *
 *
 * Created by Vove on 2018/7/2
 */
object RegUtils {
    //    val REG_ALL_CHAR = "([\\S\\s]*?)"
    val REG_NUMBER_CHAR get() = "([0-9零一二两三四五六七八九十百千万]*)"
    val REG_ALL_CHAR get() = "([\\S\\s]*?)"

    private val confirmWords
        get() = arrayOf(
                "确[定认]"
                , "好(的)?"
                , "可以"
                , "[嗯是]%"
                , "继续%"
                , "yes"
                , "ok"
        )
    private val cancelWords = arrayOf(
            "取消"
            , "停止"
            , "不好"
            , "不要"
            , "stop"
            , "cancel"
    )

    /**
     * 确认命令
     * @param word String
     * @return Boolean
     */
    fun checkConfirm(word: String): Boolean {
        return check(word, confirmWords)
    }

    /**
     * 取消命令
     * @param word String
     * @return Boolean
     */
    fun checkCancel(word: String): Boolean {
        return check(word, cancelWords)
    }

    private fun check(w: String, arr: Array<String>): Boolean {
        val word = w.toLowerCase()
        arr.forEach {
            if (it.toParamRegex().match(word) != null)
                return true
        }
        return false
    }


    /**
     *
     * @param r String regex with %
     * @return Regex
     */
    private val RegisterMatcher
        get() = "settings[\\S\\s\n]*registerSettings\\([ ]*%[\\\"'](%)[\"']%settings%([0-9]*)[ ]*\\)"
                .replace("%", "[\\S ]*?").toRegex()


    /**
     *
     * @param r String regex with %
     * @return Regex
     */
    fun dealRawReg(r: String): Regex = r.replace("%", REG_ALL_CHAR)
            .replace("#", TextDateParser.REG_NUMBER_CHAR).toRegex()


    fun getRegisterSettingsTextAndName(s: String): InstSettingInfo? {
        val r = RegisterMatcher.find(s)
        if (r != null) {
            try {
                val script = r.groupValues[0]
                val name = r.groupValues[1]
                val version = if (r.groupValues.size < 3) null
                else r.groupValues[2]
                return InstSettingInfo(script, name, version?.toInt() ?: 0)
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalLog.err(e)
            }
        }
        return null
    }

    private const val rHeader = "require 'bridges'\n" +
            "local args = {...}\n" +
            "if args then\n" +
            "  argMap = args[1]\n" +
            "else\n" +
            "  argMap = nil\n" +
            "end\n"

    //todo 弃用
//    private val luaHeaderReg = "require[ ]+[\"']accessibility[\"']".toRegex()

    /**
     * 替换Lua 无障碍声明头部
     */
    fun replaceLuaHeader(s: String): String {
        return (rHeader + s.replace(headerReg,"requireAccessibility()")).also {
            //            print(it)
            Vog.d(it)
        }
    }

    private val headerReg get()= "require[ ]+[\"']accessibility[\"']".toRegex()

    /**
     * Rhino 无障碍声明头部
     */
    fun replaceRhinoHeader(s: String): String {
        return s.replace(headerReg,"requireAccessibility()")
    }

    val PACKAGE_REGEX get()= "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)+".toRegex()

    /**
     * 是否为包名
     * @param s String
     * @return Boolean
     */
    fun isPackage(s: String): Boolean {
        return PACKAGE_REGEX.matches(s)
    }
}