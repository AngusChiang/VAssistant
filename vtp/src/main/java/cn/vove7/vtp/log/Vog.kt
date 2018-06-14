package cn.vove7.vtp.log

import android.util.Log

/**
 * #Vog
 *
 */
object Vog {
    private const val v = 0
    private const val d = 1
    private const val i = 2
    private const val w = 3
    private const val e = 4
    private const val a = 5
    private var output_level = v

    private var sendToServer = false

    private var logServer: String = ""

    
    fun send2Server(msg: String) {

    }

    fun init(l: Int, sendToServer: Boolean) {
        output_level = l
        this.sendToServer = sendToServer
    }

    fun d(o: Any, msg: String) {
        if (output_level <= d) {
            Log.d(o.javaClass.simpleName, "  ----> $msg")
        }
    }

    fun wtf(o: Any, msg: String) {
        if (output_level <= d) {
            Log.wtf(o.javaClass.simpleName, "  ----> $msg")
        }
    }

    fun v(o: Any, msg: Any) {
        if (output_level <= v) {
            Log.v(o.javaClass.simpleName, "  ----> $msg")
        }
    }

    fun i(o: Any, msg: Any) {
        if (output_level <= i) {
            Log.i(o.javaClass.simpleName, "  ----> $msg")
        }
    }

    fun w(o: Any, msg: Any) {
        if (output_level <= w) {
            Log.w(o.javaClass.simpleName, "  ----> $msg")
        }
    }

    fun e(o: Any, msg: Any) {
        if (output_level <= e) {
            Log.e(o.javaClass.simpleName, "  ----> $msg")
        }
    }

    fun a(o: Any, msg: Any) {
        if (output_level <= a) {
            Log.wtf(o.javaClass.simpleName, "  ----> $msg")
        }
    }
}
