package cn.vove7.androlua

import android.content.Context
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.ThreadPool.runOnPool
import java.io.IOException


/**
 * LuaApp
 *
 *
 * Created by Vove on 2018/7/31
 */
class LuaApp {
    companion object {
        /**
         * 初始化lua资源
         * @param context Context
         */
        fun init(context: Context, update: Boolean) {
            if(!update) return
            runOnPool {
                initAsset(context)
            }
        }

        private fun initAsset(context: Context) {
            context.assets?.list("lua_requires")
                    ?.filter { it.endsWith(".lua") }
                    ?.forEach {
                        val fp = context.filesDir.absolutePath + '/' + it
//                    if (!File(fp).exists() || BuildConfig.DEBUG)
                        try {
                            LuaUtil.assetsToSD(context, "lua_requires/$it", fp)
                        } catch (e: IOException) {
                            GlobalLog.err(e)
                            e.printStackTrace()
                        }
                    }
        }

    }
}