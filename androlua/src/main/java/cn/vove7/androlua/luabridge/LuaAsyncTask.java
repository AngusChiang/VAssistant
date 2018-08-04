package cn.vove7.androlua.luabridge;

import android.os.AsyncTask;

import cn.vove7.androlua.luautils.LuaGcable;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaObject;
import com.luajava.LuaState;

import cn.vove7.androlua.LuaApp;
import cn.vove7.androlua.luautils.LuaManagerI;

import static cn.vove7.androlua.luabridge.LuaUtil.errorReason;

public class LuaAsyncTask extends AsyncTask implements LuaGcable {

    private Object[] loadeds;
    private LuaState L;

    private LuaManagerI luaManager;
    private byte[] mBuffer;
    private long mDelay = 0;
    private LuaObject mCallback;
    private LuaObject mUpdate;

    public LuaAsyncTask(LuaManagerI luaManager, long delay, LuaObject callback) throws LuaException {
        LuaApp.getInstance().regGc(this);
        this.luaManager = luaManager;
        mDelay = delay;
        mCallback = callback;
    }

    public LuaAsyncTask(LuaManagerI luaManager, String src, LuaObject callback) throws LuaException {
        luaManager.regGc(this);
        this.luaManager = luaManager;
        mBuffer = src.getBytes();
        mCallback = callback;
    }

    public LuaAsyncTask(LuaManagerI luaManager, LuaObject func, LuaObject callback) throws LuaException {
        luaManager.regGc(this);
        this.luaManager = luaManager;
        mBuffer = func.dump();
        mCallback = callback;
        LuaState l = func.getLuaState();
        LuaObject g = l.getLuaObject("luajava");
        LuaObject loaded = g.getField("imported");
        if (!loaded.isNil()) {
            loadeds = loaded.asArray();
        }
    }


    public LuaAsyncTask(LuaManagerI luaManager, LuaObject func, LuaObject update, LuaObject callback) throws LuaException {
        this.luaManager = luaManager;
        luaManager.regGc(this);
        mBuffer = func.dump();
        mUpdate = update;
        mCallback = callback;
    }

    @Override
    public void gc() {
        if (getStatus() == Status.RUNNING)
            cancel(true);
    }

    public void execute() throws IllegalArgumentException, ArrayIndexOutOfBoundsException, LuaException {
        super.execute();
    }

    public void update(Object msg) {
        publishProgress(msg);
    }

    public void update(String msg) {
        publishProgress(msg);
    }

    public void update(int msg) {
        publishProgress(msg);
    }

    @Override
    protected Object doInBackground(Object[] args) {
        if (mDelay != 0) {
            try {
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
            }
            return args;
        }
        L = LuaApp.getInstance().getLuaHelper().L;

        try {
            JavaFunction update = new JavaFunction(L) {
                @Override
                public int execute() throws LuaException {
                    update(L.toJavaObject(2));
                    return 0;
                }
            };
            update.register("update");
        } catch (LuaException e) {
            e.printStackTrace();
            luaManager.log("AsyncTask" + e.getMessage());
        }

        if (loadeds != null) {
            LuaObject require = L.getLuaObject("require");
            try {
                require.call("import");
                LuaObject _import = L.getLuaObject("import");
                for (Object s : loadeds)
                    _import.call(s.toString());
            } catch (LuaException e) {
                e.printStackTrace();
            }
        }

        try {
            L.setTop(0);
            int ok = L.LloadBuffer(mBuffer, "LuaAsyncTask");

            if (ok == 0) {
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);
                int l = args.length;
                for (Object arg : args) {
                    L.pushObjectValue(arg);
                }
                ok = L.pcall(l, LuaState.LUA_MULTRET, -2 - l);
                if (ok == 0) {
                    int n = L.getTop() - 1;
                    Object[] ret = new Object[n];
                    for (int i = 0; i < n; i++)
                        ret[i] = L.toJavaObject(i + 2);
                    return ret;
                }
            }
            throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
        } catch (LuaException e) {
            e.printStackTrace();
            luaManager.handleMessage(LuaManagerI.E,"doInBackground" + e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (isCancelled())
            return;
        try {
            if (mCallback != null)
                mCallback.call((Object[]) result);
        } catch (LuaException e) {
            luaManager.handleMessage(LuaManagerI.E,"onPostExecute" + e.toString());
        }
        super.onPostExecute(result);
        if (L != null)
            L.gc(LuaState.LUA_GCCOLLECT, 1);
        System.gc();
        //L.close();
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        // TODO: Implement this method
        try {
            if (mUpdate != null)
                mUpdate.call(values);
        } catch (LuaException e) {
            luaManager.handleMessage(LuaManagerI.E, "onProgressUpdate" + e.getMessage());
        }
        super.onProgressUpdate(values);
    }

}

