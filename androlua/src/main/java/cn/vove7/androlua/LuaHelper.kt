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
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.executor.OnPrint
import cn.vove7.vtp.log.Vog
import com.luajava.JavaFunction
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
class LuaHelper : LuaManagerI {
    private val context: Context

    constructor(context: Context) {
        this.context = context
        bridgeManager = sBridgeManager
        initPath()
        init()
//        Vog.d(this, "constructor $sBridgeManager")
    }

    override var bridgeManager: BridgeManager?


    constructor(context: Context, b: BridgeManager) : this(context) {
        bridgeManager = b
        sBridgeManager = b
//        Vog.d(this, "constructor2 $sBridgeManager")
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
        Vog.d(this, "initLua luaDir ----> " + luaDir!!)
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

//        L.getGlobal("require")
//        L.pushString("bridges")
//        L.call(1, 0)
        LuaPrinter(L, object : OnPrint {

            override fun onPrint(l: Int, output: String) {
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

    internal fun safeEvalLua(src: String) {
        try {
            evalString(src)
        } catch (e: LuaException) {
            GlobalLog.err(e)
            Vog.d(this, "safeEvalLua  ----> " + e.message)
        }
    }

    private fun notifyOutput(l: Int, o: String) {
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

    fun evalString(src: String, args: Array<*>? = null) {
        var r = 1
        if (loadString(src).also { r = it } == 0) {
            if (args != null)
                loadAfterExec(args)
        } else
            checkErr(r)
    }

    private fun loadAfterExec(args: Array<*>) {
        L.getGlobal("debug")
        L.getField(-1, "traceback")
        L.remove(-2)
        L.insert(-2)
        val l = args.size
        for (arg in args) {
            L.pushObjectValue(arg)
        }
        val ok = L.pcall(l, 0, -2 - l)
        if (ok == 0) {
            return
        } else {
            throw Exception(checkErr(ok))
        }
    }

    fun checkErr(r: Int): String {
        var e = errorReason(r) + ": " + L.toString(-1)
//        var end = e.indexOf("stack traceback:")//隐藏stack traceback
//        if (end == -1) end = e.length
//        Vog.e(this, "checkErr ---> $e")
//        e = e.substring(0, end)
        if (e.contains("java.lang.UnsupportedOperationException") ||
                e.contains("java.lang.InterruptedException"))
            handleMessage(OnPrint.WARN, "强制终止\n")
        else
            handleError(e)
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
        Vog.d(this, "autoRun  ----> $s")
        when {
            Pattern.matches("^\\w+$", s) -> execFromAsset("$s.lua", args)
            Pattern.matches("^[\\w._/]+$", s) -> execFromFile(s, args)
            else -> evalString(s, args)
        }
    }

    fun execFromFile(filePath: String, args: Array<Any>) {
        var r = 1
        if (loadFile(filePath).also { r = it } == 0) {
            loadAfterExec(args)
        } else
            checkErr(r)
    }

    fun execFromAsset(name: String, args: Array<Any>) {
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
            loadAfterExec(args)
        } else checkErr(ok)
    }

    override fun regGc(obj: LuaGcable) {
        Vog.d(this, "regGc $obj")
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
                Vog.d(this, "removeGc $obj $it")
            }
        }
    }

    private fun gcAll() {
        //清空线程..
        synchronized(gcList) {
            Vog.d(this, "gcAll ${gcList.size}")
            for (gcable in gcList) {
                gcable.gc()
            }
            gcList.clear()
        }
    }

    override fun stop() {
        Vog.d(this, "stop by external")
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
