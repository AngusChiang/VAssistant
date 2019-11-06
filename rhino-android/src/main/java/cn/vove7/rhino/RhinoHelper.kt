package cn.vove7.rhino

import android.os.Looper
import cn.vove7.common.ScriptEngineBridges
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.interfaces.ScriptEngine
import cn.vove7.rhino.api.RhinoApi
import cn.vove7.rhino.api.RhinoApi.quit
import cn.vove7.rhino.common.AndroidContextFactory
import cn.vove7.rhino.common.GcCollector
import cn.vove7.rhino.common.MapScriptable
import cn.vove7.rhino.common.RhinoAndroidHelper
import cn.vove7.vtp.log.Vog
import org.mozilla.javascript.*
import org.mozilla.javascript.tools.SourceReader
import org.mozilla.javascript.tools.shell.Global
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class RhinoHelper : ScriptableObject, ScriptEngine {

    private val appContext = GlobalApp.APP

    private val androidHelper = RhinoAndroidHelper(appContext)

    private val rhinoContext: Context = androidHelper.enterContext()

    private val global = Global()

    override fun getClassName(): String {
        return "RhinoHelper"
    }

    constructor() {
        init()
    }

    private val builtinVariables = "var com = Packages.com;\n" +
            "var javax = Packages.javax;\n" +
            "var net = Packages.net;\n" +
            "var cn = Packages.cn;\n" +
            "var kotlin = Packages.kotlin;\n" +
            "var android = Packages.android;\n" +
            "var org = Packages.org;\n"

    constructor(scriptEngineBridges: ScriptEngineBridges) {
        scriptEngineBridges.apis.forEach { (t, u) ->
            putProperty(t, u)
        }
        init()
    }

    //fixme  loadAsset 效率问题
    fun init() {
        val originalThread = Thread.currentThread()
        GcCollector.regMainThread(global, originalThread)//注册线程
        if (!global.isInitialized) {
            global.init(androidHelper.contextFactory)
        }

        RhinoApi().define(global)

        //包名
        rhinoContext.evaluateString(global, builtinVariables, "<builtin>", 1, null)

        loadAsset(
                "rhino_require/threads.js",
                "rhino_require/view_op.js",
                "rhino_require/executor.js",
                "rhino_require/global.js",
                "rhino_require/storages.js",
                "rhino_require/settings.js",
                "rhino_require/utils.js"
        )
    }

    private fun putProperty(key: String, value: Any) {
        putProperty(global, key, value)
    }

    private fun loadAsset(vararg args: String) {
        RhinoApi.loadAsset(rhinoContext, global, args, null)
    }

    override fun stop() {
        quit(rhinoContext, global, null, null)
    }

    override fun release() {
        quit(rhinoContext, global, null, null)
    }

    fun setArgs(args: Array<*>?) {
        args ?: return
        Vog.d("args" + Arrays.toString(args))
        val array = arrayOfNulls<Any>(args!!.size)
        System.arraycopy(args, 0, array, 0, args!!.size)
        val argsObj = rhinoContext.newArray(global, array)
        global.defineProperty("args", argsObj, DONTENUM)

    }

    private fun setMap(map: Map<String, *>?) {
        global.defineProperty("argMap", MapScriptable(map), DONTENUM)
    }

    @Throws(Exception::class)

    override fun evalString(script: String, args: Array<*>?) {
        setArgs(args)
        evalS(script)
    }

    @Throws(Exception::class)
    override fun evalString(script: String, argMap: Map<String, *>?) {
        setMap(argMap)
        evalS(script)
    }

    @Throws(Exception::class)
    private fun evalS(scriptText: String) {
        try {
            val script = rhinoContext.compileString(scriptText, "<script>", 1, null)
            script?.exec(rhinoContext, global)
        } catch (we: WrappedException) {
            val e = we.wrappedException
            if (e is InterruptedException) {
                RhinoApi.doLog("强行终止")
                throw e
            } else {
                RhinoApi.doLog(e.message)
                throw e
            }
        } catch (t: Throwable) {
            RhinoApi.doLog(t.message)
            throw t
        }

    }

    override fun evalFile(file: String, argMap: Map<String, *>?) {
        setMap(argMap)
        processFileSecure(global, file, null)
    }


    @Throws(Exception::class)
    override fun evalFile(filename: String, args: Array<*>?) {
        setArgs(args)
        processFileSecure(global, filename, null)
    }


    @Throws(IOException::class)
    private fun processFileSecure(scope: Scriptable,
                                  path: String, securityDomain: Any?) {

        val isClass = path.endsWith(".class")
        val source = readFileOrUrl(path, !isClass)

        val digest = getDigest(source)
        val key = path + "_" + rhinoContext.optimizationLevel
        val ref = scriptCache.get(key, digest)
        var script: Script? = ref?.get()

        if (script == null) {
            if (isClass) {
                script = loadCompiledScript(path, source as ByteArray, securityDomain)
            } else {
                var strSrc = source as String
// Support the executable script #! syntax:  If
// the first line begins with a '#', treat the whole
// line as a comment.
                if (strSrc.isNotEmpty() && strSrc[0] == '#') {
                    for (i in 1 until strSrc.length) {
                        val c = strSrc[i].toInt()
                        if (c == '\n'.toInt() || c == '\r'.toInt()) {
                            strSrc = strSrc.substring(i)
                            break
                        }
                    }
                }
                script = rhinoContext.compileString(strSrc, path, 1, securityDomain)
            }
            scriptCache.put(key, digest, script)
        }

        script?.exec(rhinoContext, scope)
    }

    private fun getDigest(source: Any?): ByteArray? {
        val bytes: ByteArray?
        var digest: ByteArray? = null

        if (source != null) {
            bytes = if (source is String) {
                try {
                    source.toByteArray(charset("UTF-8"))
                } catch (ue: UnsupportedEncodingException) {
                    source.toByteArray()
                }

            } else {
                source as ByteArray?
            }
            try {
                val md = MessageDigest.getInstance("MD5")
                digest = md.digest(bytes)
            } catch (nsa: NoSuchAlgorithmException) {
// Should not happen
                throw RuntimeException(nsa)
            }

        }

        return digest
    }

    @Throws(FileNotFoundException::class)
    private fun loadCompiledScript(path: String,
                                   data: ByteArray?, securityDomain: Any?): Script {
        if (data == null) {
            throw FileNotFoundException(path)
        }
// XXX: For now extract class name of compiled Script from path
// instead of parsing class bytes
        var nameStart = path.lastIndexOf('/')
        if (nameStart < 0) {
            nameStart = 0
        } else {
            ++nameStart
        }
        var nameEnd = path.lastIndexOf('.')
        if (nameEnd < nameStart) {
// '.' does not exist in path (nameEnd < 0)
// or it comes before nameStart
            nameEnd = path.length
        }
        val name = path.substring(nameStart, nameEnd)
        try {
            val loader = SecurityController.createLoader(rhinoContext!!.getApplicationClassLoader(), securityDomain)
            val clazz = loader.defineClass(name, data)
            loader.linkClass(clazz)
            if (!Script::class.java.isAssignableFrom(clazz)) {
                throw Context.reportRuntimeError("msg.must.implement.Script")
            }
            return clazz.newInstance() as Script
        } catch (iaex: IllegalAccessException) {
//Context.reportError(iaex.toString());
            throw RuntimeException(iaex)
        } catch (inex: InstantiationException) {
//Context.reportError(inex.toString());
            throw RuntimeException(inex)
        }

    }

    /**
     * Read file or url specified by <tt>path</tt>.
     *
     * @return file or url content as <tt>byte[]</tt> or as <tt>String</tt> if
     * <tt>convertToString</tt> is true.
     */
    @Throws(IOException::class)
    private fun readFileOrUrl(path: String, convertToString: Boolean): Any {
        return SourceReader.readFileOrUrl(path, convertToString,
                Charset.defaultCharset().toString())
//contextFactory.getCharacterEncoding());
    }

    internal class ScriptReference(var path: String, var digest: ByteArray?,
                                   script: Script?, queue: ReferenceQueue<Script>) : SoftReference<Script>(script, queue)

    internal class ScriptCache(var capacity: Int) : LinkedHashMap<String, ScriptReference>(capacity + 1, 2f, true) {
        var queue: ReferenceQueue<Script> = ReferenceQueue()

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ScriptReference>?): Boolean {
            return size > capacity
        }


        fun get(path: String, digest: ByteArray?): ScriptReference? {
            var ref: ScriptReference? = null
            while (queue.poll().also { ref = it as ScriptReference? } != null) {
                remove(ref!!.path)
            }
            ref = get(path)
            if (ref != null && !Arrays.equals(digest, ref!!.digest)) {
                remove(ref!!.path)
                ref = null
            }
            return ref
        }

        fun put(path: String, digest: ByteArray?, script: Script?) {
            put(path, ScriptReference(path, digest, script, queue))
        }

    }

    private class InterruptibleAndroidContextFactory(cacheDirectory: File) : AndroidContextFactory(cacheDirectory) {


        protected override fun observeInstructionCount(cx: Context?, instructionCount: Int) {
            if (Thread.currentThread().isInterrupted() && Looper.myLooper() != Looper.getMainLooper()) {
                throw RuntimeException("Thread isInterrupted")
            }
        }

        protected override fun makeContext(): Context {
            val cx = super.makeContext()
            cx.setInstructionObserverThreshold(10000)
            return cx
        }

    }

    companion object {
        private val serialVersionUID = -804736422632824973L
        private val scriptCache = ScriptCache(32)
    }
}
