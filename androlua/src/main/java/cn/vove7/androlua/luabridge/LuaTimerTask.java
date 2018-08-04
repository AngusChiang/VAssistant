package cn.vove7.androlua.luabridge;

import cn.vove7.androlua.luautils.TimerTaskX;
import com.luajava.LuaException;
import com.luajava.LuaObject;
import com.luajava.LuaState;

import cn.vove7.androlua.LuaApp;
import cn.vove7.androlua.luautils.LuaManagerI;

/**
 * LuaTask
 */
public class LuaTimerTask extends TimerTaskX implements LuaRunnableI {
    private LuaState L;
    private LuaManagerI luaManager;

    private String mSrc;
    private LuaThreadHelper threadHelper;

    private Object[] mArg = new Object[0];

    private boolean mEnabled = true;

    private byte[] mBuffer;

    public LuaTimerTask(LuaManagerI luaManager, String src) {
        this(luaManager, src, null);
    }

    public LuaTimerTask(LuaManagerI luaManager, String src, Object[] arg) {
        this.luaManager = luaManager;
        mSrc = src;
        if (arg != null)
            mArg = arg;
        L = LuaApp.getInstance().getLuaHelper().L;
        threadHelper = new LuaThreadHelper(luaManager, L);
    }

    @Override
    public void quit() {
        L.gc(LuaState.LUA_GCCOLLECT, 1);
        System.gc();
        if (isCancelled()) {
            luaManager.handleMessage(LuaManagerI.W, getName() + " already canceled");
            return;
        }
        luaManager.log(getName() + " quit");
        cancel();
    }

    public LuaTimerTask(LuaManagerI luaManager, LuaObject func) throws LuaException {
        this(luaManager, func, null);
    }

    public LuaTimerTask(LuaManagerI luaManager, LuaObject func, Object[] arg) throws LuaException {
        this.luaManager = luaManager;
        if (arg != null)
            mArg = arg;
        mBuffer = func.dump();
        L = LuaApp.getInstance().getLuaHelper().L;
        threadHelper = new LuaThreadHelper(luaManager, L);
    }

    String getName() {
        return "TimerTask " + hashCode() + " ";
    }

    @Override
    public void run() {
        if (!mEnabled) {
            luaManager.log(getName() + "Enabled is false");
            return;
        }
        L.getGlobal("run");
        try {
            if (!L.isNil(-1))
                threadHelper.runFunc("run");
            else {
                if (mBuffer != null)
                    threadHelper.newLuaThread(mBuffer, mArg);
                else
                    threadHelper.newLuaThread(mSrc, mArg);
            }
        } catch (LuaException e) {
            luaManager.handleMessage(LuaManagerI.E,e.getMessage());
            e.printStackTrace();
            quit();
        }
    }

    public void setArg(Object[] arg) {
        mArg = arg;
    }

    public void setArg(LuaObject arg) throws ArrayIndexOutOfBoundsException, LuaException, IllegalArgumentException {
        mArg = arg.asArray();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void set(String key, Object value) throws LuaException {
        L.pushObjectValue(value);
        L.setGlobal(key);
    }

    public Object get(String key) throws LuaException {
        L.getGlobal(key);
        return L.toJavaObject(-1);
    }
}
