package cn.vove7.androlua.luabridge;

import androidx.annotation.NonNull;

import cn.vove7.androlua.luautils.LuaGcable;
import cn.vove7.androlua.luautils.LuaRunnableI;
import cn.vove7.androlua.luautils.TimerX;
import com.luajava.LuaException;
import com.luajava.LuaObject;

import cn.vove7.androlua.luautils.LuaManagerI;

/**
 * [定时器]任务
 */
public class LuaTimer extends TimerX implements LuaGcable,LuaRunnableI,Comparable<LuaTimer> {
    @Override
    public int compareTo(@NonNull LuaTimer o) {
        return hashCode()-o.hashCode();
    }
    private LuaTimerTask task;
    private LuaManagerI luaManager;

    public LuaTimer(LuaManagerI luaManager, String src) {
        this(luaManager, src, null);
    }

    public LuaTimer(LuaManagerI luaManager, String src, Object[] arg) {
        super("LuaTimer");
        this.luaManager = luaManager;
        luaManager.regGc(this);
        task = new LuaTimerTask(luaManager, src, arg);
    }

    public LuaTimer(LuaManagerI luaManager, LuaObject func) throws LuaException {
        this(luaManager, func, null);
    }

    public LuaTimer(LuaManagerI luaManager, LuaObject func, Object[] arg) throws LuaException {
        super("LuaTimer");
        this.luaManager = luaManager;
        luaManager.regGc(this);
        task = new LuaTimerTask(luaManager, func, arg);
    }

    @Override
    public void quit() {
        quit(false);
    }

    @Override
    public void quit(boolean self) {
        task.quit(self);
        luaManager.removeGc(this);
    }

    @Override
    public void gc() {
        task.quit(false);
    }

    public void start(long delay, long period) {
        luaManager.log(getName() + "timer start");
        schedule(task, delay, period);
    }

    public void start(long delay) {
        luaManager.log(getName() + " start");
        schedule(task, delay);
    }


    private String getName() {
        return task.getName();
    }

    public boolean isEnabled() {
        return task.isEnabled();
    }

    public boolean getEnabled() {
        return task.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        task.setEnabled(enabled);
    }

    public long getPeriod() {
        return task.getPeriod();
    }

    public void setPeriod(long period) {
        task.setPeriod(period);
    }
}
