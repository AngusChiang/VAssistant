package cn.vove7.androlua.luautils

import android.util.Log
import cn.vove7.common.executor.OnPrint
import cn.vove7.vtp.log.Vog

import com.luajava.JavaFunction
import com.luajava.LuaException
import com.luajava.LuaState
import java.util.*

/**
 * LuaPrinter
 *
 *
 * Created by Vove on 2018/7/31
 */
class LuaPrinter @JvmOverloads constructor(Ls: LuaState, private val print: OnPrint? = null) : JavaFunction(Ls) {
    private val output = StringBuffer()

    init {
        try {
            register("print")
        } catch (e: LuaException) {
            Log.e("Vove :", "LuaPrinter  ----> register failed!")
            e.printStackTrace()
        }

    }

    @Throws(LuaException::class)
    override fun execute(): Int {
        if (L.top < 2) {
            return 0
        }
        for (i in 2..L.top) {
            val type = L.type(i)
            var value: String?
            val stype = L.typeName(type)
            value = when (stype) {
                "userdata" -> {
                    val obj = L.toJavaObject(i)
                    if (obj != null) {
                        if (obj::class.java.isArray) {
                            Arrays.toString(obj as Array<*>)
                        } else obj.toString()
                    } else {
                        "nil"
                    }
                }
                "boolean" -> if (L.toBoolean(i)) "true" else "false"
                else -> L.toString(i)
            }
            if (value == null)
                value = stype
            output.append(value)
            output.append("  ")
//            output.append("\t")
        }
        output.append('\n')
        print?.onPrint(OnPrint.LOG, output.toString())
        Vog.i(output)
        output.setLength(0)
        return 0
    }

}