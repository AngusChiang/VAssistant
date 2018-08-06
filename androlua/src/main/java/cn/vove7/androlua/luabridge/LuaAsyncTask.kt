package cn.vove7.androlua.luabridge

import android.os.AsyncTask
import cn.vove7.androlua.LuaApp
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luabridge.LuaUtil.errorReason
import cn.vove7.androlua.luautils.LuaGcable
import cn.vove7.androlua.luautils.LuaManagerI
import com.luajava.JavaFunction
import com.luajava.LuaException
import com.luajava.LuaObject
import com.luajava.LuaState

class LuaAsyncTask : AsyncTask<Any, Any, Any>, LuaGcable {

    private var loadeds: Array<Any>? = null
    private lateinit var L: LuaState

    private var luaManager: LuaManagerI
    private var mBuffer: ByteArray? = null
    private var mDelay: Long = 0
    private var mCallback: LuaObject? = null
    private var mUpdate: LuaObject? = null

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, delay: Long, callback: LuaObject) {
        luaManager.regGc(this)
        this.luaManager = luaManager
        mDelay = delay
        mCallback = callback
    }

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, src: String, callback: LuaObject) {
        luaManager.regGc(this)
        this.luaManager = luaManager
        mBuffer = src.toByteArray()
        mCallback = callback
    }

    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, func: LuaObject, callback: LuaObject) {
        luaManager.regGc(this)
        this.luaManager = luaManager
        mBuffer = func.dump()
        mCallback = callback
        val l = func.luaState
        val g = l.getLuaObject("luajava")
        val loaded = g.getField("imported")
        if (!loaded.isNil) {
            loadeds = loaded.asArray()
        }
    }


    @Throws(LuaException::class)
    constructor(luaManager: LuaManagerI, func: LuaObject, update: LuaObject, callback: LuaObject) {
        this.luaManager = luaManager
        luaManager.regGc(this)
        mBuffer = func.dump()
        mUpdate = update
        mCallback = callback
    }

    override fun gc() {
        if (status == AsyncTask.Status.RUNNING)
            cancel(true)
    }

    @Throws(IllegalArgumentException::class, ArrayIndexOutOfBoundsException::class, LuaException::class)
    fun execute() {
        super.execute()
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

    protected override fun doInBackground(args: Array<Any>): Any? {
        if (mDelay != 0L) {
            try {
                Thread.sleep(mDelay)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                luaManager.handleError(e)
            }
            return args
        }
        L = LuaHelper(LuaApp.instance).L

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
            luaManager.log("AsyncTask" + e.message)
        }

        if (loadeds != null) {
            val require = L.getLuaObject("require")
            try {
                require.call("import")
                val _import = L.getLuaObject("import")
                for (s in loadeds!!)
                    _import.call(s.toString())
            } catch (e: LuaException) {
                e.printStackTrace()
            }

        }

        try {
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
                }
            }
            throw LuaException(errorReason(ok) + ": " + L.toString(-1))
        } catch (e: LuaException) {
            e.printStackTrace()
            luaManager.handleMessage(LuaManagerI.E, "doInBackground$e")
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
            luaManager.handleMessage(LuaManagerI.E, "onPostExecute" + e.toString())
        }

        super.onPostExecute(result)
        L.gc(LuaState.LUA_GCCOLLECT, 1)
        System.gc()
    }

    protected override fun onProgressUpdate(values: Array<Any>) {
        try {
            mUpdate?.call(*values)
        } catch (e: LuaException) {
            luaManager.handleMessage(LuaManagerI.E, "onProgressUpdate" + e.message)
        }
        super.onProgressUpdate(*values)
    }

}

