package cn.vove7.androlua

import android.content.Context
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.androlua.luabridge.LuaUtil.errorReason
import cn.vove7.androlua.luautils.LuaDexLoader
import cn.vove7.androlua.luautils.LuaGcable
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaPrinter
import cn.vove7.common.ScriptEnginesBridges
import cn.vove7.common.MessageException
import cn.vove7.common.NotSupportException
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.executor.OnPrint
import cn.vove7.common.interfaces.ScriptEngine
import cn.vove7.vtp.log.Vog
import com.luajava.LuaException
import com.luajava.LuaState
import com.luajava.LuaState.LUA_GCSTOP
import com.luajava.LuaStateFactory
import dalvik.system.DexClassLoader
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import java.util.regex.Pattern
import kotlin.collections.HashSet

/**
 * LuaHelper
 *
 *
 * Created by Vove on 2018/7/30
 */
class LuaHelper : LuaManagerI, ScriptEngine {
    private val context: Context

    constructor(context: Context) {
        this.context = context
        bridgeManager = sScriptEnginesBridges
        initPath()
        init()
    }

    override var bridgeManager: ScriptEnginesBridges?


    constructor(context: Context, b: ScriptEnginesBridges) {
        this.context = context
        sScriptEnginesBridges = b
        bridgeManager = b
        initPath()
        init()
    }

    lateinit var L: LuaState
    override val luaState: LuaState
        get() = L

    private val gcList = ConcurrentSkipListSet<LuaGcable>()
    private var luaRequireSearchPath: String? = null
    private var mLuaDexLoader: LuaDexLoader? = null


    /**
     * 注册打印
     */
    companion object {
        private var libDir: String? = null
        private var jniLibsPath: String? = null
        private var luaDir: String? = null
        private val printList = HashSet<OnPrint>()
        private var sScriptEnginesBridges: ScriptEnginesBridges? = null

        fun regPrint(print: OnPrint) {
            synchronized(printList) {
                printList.add(print)
            }
        }

        fun unRegPrint(print: OnPrint) {
            synchronized(printList) {
                printList.remove(print)
            }
        }
    }

    private fun init() {
        synchronized(gcList) {
            gcList.clear()
        }
        mLuaDexLoader = LuaDexLoader(context)
        try {
            mLuaDexLoader!!.loadLibs()
        } catch (e: LuaException) {
            e.printStackTrace()
        }
        initLua()
    }

    private fun initPath() {
        luaDir = context.filesDir.absolutePath
        Vog.d("initLua luaDir ----> " + luaDir!!)
        libDir = context.getDir("lib", Context.MODE_PRIVATE).absolutePath
        jniLibsPath = context.applicationInfo.nativeLibraryDir + "/lib?.so" + ";" + libDir + "/lib?.so;"

        luaRequireSearchPath = "$luaDir/?.lua;$luaDir/lua/?.lua;$luaDir/?/init.lua;"
        luaRequireSearchPath += (jniLibsPath ?: ";")
    }

    override fun log(log: Any?) {
        GlobalLog.log(log.toString())
    }

    private fun initLua() {
        L = LuaStateFactory.newLuaState()
        L.openLibs()
        L.pushJavaObject(this)

        L.setGlobal("luaman")
        L.getGlobal("luaman")

        L.pushContext(context)
        //L.setGlobal("app");

//        L.getGlobal("luajava")

        //L.pushString(luaDir);
        //L.setField(-2, "luaextdir");
        L.pushString(luaDir)
        L.setField(-2, "luaDir")
        //L.pushString(luaDir);
        //L.setField(-2, "luapath");
        L.pop(1)

        L.getGlobal("package")
        L.pushString(luaRequireSearchPath)
        L.setField(-2, "path")
        L.pushString(jniLibsPath)
        L.setField(-2, "cpath")
        L.pop(1)

        LuaPrinter(L, object : OnPrint {
            override fun onPrint(l: Int, output: String?) {
                notifyOutput(l, output)
            }
        })

        //从assert加载api
        requireFromAsset(
                "lua_requires/import.lua",
                "lua_requires/bridges.lua",
                "lua_requires/utils.lua",
                "lua_requires/executor.lua",
                "lua_requires/view_op.lua",
                "lua_requires/global_op.lua",
                "lua_requires/storages.lua",
                "lua_requires/json.lua",
                "lua_requires/settings.lua"
        )
    }

    private fun requireFromAsset(vararg files: String) {
        files.forEach {
            execFromAsset(it, emptyArray())
        }
    }

    override fun handleMessage(l: Int, msg: String) {
        notifyOutput(l, msg)
    }


    private fun notifyOutput(l: Int, o: String?) {
        synchronized(printList) {
            for (p in printList) {
                p.onPrint(l, o)
            }
        }
    }

    private fun loadString(s: String): Int {
        L.top = 0
        return L.LloadString(s)
    }

    private fun loadFile(fileName: String): Int {
        L.top = 0
        return L.LloadFile(fileName)
    }

    @Throws(Exception::class)
    override fun evalString(script: String, args: Array<*>?) {
        val r = loadString(script)
        if (r == 0) {
            setArgs(args)
            exec(args?.size ?: 0)
        } else
            throw checkErr(r)
    }


    @Throws(Exception::class)
    override fun evalString(script: String, argMap: Map<String, *>?) {
        evalString(script, arrayOf(argMap))
    }

    override fun evalFile(file: String, argMap: Map<String, *>?) {
        evalFile(file, arrayOf(argMap))
    }

    private fun setArgs(args: Array<*>?) {
        L.getGlobal("debug")
        L.getField(-1, "traceback")
        L.remove(-2)
        L.insert(-2)

        args ?: return
        for (arg in args) {
            L.pushObjectValue(arg)
        }
    }

    private fun exec(argSize: Int) {
        val ok = L.pcall(argSize, 0, -2 - argSize)
        if (ok == 0) {
            return
        } else {//载入参数错误
            throw checkErr(ok)
        }
    }

    fun checkErr(r: Int): Throwable {
        val e = errorReason(r) + ": " + L.toString(-1)

        if (e.contains("java.lang.UnsupportedOperationException") ||
                e.contains("java.lang.InterruptedException")) {
            return InterruptedException(e)
        } else if (e.contains("cn.vove7.common.NotSupportException")) {
            return NotSupportException()
        }
        return MessageException(e)
    }

    override fun handleError(err: String) {
        GlobalLog.err(err)
        handleMessage(OnPrint.ERROR, err)
    }

    override fun handleError(e: Throwable) {
        GlobalLog.err(e)
        handleMessage(OnPrint.ERROR, e.message ?: "")
    }


    fun autoRun(s: String, args: Array<Any>) {
        Vog.d("autoRun  ----> $s")
        when {
            Pattern.matches("^\\w+$", s) -> execFromAsset("$s.lua", args)
            Pattern.matches("^[\\w._/]+$", s) -> evalFile(s, args)
            else -> evalString(s, args)
        }
    }

    override fun evalFile(file: String, args: Array<*>?) {
        var r = 1
        if (loadFile(file).also { r = it } == 0) {
            setArgs(args)
            exec(args?.size ?: 0)
        } else
            checkErr(r)
    }

    fun execFromAsset(name: String, args: Array<Any>? = null) {
        val bytes: ByteArray
        try {
            bytes = LuaUtil.readAsset(context, name)
        } catch (e: IOException) {
            e.printStackTrace()
            handleError(e)
            return
        }
        L.top = 0
        val ok = L.LloadBuffer(bytes, name)
        if (ok == 0) {
            setArgs(args)
            exec(args?.size ?: 0)
        } else checkErr(ok)
    }

    override fun regGc(obj: LuaGcable) {
        Vog.d("regGc $obj")
        synchronized(gcList) {
            gcList.add(obj)
        }
    }

    override val librarys: HashMap<String, String>
        get() = mLuaDexLoader!!.librarys


    override val classLoaders: ArrayList<ClassLoader>
        get() = mLuaDexLoader!!.classLoaders


    override fun loadDex(path: String): DexClassLoader? {
        var r: DexClassLoader? = null
        try {
            r = mLuaDexLoader!!.loadDex(path)
        } catch (e: Exception) {
            e.printStackTrace()
            handleError(e)
        }
        return r
    }

    override fun gc(obj: LuaGcable) {
        synchronized(gcList) {
            obj.gc()
            gcList.remove(obj)
        }
    }

    override fun removeGc(obj: LuaGcable): Boolean {
        synchronized(gcList) {
            return gcList.remove(obj).also {
                Vog.d("removeGc $obj $it")
            }
        }
    }

    private fun gcAll() {
        //清空线程..
        synchronized(gcList) {
            Vog.d("gcAll ${gcList.size}")
            for (gcable in gcList) {
                gcable.gc()
            }
            gcList.clear()
        }
    }

    override fun stop() {
        Vog.d("stop by external")
        release()
    }

    override fun release() {
        gcAll()
        L.gc(LUA_GCSTOP, 0)
        L.close()
    }

    fun loadResources(path: String) {
        mLuaDexLoader!!.loadResources(path)
    }

    override val app: Context
        get() = GlobalApp.APP

}
