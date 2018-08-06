package cn.vove7.executorengine.luaexector

import android.content.Context
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luabridge.LuaFunHelper
import cn.vove7.common.BridgeManager
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.executor.PartialResult
import cn.vove7.executorengine.AbsExecutorImpl
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.v1.OnExecutorResult

/**
 * # LuaExecutor
 *
 * @author Vove
 * 2018/8/4
 */
class LuaExecutor(
        context: Context,
        systemBridge: SystemBridge,
        serviceBridge: ServiceBridge,
        onExecutorResult: OnExecutorResult
) : AbsExecutorImpl(context, systemBridge, serviceBridge, onExecutorResult) {
    private val luaHelper = LuaHelper(context, BridgeManager(this, globalAutomator))
    private val luaFunHelper = LuaFunHelper(luaHelper, luaHelper.L)

    companion object {
        private const val rHeader = "require 'bridges'\n"
    }

    override fun runScript(src: String, voiceArg: String?): PartialResult {
        val script = preRun(src)
        return try {
            if (voiceArg != null)
                luaHelper.evalString(script, arrayOf(voiceArg))
            else
                luaHelper.evalString(script)

            PartialResult(true)
        } catch (e: Exception) {
            luaHelper.handleError(e)
            PartialResult(false, true, e.message ?: "no message")
        }
    }

    private fun preRun(src: String): String {
        return (rHeader + src).replace("require 'checkservice'",
                "require 'checkservice'\nif (not checkservice()) then return end")
    }


}