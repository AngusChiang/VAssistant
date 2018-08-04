package cn.vove7.androlua.luautils;

import android.content.Context;

import com.luajava.LuaException;

import java.util.ArrayList;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

/**
 * LuaManagerI
 * 负责管理
 * <p>
 * Created by Vove on 2018/8/1
 */
public interface LuaManagerI {
    HashMap<String, String> getLibrarys();

    ArrayList<ClassLoader> getClassLoaders();

    DexClassLoader loadDex(String path) throws LuaException;

    void regGc(LuaGcable obj);

    void gc(LuaGcable obj);

    void stop();


    int L = 0;
    int W = 1;//Prompt
    int E = 2;

    void handleMessage(int l, String msg);

    void log(String log);

    Context getApp();
}
