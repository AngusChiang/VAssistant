package cn.vove7.androlua.luabridge

import android.os.AsyncTask
import cn.vove7.androlua.LuaApp
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luautils.LuaGcable
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaRunnableI
import cn.vove7.common.executor.OnPrint
import cn.vove7.vtp.log.Vog
import com.luajava.JavaFunction
import com.luajava.LuaException
import com.luajava.LuaObject
import com.luajava.LuaState


class LuaAsyncTask : AsyncTask<Any, Any, Any>, LuaGcable, LuaRunnableI, Comparable<LuaAsyncTask> {
    override fun compareTo(other: LuaAsyncTask): Int = hashCode() - other.hashCode()
    private var L: LuaState

    private var luaManager: LuaManagerI
    private var mBuffer: ByteArray? = null
    private var mDelay: Long = 0
    private var mCallback: LuaObject? = null
    private var mUpdate: LuaObject? = null

    private var funHelper: LuaFunHelper
    private var luaHelper: LuaHelper

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, delay: Long, callback: LuaObject) : this(luaManager, callback) {
        mDelay = delay
    }

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, src: String, callback: LuaObject) : this(luaManager, callback) {
        mBuffer = src.toByteArray()
    }

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, func: LuaObject, callback: LuaObject) : this(luaManager, callback) {
        mBuffer = func.dump()
    }


    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, func: LuaObject, update: LuaObject,
                callback: LuaObject) : this(luaManager, callback) {
        mBuffer = func.dump()
        mUpdate = update
    }

    constructor(luaManager: LuaManagerI, callback: LuaObject) {
        this.luaManager = luaManager
        luaManager.regGc(this)
        mCallback = callback

        luaHelper = LuaHelper(LuaApp.instance)
        L = luaHelper.L
        funHelper = LuaFunHelper(luaHelper, L)

        funHelper.copyRuntimeFrom(luaManager.luaState)
    }

    override fun quit() {
        quit(false)
    }

    override fun quit(self: Boolean) {
        Vog.d(this, "quit $this $self")
        luaManager.removeGc(this)
        L.gc(LuaState.LUA_GCCOLLECT, 1)
        System.gc()
//        L.close()
        if (status == AsyncTask.Status.RUNNING) {
            Vog.d(this, "cancel")
            cancel(true)
        }
    }

    override fun gc() {
        quit(false)
    }

    @Throws(IllegalArgumentException::class, ArrayIndexOutOfBoundsException::class, LuaException::class)
    fun exec() {
        super.execute()
    }

    fun cancel() {
        quit()
        cancel(true)
    }

    fun exec(args: Array<Any>) {
        Vog.d(this, "exec $args")
        super.execute(*args)
    }


    fun update(msg: Any?) {
        publishProgress(msg)
    }

    fun update(msg: String) {
        publishProgress(msg)
    }

    fun update(msg: Int) {
        publishProgress(msg)
    }

    override fun doInBackground(args: Array<Any>): Any? {
        if (mDelay != 0L) {
            try {
                Thread.sleep(mDelay)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                luaManager.handleError(e)
                quit()
                return null
            }
            return args
        }

        try {
            val update = object : JavaFunction(L) {
                @Throws(LuaException::class)
                override fun execute(): Int {
                    update(L.toJavaObject(2))
                    return 0
                }
            }
            update.register("update")
        } catch (e: LuaException) {
            e.printStackTrace()
            luaManager.handleError(e)
        }
        L.top = 0
        var ok = L.LloadBuffer(mBuffer, "LuaAsyncTask")

        if (ok == 0) {
            L.getGlobal("debug")
            L.getField(-1, "traceback")
            L.remove(-2)
            L.insert(-2)
            val l = args.size
            for (arg in args) {
                L.pushObjectValue(arg)
            }
            ok = L.pcall(l, LuaState.LUA_MULTRET, -2 - l)
            if (ok == 0) {
                val n = L.top - 1
                val ret = arrayOfNulls<Any>(n)
                for (i in 0 until n)
                    ret[i] = L.toJavaObject(i + 2)
                return ret
            } else luaHelper.checkErr(ok)
        }
        return null
    }

    override fun onPostExecute(result: Any) {
        if (isCancelled)
            return
        try {
            if (mCallback != null)
                mCallback!!.call(*result as Array<Any>)
        } catch (e: LuaException) {
            luaManager.handleMessage(OnPrint.ERROR, "onPostExecute" + e.toString())
        }
        super.onPostExecute(result)
        quit(true)
    }

    protected override fun onProgressUpdate(values: Array<Any>) {
        try {
            mUpdate?.call(*values)
        } catch (e: LuaException) {
            e.printStackTrace()
            luaManager.handleMessage(OnPrint.ERROR, "onProgressUpdate" + e.message)
        }
//        super.onProgressUpdate(*values)
    }

}

