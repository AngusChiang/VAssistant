package com.luajava;

public class LuaFunction<T> extends LuaObject implements LuaMetaTable {

    protected LuaFunction(LuaState L, String globalName) {
        super(L, globalName);
    }

    protected LuaFunction(LuaState L, int index) {
        super(L, index);
    }

    @Override
    public T __call(Object[] arg) throws LuaException {
        return (T) super.call(arg);
    }

    @Override
    public Object __index(String key) {
        return null;
    }

    @Override
    public void __newIndex(String key, Object value) {
    }

    @Override
    public T call(Object... args) throws LuaException {
        return (T) super.call(args);
    }
}
