package cn.vove7.androlua.luautils;

import android.content.Context;

import com.luajava.LuaState;

import java.util.Map;

public interface LuaContext {

    public void call(String func, Object... args);

    public void set(String name, Object value);
    public Object get(String name);

    public Context getContext();

    public void sendMsg(String msg);

    public void sendError(String title, Exception msg);

    public int getWidth();

    public int getHeight();

    public Map getGlobalData();

    //LuaState getLuaState();

}
