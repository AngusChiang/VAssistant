package cn.vove7.androlua.luautils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.luajava.LuaException;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import cn.vove7.androlua.LuaApp;
import cn.vove7.androlua.luabridge.LuaUtil;
import dalvik.system.DexClassLoader;

public class LuaDexLoader {
    private static HashMap<String, LuaDexClassLoader> dexCache = new HashMap<String, LuaDexClassLoader>();
    private ArrayList<ClassLoader> dexList = new ArrayList<ClassLoader>();
    private HashMap<String, String> libCache = new HashMap<String, String>();

    private Context mContext;

    private String luaDir;

    private AssetManager mAssetManager;

    private Resources mResources;

    private Resources.Theme mTheme;

    private String odexDir;

    public LuaDexLoader(Context context) {
        LuaApp app = LuaApp.Companion.getInstance();
        mContext = context;
        luaDir = app.getFilesDir().getAbsolutePath();
        //localDir = app.getLocalDir();
        odexDir = app.getDir("odex", Context.MODE_PRIVATE).getAbsolutePath();
    }

    public ArrayList<ClassLoader> getClassLoaders() {
        return dexList;
    }

    public void loadLibs() throws LuaException {
        File[] libs = new File(luaDir + "/libs").listFiles();
        if (libs == null)
            return;
        for (File f : libs) {
            if (f.getAbsolutePath().endsWith(".so"))
                loadLib(f.getName());
            else
                loadDex(f.getAbsolutePath());
        }
    }

    public void loadLib(String name) throws LuaException {
        String fn = name;
        int i = name.indexOf(".");
        if (i > 0)
            fn = name.substring(0, i);
        if (fn.startsWith("lib"))
            fn = fn.substring(3);
        String libDir = mContext.getDir(fn, Context.MODE_PRIVATE).getAbsolutePath();
        String libPath = libDir + "/lib" + fn + ".so";
        File f = new File(libPath);
        if (!f.exists()) {
            f = new File(luaDir + "/libs/lib" + fn + ".so");
            if (!f.exists())
                throw new LuaException("can not find lib " + name);
            LuaUtil.copyFile(luaDir + "/libs/lib" + fn + ".so", libPath);

        }
        libCache.put(fn, libPath);
    }

    public HashMap<String, String> getLibrarys() {
        return libCache;
    }


    public DexClassLoader loadDex(String path) throws LuaException {
        String name = path;
        LuaDexClassLoader dex = dexCache.get(name);
        if (dex == null) {
            if (path.charAt(0) != '/')
                path = luaDir + "/" + path;
            if (!new File(path).exists())
                if (new File(path + ".dex").exists())
                    path += ".dex";
                else if (new File(path + ".jar").exists())
                    path += ".jar";
                else
                    throw new LuaException(path + " not found");
            dex = new LuaDexClassLoader(path, odexDir, LuaApp.Companion.getInstance().getApplicationInfo().nativeLibraryDir, mContext.getClassLoader());
            dexCache.put(name, dex);
        }


        if (!dexList.contains(dex)) {
            dexList.add(dex);
            path = dex.getDexPath();
            if (path.endsWith(".jar"))
                loadResources(path);
        }
        return dex;
    }

    public void loadResources(String path) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            int ok = (int) addAssetPath.invoke(assetManager, path);
            if (ok == 0)
                return;
            mAssetManager = assetManager;
            Resources superRes = mContext.getResources();
            mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),
                    superRes.getConfiguration());
            mTheme = mResources.newTheme();
            mTheme.setTo(mContext.getTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AssetManager getAssets() {
        return mAssetManager;
    }

    public Resources getResources() {
        return mResources;
    }

}
