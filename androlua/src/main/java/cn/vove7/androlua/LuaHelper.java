package cn.vove7.androlua;

import android.content.Context;
import android.util.Log;

import cn.vove7.androlua.androbridge.BridgeManager;
import cn.vove7.androlua.luabridge.LuaThread;
import cn.vove7.androlua.luabridge.LuaUtil;
import cn.vove7.androlua.luautils.LuaGcable;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import cn.vove7.androlua.luautils.LuaDexLoader;
import cn.vove7.androlua.luautils.LuaManagerI;
import cn.vove7.androlua.luautils.LuaPrinter;
import dalvik.system.DexClassLoader;

import static cn.vove7.androlua.luabridge.LuaUtil.errorReason;
import static com.luajava.LuaState.LUA_GCSTOP;

/**
 * LuaHelper
 * <p>
 * Created by Vove on 2018/7/30
 */
public class LuaHelper implements LuaManagerI {
    private Context context;
    public LuaState L;
    private String libDir;
    private String jniLibsPath;
    private String luaDir;
    private ArrayList<LuaGcable> gcList;
    private String luaRequireSearchPath;
    private LuaDexLoader mLuaDexLoader;
    private BridgeManager bridgeManager;

    public BridgeManager getBridgeManager() {
        return bridgeManager;
    }

    public void setBridgeManager(BridgeManager bridgeManager) {
        this.bridgeManager = bridgeManager;
    }

    public LuaHelper(Context context) {
        this.context = context;
        initPath();
        init();
    }

    private void init() {
        gcList = new ArrayList<>();
        mLuaDexLoader = new LuaDexLoader(context);
        try {
            mLuaDexLoader.loadLibs();
        } catch (LuaException e) {
            e.printStackTrace();
        }
        initLua();
    }

    private void initPath() {
        luaDir = context.getFilesDir().getAbsolutePath();
        Log.d("Vove :", "initLua luaDir ----> " + luaDir);
        libDir = context.getDir("lib", Context.MODE_PRIVATE).getAbsolutePath();
        jniLibsPath = context.getApplicationInfo().nativeLibraryDir + "/lib?.so" + ";" + libDir + "/lib?.so;";

        String assetPath = "file://android_assset";
        String assetInclude = assetPath + "/?.lua;" + assetPath + "/?/init.lua;";
        luaRequireSearchPath = (luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;");
        luaRequireSearchPath += jniLibsPath + assetInclude;
    }

    @Override
    public void log(String log) {
        Log.d("Vove :", "lua log  ----> " + log);
    }

    private void initLua() {
        L = LuaStateFactory.newLuaState();
        L.openLibs();
        L.pushJavaObject(this);
        L.setGlobal("luaman");
        L.getGlobal("luaman");
        L.pushContext(context);
        //L.setGlobal("app");

        L.getGlobal("luajava");

        //L.pushString(luaDir);
        //L.setField(-2, "luaextdir");
        L.pushString(luaDir);
        L.setField(-2, "luaDir");
        //L.pushString(luaDir);
        //L.setField(-2, "luapath");
        L.pop(1);

        L.getGlobal("package");
        L.pushString(luaRequireSearchPath);
        L.setField(-2, "path");
        L.pushString(jniLibsPath);
        L.setField(-2, "cpath");
        L.pop(1);

        new LuaPrinter(L, new LuaPrinter.OnPrint() {
            @Override
            public void onPrint(int l, String output) {
                notifyOutput(l, output);
            }
        });

        JavaFunction set = new JavaFunction(L) {
            @Override
            public int execute() throws LuaException {
                LuaThread thread = (LuaThread) L.toJavaObject(2);

                thread.set(L.toString(3), L.toJavaObject(4));
                return 0;
            }
        };

        JavaFunction call = new JavaFunction(L) {
            @Override
            public int execute() throws LuaException {
                LuaThread thread = (LuaThread) L.toJavaObject(2);

                int top = L.getTop();
                if (top > 3) {
                    Object[] args = new Object[top - 3];
                    for (int i = 4; i <= top; i++) {
                        args[i - 4] = L.toJavaObject(i);
                    }
                    thread.call(L.toString(3), args);
                } else if (top == 3) {
                    thread.call(L.toString(3));
                }
                return 0;
            }
        };
        try {
            set.register("set");
            call.register("call");
        } catch (LuaException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleMessage(int l, String msg) {
        notifyOutput(l,msg);
    }

    //private void initLua1() {
    //
    //    try {
    //        L = LuaStateFactory.newLuaState();
    //        L.openLibs();
    //
    //        L.pushJavaObject(context);
    //        L.setGlobal("this");
    //
    //        JavaFunction assetLoader = new LuaAssetLoader(context, L);
    //
    //        L.getGlobal("package");            // package
    //        L.getField(-1, "loaders");         // package loaders
    //        int nLoaders = L.objLen(-1);       // package loaders
    //
    //        L.pushJavaFunction(assetLoader);   // package loaders loader
    //        L.rawSetI(-2, nLoaders + 1);       // package loaders
    //        L.pop(1);                          // package
    //
    //        L.getField(-1, "path");            // package path
    //        String customPath = context.getFilesDir() + "/?.lua";
    //        L.pushString(";" + customPath);    // package path custom
    //        L.concat(2);                       // package pathCustom
    //        L.setField(-2, "path");            // package
    //        L.pop(1);
    //
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //}

    void safeEvalLua(String src) {
        try {
            evalString(src);
        } catch (LuaException e) {
            e.printStackTrace();
            Log.d("Vove :", "safeEvalLua  ----> " + e.getMessage());
        }
    }

    private void notifyOutput(int l, String o) {
        synchronized (printList) {
            for (LuaPrinter.OnPrint p : printList) {
                p.onPrint(l, o);
            }
        }
    }

    void evalString(String src, Object... args) throws LuaException {
        L.setTop(0);
        int ok = L.LloadString(src);
        if (ok == 0) {
            L.getGlobal("debug");
            L.getField(-1, "traceback");
            L.remove(-2);
            L.insert(-2);
            int l = args.length;
            for (Object arg : args) {
                L.pushObjectValue(arg);
            }
            ok = L.pcall(l, 0, -2 - l);
            if (ok == 0) {
                return;
            } else {
                String e = errorReason(ok) + ": " + L.toString(-1);
                Log.e("Vove :", "evalString  ----> " + e);
                notifyOutput(LuaManagerI.E, e);
                return;
            }
        }
        throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
    }


    public void autoRun(String s, Object... args) throws LuaException {
        Log.d("Vove :", "autoRun  ----> " + s);
        if (Pattern.matches("^\\w+$", s)) {
            execFromAsset(s + ".lua", args);
        } else if (Pattern.matches("^[\\w._/]+$", s)) {
            execFromFile(s, args);
        } else {
            evalString(s, args);
        }
    }

    public void execFromFile(String filePath, Object... args) throws LuaException {
        int ok = 0;
        L.setTop(0);
        ok = L.LloadFile(filePath);

        if (ok == 0) {
            L.getGlobal("debug");
            L.getField(-1, "traceback");
            L.remove(-2);
            L.insert(-2);
            int l = args.length;
            for (int i = 0; i < l; i++) {
                L.pushObjectValue(args[i]);
            }
            ok = L.pcall(l, 0, -2 - l);
            if (ok == 0) {
                return;
            }
        }
        throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
    }

    public void execFromAsset(String name, Object... args) throws LuaException {
        byte[] bytes;
        try {
            bytes = LuaUtil.readAsset(context, name);
        } catch (IOException e) {
            throw new LuaException(e.getMessage());
        }
        L.setTop(0);
        int ok = L.LloadBuffer(bytes, name);

        if (ok == 0) {
            L.getGlobal("debug");
            L.getField(-1, "traceback");
            L.remove(-2);
            L.insert(-2);
            int l = args.length;
            for (Object arg : args) {
                L.pushObjectValue(arg);
            }
            ok = L.pcall(l, 0, -2 - l);
            if (ok == 0) {
                return;
            }
        }
        throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
    }

    @Override
    public void regGc(LuaGcable obj) {
        gcList.add(obj);
    }


    @Override
    public HashMap<String, String> getLibrarys() {
        return mLuaDexLoader.getLibrarys();
    }

    @Override
    public ArrayList<ClassLoader> getClassLoaders() {
        return mLuaDexLoader.getClassLoaders();
    }

    @Override
    public DexClassLoader loadDex(String path) throws LuaException {
        return mLuaDexLoader.loadDex(path);
    }

    @Override
    public void gc(LuaGcable obj) {
        obj.gc();
        gcList.remove(obj);
    }

    private void gcAll() {
        //清空线程..
        for (LuaGcable gcable : gcList) {
            if (gcable != null)
                gcable.gc();
        }
        gcList.clear();
    }

    @Override
    public void stop() {
        gcAll();
        L.gc(LUA_GCSTOP, 0);
        LuaStateFactory.removeLuaState(L.getPointer());
        L = null;
        init();
    }


    public void loadResources(String path) {
        mLuaDexLoader.loadResources(path);
    }

    /**
     * 注册打印
     */
    private final List<LuaPrinter.OnPrint> printList = new ArrayList<>();

    public void regPrint(LuaPrinter.OnPrint print) {
        printList.add(print);
    }

    @Override
    public Context getApp() {
        return context;
    }

    public void unRegPrint(LuaPrinter.OnPrint print) {
        printList.remove(print);
    }
}
