package cn.vove7.rhino.api;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSFunction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cn.vove7.common.app.GlobalApp;
import cn.vove7.common.app.GlobalLog;
import cn.vove7.common.executor.OnPrint;
import cn.vove7.rhino.common.GcCollector;
import cn.vove7.vtp.asset.AssetHelper;

/**
 * Created by 17719247306 on 2018/8/28
 */
public class RhinoApi extends AbsApi {

    @Override
    protected String[] funs() {
        return new String[]{
                "loadAsset",
                "quit",
                "print",
                "log",
        };
    }

    public static void quit(Context cx, Scriptable thisObj,//global
                            Object[] args, Function funObj) {
        System.err.println("over");
        GcCollector.gc(thisObj);
    }

    /**
     * 从Asset加载
     */

    public static void loadAsset(Context cx, Scriptable thisObj,
                                 Object[] args, Function funObj) {
        for (Object arg : args) {
            String file = Context.toString(arg);
            try {
                String sc = AssetHelper.INSTANCE.getStrFromAsset(GlobalApp.APP, file);
                Log.d("RhinoApi :", "loadAsset  ----> " + file);
                cx.evaluateString(thisObj, sc, "load_" + file, 1, null);
            } catch (Exception ex) {
                // Treat StackOverflow and OutOfMemory as runtime errors
                //ex.printStackTrace();
                onException(ex);
                //String msg = ToolErrorReporter.getMessage(
                //        "msg.uncaughtJSException", ex.toString());
                //throw Context.reportRuntimeError(msg);
            }
        }
    }

    public static void onException(Throwable e) {
        GlobalLog.INSTANCE.err(e);
        notifyOutput(OnPrint.ERROR,e.getMessage());
    }

    private static final Set<OnPrint> printList = new HashSet<>();

    public static void regPrint(OnPrint print) {
        synchronized (printList) {
            printList.add(print);
        }
    }

    public static void unregPrint(OnPrint print) {
        synchronized (printList) {
            printList.remove(print);
        }
    }

    private static void notifyOutput(int l, String o) {
        synchronized (printList) {
            for (OnPrint p : printList) {
                p.onPrint(l, o);
            }
        }
    }

    @JSFunction
    public static void log(Context cx, Scriptable thisObj,
                           Object[] args, Function funObj) {
        GlobalLog.INSTANCE.log(Arrays.toString(args));
    }


    @JSFunction
    public synchronized static void print(Context cx, Scriptable thisObj,
                                          Object[] args, Function funObj) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                builder.append(' ');
            // Convert the arbitrary JavaScript value into a string form.
            String s = Context.toString(args[i]);
            builder.append(s);
        }
        builder.append("\n");
        doLog(builder.toString());
        //Log.d("Vove :", " out ----> " + builder.toString());
    }

    public static void doLog(String m) {
        notifyOutput(OnPrint.LOG, m);
    }
}
