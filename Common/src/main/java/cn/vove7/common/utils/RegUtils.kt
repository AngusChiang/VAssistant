package cn.vove7.common.utils

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.model.InstSettingInfo
import cn.vove7.vtp.log.Vog

/**
 *
 *
 * Created by Vove on 2018/7/2
 */
object RegUtils {
    val REG_ALL_CHAR = "([\\S\\s]*?)"
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
            , "不好".toRegex()
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

    private val RegisterMatcher = "settings[\\S\\s\n]*registerSettings\\([ ]*%[\\\"'](%)[\"']%settings%([0-9]*)[ ]*\\)"
            .replace("%", "[\\S ]*?").toRegex()

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

    private const val rHeader = "require 'bridges'\nlocal args = { ... }\n"
    private val luaHeaderReg = "require[ ]+[\"']accessibility[\"']".toRegex()

    /**
     * 替换Lua 无障碍声明头部
     */
    fun replaceLuaHeader(s: String): String {
        return (rHeader + s.replace(luaHeaderReg,
                "if (not accessibility()) then return end")
                ).also {
            //            print(it)
            Vog.d(this,"replaceLuaHeader ---> $it")
        }
    }

    /**
     * Rhino 无障碍声明头部
     */
    fun replaceRhinoHeader(s: String): String {
        var newS =
            s.replace(luaHeaderReg,
                    "if (accessibility()) {\n")
        if (newS != s) {
            newS += "\n}"
        }
        Vog.d(this,"replaceRhinoHeader ---> $newS")
        return newS
    }

    val PACKAGE_REGEX = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)+".toRegex()

    /**
     * 是否为包名
     * @param s String
     * @return Boolean
     */
    fun isPackage(s:String) :Boolean{
        return PACKAGE_REGEX.matches(s)
    }
}