package cn.vove7.rhino;

import android.os.Looper;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.tools.SourceReader;
import org.mozilla.javascript.tools.shell.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import cn.vove7.common.BridgeManager;
import cn.vassistant.plugininterface.app.GlobalApp;
import cn.vove7.rhino.api.RhinoApi;
import cn.vove7.rhino.common.AndroidContextFactory;
import cn.vove7.rhino.common.GcCollector;
import cn.vove7.rhino.common.RhinoAndroidHelper;
import cn.vove7.vtp.log.Vog;

import static cn.vove7.rhino.api.RhinoApi.quit;


public class RhinoHelper extends ScriptableObject {
    private static final long serialVersionUID = -804736422632824973L;

    @Override
    public String getClassName() {
        return "RhinoHelper";
    }

    private android.content.Context appContext = GlobalApp.APP;

    public RhinoHelper(android.content.Context appContext) {
        this.appContext = appContext;
        init();
    }

    private Context rhinoContext;
    private boolean inited = false;
    private Global global = new Global();
    private final static ScriptCache scriptCache = new ScriptCache(32);


    public RhinoHelper() {
        init();
    }

    public RhinoHelper(BridgeManager bridgeManager) {
        putProperty("executor", bridgeManager.getExecutor());
        putProperty("http", bridgeManager.getHttpBridge());
        putProperty("runtime", bridgeManager.getExecutor());
        putProperty("system", bridgeManager.getSystemBridge());
        putProperty("automator", bridgeManager.getAutomator());
        putProperty("androRuntime", bridgeManager.getRootHelper());
        putProperty("serviceBridge", bridgeManager.getServiceBridge());
        putProperty("app", appContext);
        init();
    }

    //fixme  loadAsset 效率问题
    public void init() {
        RhinoAndroidHelper androidHelper = new RhinoAndroidHelper(appContext);
        rhinoContext = androidHelper.enterContext();

        Thread originalThread = Thread.currentThread();
        GcCollector.regMainThread(global, originalThread);//注册线程
        if (!global.isInitialized()) {
            global.init(androidHelper.getContextFactory());
        }
        //global.initQuitAction(new IProxy(IProxy.SYSTEM_EXIT));
        //errorReporter = new ToolErrorReporter(false, global.getErr());
        //rhinoContext.setErrorReporter(errorReporter);

//        if (!ContextFactory.hasExplicitGlobal()) {
//            ContextFactory.initGlobal(new InterruptibleAndroidContextFactory(
//                    new File(appContext.getCacheDir(), "classes")));
//        }

        //putProperty("app", appContext);

        //global.defineProperty("context", rhinoContext,
        //        ScriptableObject.READONLY);

        //initRequireBuilder(rhinoContext, global);

        new RhinoApi().bind(global);
        //new ViewFinderApi().bind(global);

        loadAsset("apis.js");
        inited = true;
    }

    public void putProperty(String key, Object value) {
        ScriptableObject.putProperty(global, key, value);
    }

    public boolean isInited() {
        return inited;
    }

    private void loadAsset(String... args) {
        RhinoApi.loadAsset(rhinoContext, global, args, null);
    }

    /**
     * 初始化require
     * <p>
     * //* @param context Context
     * //* @param scope   global
     */
    //private void initRequireBuilder(Context context, Scriptable scope) {
    //    AssetAndUrlModuleSourceProvider provider = new AssetAndUrlModuleSourceProvider(appContext,
    //            Collections.singletonList(new File("/").toURI()));
    //    new RequireBuilder()
    //            .setModuleScriptProvider(new SoftCachingModuleScriptProvider(provider))
    //            .setSandboxed(false)
    //            .createRequire(context, scope)
    //            .install(scope);
    //
    //}
    public RhinoHelper(Object o) {

    }

    public void stop() {
        quit(rhinoContext, global, null, null);
    }


    public void setArgs(String... args) {
        Vog.INSTANCE.d(this, "args" + Arrays.toString(args));
        Object[] array = new Object[args.length];
        System.arraycopy(args, 0, array, 0, args.length);
        Scriptable argsObj = rhinoContext.newArray(global, array);
        global.defineProperty("args", argsObj,
                ScriptableObject.DONTENUM);
        global.defineProperty("arguments", argsObj,
                ScriptableObject.DONTENUM);
    }

    public void evalString(String scriptText, String... args) {
        setArgs(args);
        Script script = rhinoContext.compileString(scriptText, "<script>", 1, null);
        if (script != null) {
            script.exec(rhinoContext, global);
        }
    }


    public void execFile(String filename, String... args) {
        try {
            processFile(filename, args);
        } catch (Exception rex) {
            RhinoApi.onException(rex);
        }
    }

    private void processFile(String filename, String... args) throws IOException {
        setArgs(args);
        processFileSecure(global, filename, null);
    }

    private void processFileSecure(Scriptable scope,
                                   String path, Object securityDomain)
            throws IOException {

        boolean isClass = path.endsWith(".class");
        Object source = readFileOrUrl(path, !isClass);

        byte[] digest = getDigest(source);
        String key = path + "_" + rhinoContext.getOptimizationLevel();
        ScriptReference ref = scriptCache.get(key, digest);
        Script script = ref != null ? ref.get() : null;

        if (script == null) {
            if (isClass) {
                script = loadCompiledScript(path, (byte[]) source, securityDomain);
            } else {
                String strSrc = (String) source;
                // Support the executable script #! syntax:  If
                // the first line begins with a '#', treat the whole
                // line as a comment.
                if (strSrc.length() > 0 && strSrc.charAt(0) == '#') {
                    for (int i = 1; i != strSrc.length(); ++i) {
                        int c = strSrc.charAt(i);
                        if (c == '\n' || c == '\r') {
                            strSrc = strSrc.substring(i);
                            break;
                        }
                    }
                }
                script = rhinoContext.compileString(strSrc, path, 1, securityDomain);
            }
            scriptCache.put(key, digest, script);
        }

        if (script != null) {
            script.exec(rhinoContext, scope);
        }
    }

    private byte[] getDigest(Object source) {
        byte[] bytes, digest = null;

        if (source != null) {
            if (source instanceof String) {
                try {
                    bytes = ((String) source).getBytes("UTF-8");
                } catch (UnsupportedEncodingException ue) {
                    bytes = ((String) source).getBytes();
                }
            } else {
                bytes = (byte[]) source;
            }
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                digest = md.digest(bytes);
            } catch (NoSuchAlgorithmException nsa) {
                // Should not happen
                throw new RuntimeException(nsa);
            }
        }

        return digest;
    }

    private Script loadCompiledScript(String path,
                                      byte[] data, Object securityDomain)
            throws FileNotFoundException {
        if (data == null) {
            throw new FileNotFoundException(path);
        }
        // XXX: For now extract class name of compiled Script from path
        // instead of parsing class bytes
        int nameStart = path.lastIndexOf('/');
        if (nameStart < 0) {
            nameStart = 0;
        } else {
            ++nameStart;
        }
        int nameEnd = path.lastIndexOf('.');
        if (nameEnd < nameStart) {
            // '.' does not exist in path (nameEnd < 0)
            // or it comes before nameStart
            nameEnd = path.length();
        }
        String name = path.substring(nameStart, nameEnd);
        try {
            GeneratedClassLoader loader = SecurityController.createLoader(rhinoContext.getApplicationClassLoader(), securityDomain);
            Class<?> clazz = loader.defineClass(name, data);
            loader.linkClass(clazz);
            if (!Script.class.isAssignableFrom(clazz)) {
                throw Context.reportRuntimeError("msg.must.implement.Script");
            }
            return (Script) clazz.newInstance();
        } catch (IllegalAccessException iaex) {
            //Context.reportError(iaex.toString());
            throw new RuntimeException(iaex);
        } catch (InstantiationException inex) {
            //Context.reportError(inex.toString());
            throw new RuntimeException(inex);
        }
    }

    /**
     * Read file or url specified by <tt>path</tt>.
     *
     * @return file or url content as <tt>byte[]</tt> or as <tt>String</tt> if
     * <tt>convertToString</tt> is true.
     */
    private Object readFileOrUrl(String path, boolean convertToString)
            throws IOException {
        return SourceReader.readFileOrUrl(path, convertToString,
                Charset.defaultCharset().toString());
        //contextFactory.getCharacterEncoding());
    }

    static class ScriptReference extends SoftReference<Script> {
        String path;
        byte[] digest;

        ScriptReference(String path, byte[] digest,
                        Script script, ReferenceQueue<Script> queue) {
            super(script, queue);
            this.path = path;
            this.digest = digest;
        }
    }

    static class ScriptCache extends LinkedHashMap<String, ScriptReference> {
        ReferenceQueue<Script> queue;
        int capacity;

        ScriptCache(int capacity) {
            super(capacity + 1, 2f, true);
            this.capacity = capacity;
            queue = new ReferenceQueue<Script>();
        }

        @Override
        protected boolean removeEldestEntry(Entry<String, ScriptReference> eldest) {
            return size() > capacity;
        }

        ScriptReference get(String path, byte[] digest) {
            ScriptReference ref;
            while ((ref = (ScriptReference) queue.poll()) != null) {
                remove(ref.path);
            }
            ref = get(path);
            if (ref != null && !Arrays.equals(digest, ref.digest)) {
                remove(ref.path);
                ref = null;
            }
            return ref;
        }

        void put(String path, byte[] digest, Script script) {
            put(path, new ScriptReference(path, digest, script, queue));
        }

    }

    private static class InterruptibleAndroidContextFactory extends AndroidContextFactory {

        public InterruptibleAndroidContextFactory(File cacheDirectory) {
            super(cacheDirectory);
        }


        @Override
        protected void observeInstructionCount(Context cx, int instructionCount) {
            if (Thread.currentThread().isInterrupted() && Looper.myLooper() != Looper.getMainLooper()) {
                throw new RuntimeException("Thread isInterrupted");
            }
        }

        @Override
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setInstructionObserverThreshold(10000);
            return cx;
        }

    }
}
