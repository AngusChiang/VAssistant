package com.myopicmobile.textwarrior.common

import cn.vove7.common.interfaces.VApi
import cn.vove7.common.interfaces.VApi.Companion.JS_OPERATORS
import cn.vove7.common.interfaces.VApi.Companion.appFunctions
import cn.vove7.common.interfaces.VApi.Companion.executorFunctions
import cn.vove7.common.interfaces.VApi.Companion.finderFuns
import cn.vove7.common.interfaces.VApi.Companion.globalFuns
import cn.vove7.common.interfaces.VApi.Companion.inputFuns
import cn.vove7.common.interfaces.VApi.Companion.keywordss
import cn.vove7.common.interfaces.VApi.Companion.myApiName
import cn.vove7.common.interfaces.VApi.Companion.runtimeFunctions
import cn.vove7.common.interfaces.VApi.Companion.spFuncs
import cn.vove7.common.interfaces.VApi.Companion.systemFuncs
import cn.vove7.common.interfaces.VApi.Companion.utilFuns
import cn.vove7.common.interfaces.VApi.Companion.viewNodeFunctions
import cn.vove7.common.utils.ArrayUtil

/**
 * Created by Administrator on 2018/10/9
 */
@Deprecated("unused")
class LanguageJs private constructor() : Language(), VApi {
    init {
        val kkk= ArrayUtil.merge(arrayOf(keywordss,
                appFunctions, runtimeFunctions, executorFunctions,
                viewNodeFunctions, finderFuns, globalFuns, systemFuncs
                , utilFuns, spFuncs))

        setOperators(JS_OPERATORS)


        super.setKeywords(kkk)
        super.setNames(ArrayUtil.merge(arrayOf(kkk, myApiName)))

        super.addBasePackage("app", appFunctions)
        super.addBasePackage("runtime", runtimeFunctions)
        super.addBasePackage("input", inputFuns)
        super.addBasePackage("ViewFinder()", finderFuns)
        super.addBasePackage("system", systemFuncs)
    }

    companion object {

        var _theOne: Language? = null
        val instance: Language
            get() {
                if (_theOne == null) {
                    _theOne = LanguageJs()
                }
                currentLang = _theOne
                return _theOne!!
            }
    }

    /**
     * Whether the word after c is a token
     */
    fun isWordStart2(c: Char): Boolean {
        return c == '.'
    }

    override fun isLineAStart(c: Char): Boolean {
        return false
    }

    /**
     * Whether c0c1L is a token, where L is a sequence of characters until the end of the line
     */
    override fun isLineStart(c0: Char, c1: Char): Boolean {
        return c0 == '/' && c1 == '/'
    }

    override fun isMultilineStartDelimiter(c0: Char, c1: Char): Boolean {
        return c0 == '/' && c1 == '*'
    }

    /**
     * Whether c0c1 signifies the end of a multi-line token
     */
    override fun isMultilineEndDelimiter(c0: Char, c1: Char): Boolean {
        return c0 == '*' && c1 == '/'
    }

}
