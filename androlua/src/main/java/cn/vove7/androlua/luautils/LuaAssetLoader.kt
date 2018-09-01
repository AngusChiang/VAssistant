package cn.vove7.androlua.luautils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log

import com.luajava.JavaFunction
import com.luajava.LuaState

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream

import cn.vove7.vtp.log.Vog

import cn.vove7.androlua.luabridge.LuaUtil.readAll

/**
 * LuaAssetLoader
 *
 *
 * Created by Vove on 2018/7/31
 */
class LuaAssetLoader(private val context: Context, L: LuaState) : JavaFunction(L) {

    override fun execute(): Int {
        val name = L.toString(-1)

        val am = context.assets
        return try {
            Vog.d(this, "assetLoader  ----> $name")
            val `is` = am.open("lua_requires/$name.lua")
            val bytes = readAll(`is`)
            L.LloadBuffer(bytes, name)
            1
        } catch (e: Exception) {
            val os = ByteArrayOutputStream()
            e.printStackTrace(PrintStream(os))
            L.pushString("Cannot load module " + name + ":\n" + os.toString())
            1
        }

    }
}
