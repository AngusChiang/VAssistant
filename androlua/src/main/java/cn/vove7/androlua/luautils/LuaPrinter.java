package cn.vove7.androlua.luautils;

import android.util.Log;

import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

/**
 * LuaPrinter
 * <p>
 * Created by Vove on 2018/7/31
 */
public class LuaPrinter extends JavaFunction {

    private LuaState L;
    private StringBuffer output = new StringBuffer();
    private OnPrint print;

    public LuaPrinter(LuaState L, OnPrint print) {
        super(L);
        this.print = print;
        this.L = L;
        try {
            register("print");
        } catch (LuaException e) {
            Log.e("Vove :", "LuaPrinter  ----> register failed!");
            e.printStackTrace();
        }
    }

    public LuaPrinter(LuaState L) {
        this(L, null);
    }

    @Override
    public int execute() throws LuaException {
        if (L.getTop() < 2) {
            return 0;
        }
        for (int i = 2; i <= L.getTop(); i++) {
            int type = L.type(i);
            String val = null;
            String stype = L.typeName(type);
            switch (stype) {
                case "userdata":
                    Object obj = L.toJavaObject(i);
                    if (obj != null)
                        val = obj.toString();
                    break;
                case "boolean":
                    val = L.toBoolean(i) ? "true" : "false";
                    break;
                default:
                    val = L.toString(i);
                    break;
            }
            if (val == null)
                val = stype;
            output.append("\t");
            output.append(val);
            output.append("\t");
        }
        output.append('\n');
        if (print != null) {
            print.onPrint(LuaManagerI.L,output.toString());
        }
        Log.i("Vove :", "execute  ----> " + output);
        output.setLength(0);
        return 0;
    }

    public interface OnPrint {
        void onPrint(int l, String output);
    }
}

