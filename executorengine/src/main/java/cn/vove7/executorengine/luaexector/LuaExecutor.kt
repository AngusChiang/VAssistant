package cn.vove7.executorengine.luaexector

import android.content.Context
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.common.BridgeManager
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.executor.OnExecutorResult
import cn.vove7.common.executor.PartialResult
import cn.vove7.executorengine.AbsExecutorImpl
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.vtp.log.Vog

/**
 * # LuaExecutor
 *
 * @author Vove
 * 2018/8/4
 */
class LuaExecutor(
        context: Context,
        serviceBridge: ServiceBridge,
        onExecutorResult: OnExecutorResult
) : AbsExecutorImpl(context, serviceBridge, onExecutorResult) {
    private val luaHelper = LuaHelper(context, BridgeManager(this, globalAutomator, systemBridge, onExecutorResult))
//    private val luaFunHelper = LuaFunHelper(luaHelper, luaHelper.L)

    companion object {
        private const val rHeader = "require 'bridges'\nlocal args = { ... }\n"
    }

    override fun runScript(src: String, voiceArg: String?): PartialResult {
        val script = preRun(src)
        return try {
            if (voiceArg != null) {
                Vog.d(this, "runScript voiceArg : $voiceArg")
                luaHelper.evalString(script, arrayOf(voiceArg))
            } else
                luaHelper.evalString(script)

            luaHelper.handleMessage(LuaManagerI.I,"主线程执行完毕\n")
            PartialResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            luaHelper.handleError(e)
            PartialResult(false, true, e.message ?: "no message")
        }
    }

    private fun preRun(src: String): String {
        return (rHeader + src.replace("'accessibility'",
                "'accessibility' if (not accessibility()) then return end")
                ).also {
            Vog.v(this, "preRun $it")
        }
    }

    override fun interrupt() {
        super.interrupt()
        luaHelper.stop()
    }
}