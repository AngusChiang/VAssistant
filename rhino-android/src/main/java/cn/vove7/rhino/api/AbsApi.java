package cn.vove7.rhino.api;

import org.mozilla.javascript.ScriptableObject;

/**
 * Created by Vove on 2018/8/28
 */
public abstract class AbsApi {

    protected abstract String[] funs();
    public void define(ScriptableObject global) {
        global.defineFunctionProperties(funs(), this.getClass(),
                ScriptableObject.DONTENUM);
    }
}
