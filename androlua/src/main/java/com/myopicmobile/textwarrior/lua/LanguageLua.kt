/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package com.myopicmobile.textwarrior.lua

import cn.vove7.common.interfaces.VApi
import cn.vove7.common.interfaces.VApi.Companion.appFunctions
import cn.vove7.common.interfaces.VApi.Companion.executorFunctions
import cn.vove7.common.interfaces.VApi.Companion.finderFuns
import cn.vove7.common.interfaces.VApi.Companion.globalFuns
import cn.vove7.common.interfaces.VApi.Companion.keywordss
import cn.vove7.common.interfaces.VApi.Companion.myApiName
import cn.vove7.common.interfaces.VApi.Companion.runtimeFunctions
import cn.vove7.common.interfaces.VApi.Companion.spFuncs
import cn.vove7.common.interfaces.VApi.Companion.systemFuncs
import cn.vove7.common.interfaces.VApi.Companion.utilFuns
import cn.vove7.common.interfaces.VApi.Companion.viewNodeFunctions
import cn.vove7.common.utils.ArrayUtil
import com.myopicmobile.textwarrior.common.Language

/**
 * Singleton class containing the symbols and operators of the Javascript language
 */
class LanguageLua private constructor() : Language(), VApi {

    init {
        super.setOperators(LUA_OPERATORS)

        val kkk=ArrayUtil.merge(arrayOf(keywordsss, keywordss, appFunctions,
                runtimeFunctions, executorFunctions, viewNodeFunctions, finderFuns,
                globalFuns, systemFuncs, utilFuns, spFuncs)
        )
        super.setKeywords(kkk)
        super.setNames(ArrayUtil.merge(arrayOf(kkk, myApiName)))

        super.addBasePackage("app", appFunctions)
        super.addBasePackage("runtime", runtimeFunctions)
        super.addBasePackage("ViewFinder()", finderFuns)
        super.addBasePackage("system", systemFuncs)

        super.addBasePackage("io", package_io.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("string", package_string.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("luajava", package_luajava.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("os", package_os.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("table", package_table.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("math", package_math.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("utf8", package_utf8.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("coroutine", package_coroutine.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("package", package_package.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())
        super.addBasePackage("debug", package_debug.split("\\|".toRegex()).dropLastWhile
        { it.isEmpty() }.toTypedArray())


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
        return c0 == '-' && c1 == '-'
    }

    /**
     * Whether c0c1 signifies the start of a multi-line token
     */
    override fun isMultilineStartDelimiter(c0: Char, c1: Char): Boolean {
        return c0 == '[' && c1 == '['
    }

    /**
     * Whether c0c1 signifies the end of a multi-line token
     */
    override fun isMultilineEndDelimiter(c0: Char, c1: Char): Boolean {
        return c0 == ']' && c1 == ']'
    }

    companion object {
        private val keywordTarget = "and|break|do|else|elseif|end|false|for|function|goto|if|in|local|nil|not|or|repeat|return|then|true|until|while"

        //private final static String functionTarget   = "_ENV|_G|_VERSION|assert|collectgarbage|coroutine|create|isyieldable|resume|running|status|wrap|yield|debug|gethook|getinfo|getlocal|getmetatable|getregistry|getupvalue|getuservalue|sethook|setlocal|setmetatable|setupvalue|setuservalue|traceback|upvalueid|upvaluejoin|dofile|error|getfenv|getmetatable|io|close|flush|input|lines|open|output|popen|read|stderr|stdin|stdout|tmpfile|type|write|ipairs|load|loadfile|loadstring|luajava|bindClass|clear|coding|createArray|createProxy|instanceof|loadLib|loaded|luapath|new|newInstance|package|math|abs|acos|asin|atan|atan2|ceil|cos|cosh|deg|exp|floor|fmod|frexp|huge|ldexp|log|log10|max|maxinteger|min|mininteger|modf|pi|pow|rad|random|randomseed|sin|sinh|sqrt|tan|tanh|tointeger|type|ult|module|next|os|clock|date|difftime|execute|exit|getenv|remove|rename|setlocale|time|tmpname|package|config|cpath|loaded|loaders|loadlib|path|preload|searchers|searchpath|seeall|pairs|pcall|print|rawequal|rawget|rawlen|rawset|require|select|setfenv|setmetatable|string|byte|char|dump|find|format|gfind|gmatch|gsub|len|lower|match|pack|packsize|rep|reverse|sub|unpack|upper|table|concat|foreach|foreachi|insert|maxn|move|pack|remove|sort|unpack|tonumber|tostring|type|unpack|char|charpattern|utf8|codepoint|codes|len|offset|xpcall";
        //private final static String functionTarget   = "_ENV|_G|_VERSION|assert|collectgarbage|coroutine.create|coroutine.isyieldable|coroutine.resume|coroutine.running|coroutine.status|coroutine.wrap|coroutine.yield|debug.debug|debug.gethook|debug.getinfo|debug.getlocal|debug.getmetatable|debug.getregistry|debug.getupvalue|debug.getuservalue|debug.sethook|debug.setlocal|debug.setmetatable|debug.setupvalue|debug.setuservalue|debug.traceback|debug.upvalueid|debug.upvaluejoin|dofile|error|getfenv|getmetatable|io.close|io.flush|io.input|io.lines|io.open|io.output|io.popen|io.read|io.stderr|io.stdin|io.stdout|io.tmpfile|io.type|io.write|ipairs|load|loadfile|loadstring|luajava.bindClass|luajava.clear|luajava.coding|luajava.createArray|luajava.createProxy|luajava.instanceof|luajava.loadLib|luajava.loaded|luajava.luapath|luajava.new|luajava.newInstance|luajava.package|math.abs|math.acos|math.asin|math.atan|math.atan2|math.ceil|math.cos|math.cosh|math.deg|math.exp|math.floor|math.fmod|math.frexp|math.huge|math.ldexp|math.log|math.log10|math.max|math.maxinteger|math.min|math.mininteger|math.modf|math.pi|math.pow|math.rad|math.random|math.randomseed|math.sin|math.sinh|math.sqrt|math.tan|math.tanh|math.tointeger|math.type|math.ult|module|next|os.clock|os.date|os.difftime|os.execute|os.exit|os.getenv|os.remove|os.rename|os.setlocale|os.time|os.tmpname|package.config|package.cpath|package.loaded|package.loaders|package.loadlib|package.path|package.preload|package.searchers|package.searchpath|package.seeall|pairs|pcall|print|rawequal|rawget|rawlen|rawset|require|select|setfenv|setmetatable|string.byte|string.char|string.dump|string.find|string.format|string.gfind|string.gmatch|string.gsub|string.len|string.lower|string.match|string.pack|string.packsize|string.rep|string.reverse|string.sub|string.unpack|string.upper|table.concat|table.foreach|table.foreachi|table.insert|table.maxn|table.move|table.pack|table.remove|table.sort|table.unpack|tonumber|tostring|type|unpack|utf8.char|utf8.charpattern|utf8.codepoint|utf8.codes|utf8.len|utf8.offset|xpcall";
        private val globalTarget = "__add|__band|__bnot|__bor|__bxor|__call|__concat|__div|__eq|__idiv|__index|__le|__len|__lt|__mod|__mul|__newindex|__pow|__shl|__shr|__sub|__unm|_ENV|_G|assert|collectgarbage|dofile|error|findtable|getmetatable|ipairs|load|loadfile|loadstring|module|next|pairs|pcall|print|rawequal|rawget|rawlen|rawset|require|select|self|setmetatable|tointeger|tonumber|tostring|type|unpack|xpcall"
        private val packageName = "coroutine|debug|io|luajava|math|os|package|string|table|utf8"
        private val package_coroutine = "create|isyieldable|resume|running|status|wrap|yield"
        private val package_debug = "debug|gethook|getinfo|getlocal|getmetatable|getregistry|getupvalue|getuservalue|sethook|setlocal|setmetatable|setupvalue|setuservalue|traceback|upvalueid|upvaluejoin"
        private val package_io = "close|flush|input|lines|open|output|popen|read|stderr|stdin|stdout|tmpfile|type|write"
        private val package_luajava = "astable|bindClass|clear|coding|createArray|createProxy|instanceof|loadLib|loaded|luapath|new|newInstance|package|tostring"
        private val package_math = "abs|acos|asin|atan|atan2|ceil|cos|cosh|deg|exp|floor|fmod|frexp|huge|ldexp|log|log10|max|maxinteger|min|mininteger|modf|pi|pow|rad|random|randomseed|sin|sinh|sqrt|tan|tanh|tointeger|type|ult"
        private val package_os = "clock|date|difftime|execute|exit|getenv|remove|rename|setlocale|time|tmpname"
        private val package_package = "config|cpath|loaded|loaders|loadlib|path|preload|searchers|searchpath|seeall"
        private val package_string = "byte|char|dump|find|format|gfind|gmatch|gsub|len|lower|match|pack|packsize|rep|reverse|sub|unpack|upper"
        private val package_table = "concat|foreach|foreachi|insert|maxn|move|pack|remove|sort|unpack"
        private val package_utf8 = "char|charpattern|codepoint|codes|len|offset"
        private val extFunctionTarget = "call|compile|dump|each|enum|import|set|task|thread|timer"


        private val functionTarget = (globalTarget + "|" + packageName + "|"
                + extFunctionTarget)
        private val keywordsss = keywordTarget.split("\\|".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()

        private val namess = functionTarget.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        private val LUA_OPERATORS = charArrayOf('(', ')', '{', '}', ',', ';',
                '=', '+', '-', '/', '*', '&', '!', '|', ':', '[', ']', '<', '>', '?',
                '~', '%', '^')
        private var _theOne: Language? = null

        val instance: Language
            get() {
                if (_theOne == null) {
                    _theOne = LanguageLua()
                }
                return _theOne!!
            }
    }

}
