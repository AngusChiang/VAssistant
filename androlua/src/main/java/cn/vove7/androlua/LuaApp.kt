package cn.vove7.androlua

import android.app.Application
import cn.vove7.androlua.luabridge.LuaUtil
import java.io.File
import java.io.IOException


/**
 * LuaApp
 *
 *
 * Created by Vove on 2018/7/31
 */
open class LuaApp : Application() {

    private var luaDir: String? = null
//    lateinit var luaHelper: LuaHelper
//
//    override fun getLuaState(): LuaState {
//        return luaHelper.L
//    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //CrashHandler crashHandler = CrashHandler.getInstance();
        //// 注册crashHandler
        //crashHandler.init(getApplicationContext());

        luaDir = filesDir.absolutePath
//        luaHelper = LuaHelper(this)

        RemoteDebugServer(this).start()

        initAsset()
    }

    private fun initAsset() {
        assets.list("").forEach {
            if (assets.list(it).isNotEmpty()) return@forEach
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


    companion object {

        lateinit var instance: LuaApp
            private set
    }


}
