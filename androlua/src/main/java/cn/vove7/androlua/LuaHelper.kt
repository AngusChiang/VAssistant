package cn.vove7.androlua

import android.content.Context
import cn.vove7.androlua.luabridge.LuaThread
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.androlua.luabridge.LuaUtil.errorReason
import cn.vove7.androlua.luautils.LuaDexLoader
import cn.vove7.androlua.luautils.LuaGcable
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaPrinter
import cn.vove7.common.BridgeManager
import cn.vove7.common.MessageException
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.executor.OnPrint
import cn.vove7.common.interfaces.ScriptEngine
import cn.vove7.vtp.log.Vog
import com.luajava.*
import com.luajava.LuaState.LUA_GCSTOP
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
        bridgeManager = sBridgeManager
        initPath()
        init()
//        Vog.d("constructor $sBridgeManager")
    }

    override var bridgeManager: BridgeManager?


    constructor(context: Context, b: BridgeManager) : this(context) {
        bridgeManager = b
        sBridgeManager = b
//        Vog.d("constructor2 $sBridgeManager")
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
        private var sBridgeManager: BridgeManager? = null

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

//        val assetPath = "file://android_assset"
//        val assetInclude = "$assetPath/?.lua;$assetPath/?/init.lua;"
        luaRequireSearchPath = "$luaDir/?.lua;$luaDir/lua/?.lua;$luaDir/?/init.lua;"
        luaRequireSearchPath += jniLibsPath ?: "" /*+ assetInclude*/
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

        val set = object : JavaFunction(L) {
            override fun execute(): Int {
                val thread = L.toJavaObject(2) as LuaThread
                thread[L.toString(3)] = L.toJavaObject(4)
                return 0
            }
        }

        val call = object : JavaFunction(L) {
            override fun execute(): Int {
                val thread = L.toJavaObject(2) as LuaThread

                val top = L.top
                if (top > 3) {
                    val args = arrayOf<Any>(top - 3)
                    for (i in 4..top) {
                        args[i - 4] = L.toJavaObject(i)
                    }
                    thread.call(L.toString(3), args)
                } else if (top == 3) {
                    thread.call(L.toString(3))
                }
                return 0
            }
        }
        try {
            set.register("set")
            call.register("call")
        } catch (e: LuaException) {
            e.printStackTrace()
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

    fun loadString(s: String): Int {
        L.top = 0
        return L.LloadString(s)
    }

    fun loadFile(fileName: String): Int {
        L.top = 0
        return L.LloadFile(fileName)
    }

    override fun evalString(script: String, args: Array<*>?) {
        val r = loadString(script)
        if (r == 0) {
            setArgs(args)
            exec(args?.size ?: 0)
        } else
            checkErr(r)
    }


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
            throw MessageException(checkErr(ok))
        }
    }

    fun checkErr(r: Int): String {
        val e = errorReason(r) + ": " + L.toString(-1)

        if (e.contains("java.lang.UnsupportedOperationException") ||
                e.contains("java.lang.InterruptedException")) {
            handleMessage(OnPrint.WARN, "强制终止\n")
            return "强制终止"
        } else handleError(e)
        return e
    }

    override fun handleError(err: String) {
        GlobalLog.err(err)
        handleMessage(OnPrint.ERROR, err)
    }

    override fun handleError(e: Throwable) {
        GlobalLog.err(e)
    }


    fun autoRun(s: String, args: Array<Any>) {
        Vog.d("autoRun  ----> $s")
        when {
            Pattern.matches("^\\w+$", s) -> execFromAsset("$s.lua", args)
            Pattern.matches("^[\\w._/]+$", s) -> evalFile(s, args)
            else -> evalString(s, args)
        }
    }

    override fun evalFile(filePath: String, args: Array<*>?) {
        var r = 1
        if (loadFile(filePath).also { r = it } == 0) {
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
        gcAll()
        L.gc(LUA_GCSTOP, 0)
        L.close()
        init()
    }

    fun loadResources(path: String) {
        mLuaDexLoader!!.loadResources(path)
    }

    override val app: Context
        get() = GlobalApp.APP

}
