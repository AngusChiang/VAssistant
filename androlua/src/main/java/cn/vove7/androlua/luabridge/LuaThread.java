package cn.vove7.androlua.luabridge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import cn.vove7.androlua.luautils.LuaGcable;
import com.luajava.LuaException;
import com.luajava.LuaMetaTable;
import com.luajava.LuaObject;
import com.luajava.LuaState;

import cn.vove7.androlua.LuaApp;
import cn.vove7.androlua.luautils.LuaManagerI;

/**
 * 线程
 */
public class LuaThread extends Thread implements LuaMetaTable, LuaGcable, LuaRunnableI {

    public boolean isRun = false;
    private LuaState L;
    private Handler tHandler;
    private LuaManagerI luaManager;
    private boolean mIsLoop;
    private String mSrc;
    private Object[] mArg = new Object[0];
    private byte[] mBuffer;
    private LuaThreadHelper threadHelper;

    public LuaThread(LuaManagerI luaManager, String src) {
        this(luaManager, src, false, null);
    }

    public LuaThread(LuaManagerI luaManager, String src, Object[] arg) {
        this(luaManager, src, false, arg);
    }

    public LuaThread(LuaManagerI luaManager, String src, boolean isLoop) {
        this(luaManager, src, isLoop, null);
    }

    public LuaThread(LuaManagerI luaManager, String src, boolean isLoop, Object[] arg) {
        luaManager.regGc(this);
        this.luaManager = luaManager;
        mSrc = src;
        mIsLoop = isLoop;
        if (arg != null)
            mArg = arg;

        L = LuaApp.getInstance().getLuaHelper().L;
        threadHelper = new LuaThreadHelper(luaManager, L);
    }

    public LuaThread(LuaManagerI luaManager, LuaObject func) throws LuaException {
        this(luaManager, func, false, null);
    }

    public LuaThread(LuaManagerI luaManager, LuaObject func, Object[] arg) throws LuaException {
        this(luaManager, func, false, arg);
    }

    public LuaThread(LuaManagerI luaManager, LuaObject func, boolean isLoop) throws LuaException {
        this(luaManager, func, isLoop, null);
    }

    public LuaThread(LuaManagerI luaManager, LuaObject func, boolean isLoop, Object[] arg) throws LuaException {
        LuaApp.getInstance().regGc(this);
        this.luaManager = luaManager;
        if (arg != null)
            mArg = arg;
        mIsLoop = isLoop;
        mBuffer = func.dump();

        L = LuaApp.getInstance().getLuaHelper().L;
        threadHelper = new LuaThreadHelper(luaManager, L);
    }

    @Override
    public void gc() {
        quit();
    }

    @Override
    public Object __call(Object[] arg) {
        return null;
    }

    @Override
    public Object __index(final String key) {
        return new LuaMetaTable() {
            @Override
            public Object __call(Object[] arg) {
                call(key, arg);
                return null;
            }

            @Override
            public Object __index(String key) {
                return null;
            }

            @Override
            public void __newIndex(String key, Object value) {
            }
        };
    }

    @Override
    public void __newIndex(String key, Object value) {
        set(key, value);
    }

    @Override
    public void run() {
        try {
            if (mBuffer != null)
                threadHelper.newLuaThread(mBuffer, mArg);
            else
                threadHelper.newLuaThread(mSrc, mArg);
            if (mIsLoop) {
                Looper.prepare();
                tHandler = new LuaFunHandler(threadHelper, luaManager);
                isRun = true;
                L.getGlobal("run");
                if (!L.isNil(-1)) {
                    L.pop(1);
                    threadHelper.runFunc("run");
                }
                Looper.loop();
            }
        } catch (LuaException e) {
            luaManager.handleMessage(LuaManagerI.E, e.getMessage());
            e.printStackTrace();
        }
        isRun = false;
        quit();
    }

    public void call(String func) {
        push(3, func);
    }

    public void call(String func, Object[] args) {
        if (args.length == 0)
            push(3, func);
        else
            push(1, func, args);
    }

    public void set(String key, Object value) {
        push(4, key, new Object[]{value});
    }

    public Object get(String key) throws LuaException {
        L.getGlobal(key);
        return L.toJavaObject(-1);
    }

    @Override
    public void quit() {
        L.gc(LuaState.LUA_GCCOLLECT, 1);
        System.gc();
        if (isRun) {
            isRun = false;
            tHandler.getLooper().quit();
        }
    }

    public void push(int what, String s) {
        if (!isRun) {
            luaManager.handleMessage(LuaManagerI.L, "thread is not running");
            return;
        }
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("data", s);
        message.setData(bundle);
        message.what = what;

        tHandler.sendMessage(message);

    }

    public void push(int what, String s, Object[] args) {
        if (!isRun) {
            luaManager.handleMessage(LuaManagerI.L, "thread is not running");
            return;
        }

        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("data", s);
        bundle.putSerializable("args", args);
        message.setData(bundle);
        message.what = what;

        tHandler.sendMessage(message);

    }


    //private void newLuaThread(String str, Object... args) {
    //    try {
    //        LuaApp.getInstance().luaHelper.autoRun(str, args);
    //    } catch (Exception e) {
    //        luaManager.handleMessage(this.toString() + " " + e.getMessage());
    //        quit();
    //    }
    //}

    //private void newLuaThread(byte[] buf, Object... args) {
    //    try {
    //        L.setTop(0);
    //        int ok = L.LloadBuffer(buf, "Thread");
    //
    //        if (ok == 0) {
    //            L.getGlobal("debug");
    //            L.getField(-1, "traceback");
    //            L.remove(-2);
    //            L.insert(-2);
    //            int l = args.length;
    //            for (Object arg : args) {
    //                L.pushObjectValue(arg);
    //            }
    //            ok = L.pcall(l, 0, -2 - l);
    //            if (ok == 0) {
    //                return;
    //            }
    //        }
    //        throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
    //    } catch (Exception e) {
    //        luaManager.handleMessage(this.toString() + " " + e.getMessage());
    //        quit();
    //    }
    //}

    //private void runFunc(String funcName, Object... args) {
    //    try {
    //        L.setTop(0);
    //        L.getGlobal(funcName);
    //        if (L.isFunction(-1)) {
    //            L.getGlobal("debug");
    //            L.getField(-1, "traceback");
    //            L.remove(-2);
    //            L.insert(-2);
    //
    //            int l = args.length;
    //            for (int i = 0; i < l; i++) {
    //                L.pushObjectValue(args[i]);
    //            }
    //
    //            int ok = L.pcall(l, 1, -2 - l);
    //            if (ok == 0) {
    //                return;
    //            }
    //            throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
    //        }
    //    } catch (LuaException e) {
    //        luaManager.handleMessage(funcName + " " + e.getMessage());
    //    }
    //
    //}

    //private void setField(String key, Object value) {
    //    try {
    //        L.pushObjectValue(value);
    //        L.setGlobal(key);
    //    } catch (LuaException e) {
    //        luaManager.handleMessage(e.getMessage());
    //    }
    //}

    private static class LuaFunHandler extends Handler {
        LuaThreadHelper threadHelper;
        LuaManagerI luaManager;

        public LuaFunHandler(LuaThreadHelper threadHelper, LuaManagerI luaManager) {
            this.threadHelper = threadHelper;
            this.luaManager = luaManager;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            try {
                switch (msg.what) {
                    case 0:
                        threadHelper.newLuaThread(data.getString("data"), (Object[]) data.getSerializable("args"));
                        break;
                    case 1:
                        threadHelper.runFunc(data.getString("data"), (Object[]) data.getSerializable("args"));
                        break;
                    case 2:
                        threadHelper.newLuaThread(data.getString("data"));
                        break;
                    case 3:
                        threadHelper.runFunc(data.getString("data"));
                        break;
                    case 4:
                        threadHelper.setField(data.getString("data"), ((Object[]) data.getSerializable("args"))[0]);
                        break;
                }
            } catch (LuaException e) {
                luaManager.handleMessage(LuaManagerI.E, e.getMessage());
                e.printStackTrace();
            }
        }
    }
}


