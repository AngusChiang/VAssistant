package cn.vove7.executorengine.exector

import android.content.Context
import cn.vove7.androlua.LuaHelper
import cn.vove7.common.BridgeManager
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.executor.OnPrint
import cn.vove7.common.executor.PartialResult
import cn.vove7.executorengine.ExecutorImpl
import cn.vove7.rhino.RhinoHelper
import cn.vove7.rhino.api.RhinoApi
import cn.vove7.vtp.log.Vog

/**
 * # MultiExecutorEngine
 *
 * @author 17719247306
 * 2018/8/28
 */
class MultiExecutorEngine(
        context: Context,
        serviceBridge: ServiceBridge
) : ExecutorImpl(context, serviceBridge) {
    private val bridgeManager = BridgeManager(this, globalActionExecutor, systemBridge, serviceBridge)

    /**
     * Rhino impl
     */
//    private var tHandler: HandlerThread? = null
    private var rhinoHelper: RhinoHelper? = null
    override fun onRhinoExec(script: String, arg: String?): PartialResult {
//        if (tHandler?.isAlive == true) {
//            tHandler!!.interrupt()
//        }

//        tHandler = HandlerThread("run")
//        tHandler!!.start()
//        val runHandler = Handler(tHandler!!.looper)

//        runHandler.post {
        if (rhinoHelper != null) {
            rhinoHelper?.stop()
        }
        if (currentActionIndex == 1) {
            rhinoHelper = RhinoHelper(bridgeManager)
        }
        rhinoHelper!!.evalString(script, arg)
        RhinoApi.doLog("主线程执行完毕\n")
//        }
        return PartialResult.success()
    }

    /**
     * Lua Impl
     */
    private var luaHelper: LuaHelper? = null
//    private val luaFunHelper = LuaFunHelper(luaHelper, luaHelper.L)

    companion object {
        private const val rHeader = "require 'bridges'\nlocal args = { ... }\n"
    }

    //可提取ExecutorHelper 接口 handleMessage
    override fun onLuaExec(src: String, arg: String?): PartialResult {
//        if (luaHelper == null) {
        luaHelper = LuaHelper(context, bridgeManager)
//        }
        val script = preRun(src)
        return try {
            if (arg != null) {
                Vog.d(this, "runScript arg : $arg")
                luaHelper!!.evalString(script, arrayOf(arg))
            } else
                luaHelper!!.evalString(script)

            luaHelper!!.handleMessage(OnPrint.INFO, "主线程执行完毕\n")
            PartialResult.success()
        } catch (e: Exception) {
            e.printStackTrace()
            luaHelper!!.handleError(e)
            PartialResult.fatal(e.message ?: "no message")
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
        rhinoHelper?.stop()
        luaHelper?.stop()
    }
}