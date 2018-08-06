package cn.vove7.androlua.luabridge

import cn.vove7.androlua.LuaApp
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaRunnableI
import cn.vove7.androlua.luautils.TimerTaskX
import com.luajava.LuaException
import com.luajava.LuaObject
import com.luajava.LuaState

/**
 * LuaTask
 */
class LuaTimerTask : TimerTaskX, LuaRunnableI {
    private var L: LuaState
    private var luaManager: LuaManagerI

    private var mSrc: String? = null
    private var funHelper: LuaFunHelper
    private var luaHelper: LuaHelper


    private var mArg = arrayOf<Any>(0)

    private var mEnabled = true

    private var mBuffer: ByteArray? = null

    val name: String
        get() = "TimerTask " + hashCode() + " "

    @JvmOverloads constructor(luaManager: LuaManagerI, src: String, arg: Array<Any>? = null) {
        this.luaManager = luaManager
        mSrc = src
        if (arg != null)
            mArg = arg

        luaHelper = LuaHelper(LuaApp.instance)
        L = luaHelper.L
        funHelper = LuaFunHelper(luaHelper, L)
    }

    override fun quit() {
        L.gc(LuaState.LUA_GCCOLLECT, 1)
        System.gc()
        if (isCancelled) {
            luaManager.handleMessage(LuaManagerI.W, "$name already canceled")
            return
        }
        luaManager.log("$name quit")
        cancel()
    }

    @Throws(LuaException::class)
    @JvmOverloads constructor(luaManager: LuaManagerI, func: LuaObject, arg: Array<Any>? = null) {
        this.luaManager = luaManager
        if (arg != null)
            mArg = arg
        mBuffer = func.dump()
        luaHelper = LuaHelper(LuaApp.instance)
        L = luaHelper.L
        funHelper = LuaFunHelper(luaHelper, L)
    }

    override fun run() {
        if (!mEnabled) {
            luaManager.log(name + "Enabled is false")
            return
        }
        L.getGlobal("run")
        try {
            if (!L.isNil(-1))
                funHelper.runFunc("run")
            else {
                if (mBuffer != null)
                    funHelper.newLuaThread(mBuffer, *mArg)
                else
                    funHelper.newLuaThread(mSrc, *mArg)
            }
        } catch (e: LuaException) {
            luaManager.handleMessage(LuaManagerI.E, e.message ?: "")
            e.printStackTrace()
            quit()
        }

    }

    fun setArg(arg: Array<Any>) {
        mArg = arg
    }

    @Throws(ArrayIndexOutOfBoundsException::class, LuaException::class, IllegalArgumentException::class)
    fun setArg(arg: LuaObject) {
        mArg = arg.asArray()
    }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    @Throws(LuaException::class)
    operator fun set(key: String, value: Any) {
        L.pushObjectValue(value)
        L.setGlobal(key)
    }

    @Throws(LuaException::class)
    operator fun get(key: String): Any? {
        L.getGlobal(key)
        return L.toJavaObject(-1)
    }
}
