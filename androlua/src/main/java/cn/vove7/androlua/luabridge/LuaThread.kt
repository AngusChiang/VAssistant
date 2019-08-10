package cn.vove7.androlua.luabridge

import android.os.Looper
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luautils.LuaGcable
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaRunnableI
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.executor.OnPrint
import cn.vove7.vtp.log.Vog
import com.luajava.LuaException
import com.luajava.LuaObject
import com.luajava.LuaState

/**
 * 线程
 */
class LuaThread : Thread, LuaGcable, LuaRunnableI, Comparable<LuaThread> {

    override operator fun compareTo(other: LuaThread): Int {
        return this.hashCode() - other.hashCode()
    }

    var isRun = false
    var L: LuaState
    private var luaManager: LuaManagerI
    private var mIsLoop: Boolean = false
    private var mSrc: String? = null
    private var mArg = arrayOf<Any>()
    private var mBuffer: ByteArray? = null
    private var funHelper: LuaFunHelper
    private var luaHelper: LuaHelper

    constructor(luaManager: LuaManagerI, src: String, arg: Array<Any>)
            : this(luaManager, src, false, arg)

    @JvmOverloads
    constructor(luaManager: LuaManagerI, src: String, isLoop: Boolean = false,
                arg: Array<Any>? = null) : this(luaManager) {
        mSrc = src
        mIsLoop = isLoop
        if (arg != null)
            mArg = arg
    }

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, func: LuaObject, arg: Array<Any>)
            : this(luaManager, func, false, arg)

    @Throws(LuaException::class)
    @JvmOverloads
    constructor(luaManager: LuaManagerI, func: LuaObject, isLoop: Boolean = false,
                arg: Array<Any>? = null) : this(luaManager) {
        if (arg != null)
            mArg = arg
        mIsLoop = isLoop
        mBuffer = func.dump()
    }

    private constructor(luaManager: LuaManagerI) {
        this.luaManager = luaManager
        isDaemon = true
        luaManager.regGc(this)
        luaHelper = LuaHelper(GlobalApp.APP)
        L = luaHelper.L
        funHelper = LuaFunHelper(luaHelper, L)

        funHelper.copyRuntimeFrom(luaManager.luaState)
    }

    override fun quit() {
        quit(true)
    }

    override fun gc() {
        quit(false)
    }

    override fun run() {
        try {
            if (mBuffer != null)
                funHelper.newLuaThread(mBuffer, *mArg)
            else
                funHelper.newLuaThread(mSrc, *mArg)
            if (mIsLoop) {
                Looper.prepare()
                isRun = true
                L.getGlobal("run")
                if (!L.isNil(-1)) {
                    L.pop(1)
                    funHelper.runFunc("run")
                }
//                Looper.loop()
            }
        } catch (e: LuaException) {
            luaManager.handleMessage(OnPrint.ERROR, e.message ?: "")
            e.printStackTrace()
        } finally {
            isRun = false
            if (!isInterrupted) {
                quit(true)
            }
        }

    }

    @Throws(LuaException::class)
    operator fun get(key: String): Any? {
        L.getGlobal(key)
        return L.toJavaObject(-1)
    }

    override fun quit(self: Boolean) {
        synchronized(this) {
            Vog.d("quit $this $self")
            interrupt()
            L.gc(LuaState.LUA_GCCOLLECT, 1)
//            L.close()
            if (luaManager.removeGc(this).not()) return
            System.gc()
            if (isRun) {
                isRun = false
            }
        }
    }
}


