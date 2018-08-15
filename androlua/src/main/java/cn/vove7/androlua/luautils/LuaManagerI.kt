package cn.vove7.androlua.luautils

import android.content.Context
import cn.vove7.common.BridgeManager
import com.luajava.LuaException
import com.luajava.LuaState
import dalvik.system.DexClassLoader
import java.util.*

/**
 * LuaManagerI
 * 负责管理
 *
 *
 * Created by Vove on 2018/8/1
 */
interface LuaManagerI {
    val librarys: HashMap<String, String>
    val luaState: LuaState
    val classLoaders: ArrayList<ClassLoader>

    var bridgeManager: BridgeManager?
    val app: Context

    @Throws(LuaException::class)
    fun loadDex(path: String): DexClassLoader?

    fun regGc(obj: LuaGcable)

    fun gc(obj: LuaGcable)
    fun removeGc(obj: LuaGcable) :Boolean

    fun stop()

    fun handleError(err: String)

    fun handleError(e: Exception)

    fun handleMessage(l: Int, msg: String)

    fun log(log: Any?)

    companion object {

        const val L = 0
        const val W = 1//Prompt
        const val I = 2
        const val E = 3
    }
}
