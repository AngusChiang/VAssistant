package cn.vove7.androlua

import cn.vove7.androlua.luabridge.LuaUtil
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vassistant.plugininterface.app.GlobalLog
import cn.vove7.common.utils.ThreadPool.runOnPool
import java.io.IOException


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
