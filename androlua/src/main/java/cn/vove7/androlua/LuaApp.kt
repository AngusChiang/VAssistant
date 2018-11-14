package cn.vove7.androlua

import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.ThreadPool.runOnPool
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

    override fun onCreate() {
        super.onCreate()
        runOnPool {
            initAsset()
        }
    }

    private fun initAsset() {
        assets.list("lua_requires")
                ?.filter { it.endsWith(".lua") }
                ?.forEach {
                    val fp = filesDir.absolutePath + '/' + it
//                    if (!File(fp).exists() || BuildConfig.DEBUG)
                        try {
                            LuaUtil.assetsToSD(this, "lua_requires/$it", fp)
                        } catch (e: IOException) {
                            GlobalLog.err(e)
                            e.printStackTrace()
                        }
                }
    }

}
