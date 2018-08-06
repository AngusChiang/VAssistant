package cn.vove7.androlua.luabridge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import cn.vove7.androlua.LuaApp
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luautils.LuaGcable
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaRunnableI
import com.luajava.LuaException
import com.luajava.LuaMetaTable
import com.luajava.LuaObject
import com.luajava.LuaState

/**
 * 线程
 */
class LuaThread : Thread, LuaMetaTable, LuaGcable, LuaRunnableI {

    var isRun = false
    var L: LuaState? = null
    private var tHandler: Handler? = null
    private var luaManager: LuaManagerI
    private var mIsLoop: Boolean = false
    private var mSrc: String? = null
    private var mArg = arrayOf<Any>()
    private var mBuffer: ByteArray? = null
    private var funHelper: LuaFunHelper
    private var luaHelper: LuaHelper

    constructor(luaManager: LuaManagerI, src: String, arg: Array<Any>) : this(luaManager, src, false, arg) {}

    @JvmOverloads constructor(luaManager: LuaManagerI, src: String, isLoop: Boolean = false, arg: Array<Any>? = null) {
        luaManager.regGc(this)
        this.luaManager = luaManager
        mSrc = src
        mIsLoop = isLoop
        if (arg != null)
            mArg = arg
        luaHelper = LuaHelper(LuaApp.instance)
        L = luaHelper.L
        funHelper = LuaFunHelper(luaHelper, L)
    }

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, func: LuaObject, arg: Array<Any>) : this(luaManager, func, false, arg)

    @Throws(LuaException::class)
    @JvmOverloads constructor(luaManager: LuaManagerI, func: LuaObject, isLoop: Boolean = false, arg: Array<Any>? = null) {
        luaManager.regGc(this)
        this.luaManager = luaManager
        if (arg != null)
            mArg = arg
        mIsLoop = isLoop
        mBuffer = func.dump()

        luaHelper = LuaHelper(LuaApp.instance)
        L = luaHelper.L
        funHelper = LuaFunHelper(luaHelper, L)
    }

    override fun gc() {
        quit()
    }

    override fun __call(arg: Array<Any>): Any? {
        return null
    }

    override fun __index(key: String): Any {
        return object : LuaMetaTable {
            override fun __call(arg: Array<Any>): Any? {
                call(key, arg)
                return null
            }

            override fun __index(key: String): Any? {
                return null
            }

            override fun __newIndex(key: String, value: Any) {}
        }
    }

    override fun __newIndex(key: String, value: Any) {
        set(key, value)
    }

    override fun run() {
        try {
            if (mBuffer != null)
                funHelper.newLuaThread(mBuffer, *mArg)
            else
                funHelper.newLuaThread(mSrc, *mArg)
            if (mIsLoop) {
                Looper.prepare()
                tHandler = LuaFunHandler(funHelper, luaManager)
                isRun = true
                L!!.getGlobal("run")
                if (!L!!.isNil(-1)) {
                    L!!.pop(1)
                    funHelper.runFunc("run")
                }
                Looper.loop()
            }
        } catch (e: LuaException) {
            luaManager.handleMessage(LuaManagerI.E, e.message ?: "")
            e.printStackTrace()
        }

        isRun = false
        quit()
    }

    fun call(func: String) {
        push(3, func)
    }

    fun call(func: String, args: Array<Any>) {
        if (args.isEmpty())
            push(3, func)
        else
            push(1, func, args)
    }

    operator fun set(key: String, value: Any) {
        push(4, key, arrayOf(value))
    }

    @Throws(LuaException::class)
    operator fun get(key: String): Any? {
        L!!.getGlobal(key)
        return L!!.toJavaObject(-1)
    }

    override fun quit() {
        L!!.gc(LuaState.LUA_GCCOLLECT, 1)
        System.gc()
        if (isRun) {
            isRun = false
            tHandler!!.looper.quit()
        }
    }

    fun push(what: Int, s: String) {
        if (!isRun) {
            luaManager.handleMessage(LuaManagerI.L, "thread is not running")
            return
        }
        val message = Message()
        val bundle = Bundle()
        bundle.putString("data", s)
        message.data = bundle
        message.what = what

        tHandler!!.sendMessage(message)

    }

    fun push(what: Int, s: String, args: Array<Any>) {
        if (!isRun) {
            luaManager.handleMessage(LuaManagerI.L, "thread is not running")
            return
        }

        val message = Message()
        val bundle = Bundle()
        bundle.putString("data", s)
        bundle.putSerializable("args", args)
        message.data = bundle
        message.what = what

        tHandler!!.sendMessage(message)
    }


    private class LuaFunHandler(internal var funHelper: LuaFunHelper, internal var luaManager: LuaManagerI) : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val data = msg.data
            try {
                when (msg.what) {
                    0 -> funHelper.newLuaThread(data.getString("data"), *data.getSerializable("args") as Array<Any>)
                    1 -> funHelper.runFunc(data.getString("data"), *data.getSerializable("args") as Array<Any>)
                    2 -> funHelper.newLuaThread(data.getString("data"))
                    3 -> funHelper.runFunc(data.getString("data"))
                    4 -> funHelper.setField(data.getString("data"), (data.getSerializable("args") as Array<Any>)[0])
                }
            } catch (e: LuaException) {
                luaManager.handleMessage(LuaManagerI.E, e.message ?: "")
                e.printStackTrace()
            }

        }
    }
}


