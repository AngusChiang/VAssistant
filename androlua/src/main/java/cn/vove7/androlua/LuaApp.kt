package cn.vove7.androlua

import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalApp
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread


/**
 * LuaApp
 *
 *
 * Created by Vove on 2018/7/31
 */
open class LuaApp : GlobalApp() {

    private var luaDir: String? = null
//    lateinit var luaHelper: LuaHelper
//
//    override fun getLuaState(): LuaState {
//        return luaHelper.L
//    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        luaDir = filesDir.absolutePath
        thread {
            initAsset()
        }
    }

    private fun initAsset() {
        assets.list("lua_requires")
                .filter { it.endsWith(".lua") }
                .forEach {
                    val fp = filesDir.absolutePath + '/' + it
//                    if (!File(fp).exists()) {
                        try {
                            LuaUtil.assetsToSD(this, "lua_requires/$it", fp)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
//                    }
                }
    }


    companion object {

        lateinit var instance: LuaApp
            private set
    }


}
