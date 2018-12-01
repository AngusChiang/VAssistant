package cn.vove7.androlua

import android.app.Service
import android.content.Context
import cn.vove7.androlua.luautils.LuaContext
import cn.vove7.common.appbus.BusService

/**
 * # LuaService
 *
 * @author Administrator
 * 2018/11/29
 */
abstract class LuaService : BusService(), LuaContext {
    private val data = HashMap<String, Any?>()

    override fun call(func: String?, vararg args: Any?) {
    }

    override fun set(name: String, value: Any?) {
        data[name] = value
    }

    override fun get(name: String?): Any? {
        return data[name]

    }

    override fun getContext(): Context = this

    override fun getWidth(): Int {
        return resources.displayMetrics.widthPixels
    }

    override fun getHeight(): Int {
        return resources.displayMetrics.heightPixels
    }

    override fun getGlobalData(): Map<*, *> {
        return data
    }
}