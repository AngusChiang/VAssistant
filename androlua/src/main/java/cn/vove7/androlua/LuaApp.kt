package cn.vove7.androlua

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.androlua.luautils.LuaContext
import com.luajava.LuaState
import java.io.File
import java.io.IOException
import java.util.*


/**
 * LuaApp
 *
 *
 * Created by Vove on 2018/7/31
 */
open class LuaApp : Application(), LuaContext {

    private var luaDir: String? = null
    lateinit var luaHelper: LuaHelper

    override fun getLuaState(): LuaState {
        return luaHelper.L
    }

    override fun getWidth(): Int {
        return resources.displayMetrics.widthPixels
    }

    override fun getHeight(): Int {
        return resources.displayMetrics.heightPixels
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //CrashHandler crashHandler = CrashHandler.getInstance();
        //// 注册crashHandler
        //crashHandler.init(getApplicationContext());

        luaDir = filesDir.absolutePath
        luaHelper = LuaHelper(this)

        RemoteDebugServer(this).start()

        initAsset()
    }

    private fun initAsset() {
        arrayOf("import.lua","bridges.lua").forEach {
            val fp = filesDir.absolutePath + '/' + it
            if (!File(fp).exists()) {
                try {
                    LuaUtil.assetsToSD(this, it, fp)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun call(name: String, args: Array<Any>) {}

    override fun set(name: String, `object`: Any) {
        data[name] = `object`
    }


    override fun getGlobalData(): Map<*, *> {
        return data
    }

    override fun get(name: String): Any? {
        return data[name]
    }

    override fun getContext(): Context {
        return this
    }

    override fun sendMsg(msg: String) {
        Log.i("Vove :", "sendMsg  ----> $msg")
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun sendError(title: String, msg: Exception) {

    }

    companion object {

        var instance: LuaApp? = null
            private set
        private val data = HashMap<String, Any>()
    }


}
