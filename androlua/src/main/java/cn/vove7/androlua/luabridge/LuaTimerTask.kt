package cn.vove7.androlua.luabridge

import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaRunnableI
import cn.vove7.androlua.luautils.TimerTaskX
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.executor.OnPrint
import cn.vove7.vtp.log.Vog
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

    @JvmOverloads constructor(luaManager: LuaManagerI, src: String, arg: Array<Any>? = null)
            : this(luaManager, arg) {
        mSrc = src
        if (arg != null)
            mArg = arg
    }

    @Throws(LuaException::class)
    @JvmOverloads constructor(luaManager: LuaManagerI, func: LuaObject, arg: Array<Any>? = null)
            : this(luaManager, arg) {
        mBuffer = func.dump()
    }

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, arg: Array<Any>?) {
        if (arg != null)
            mArg = arg
        this.luaManager = luaManager
        luaHelper = LuaHelper(GlobalApp.APP)
        L = luaHelper.L
        funHelper = LuaFunHelper(luaHelper, L)

        funHelper.copyRuntimeFrom(luaManager.luaState)
    }

    override fun quit() {
        quit(false)
    }

    override fun quit(self: Boolean) {
        Vog.d("quit $this $self")
        L.gc(LuaState.LUA_GCCOLLECT, 1)
//        L.close()
        System.gc()
        if (isCancelled) {
            luaManager.handleMessage(OnPrint.WARN, "$name already canceled")
            return
        }
        cancel()
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
            luaManager.handleMessage(OnPrint.ERROR, e.message ?: "")
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
