package cn.vove7.executorengine.exector

import cn.vove7.androlua.LuaHelper
import cn.vove7.common.BridgeManager
import cn.vove7.common.MessageException
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.GlobalActionExecutor
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.executor.OnPrint
import cn.vove7.common.executor.PartialResult
import cn.vove7.common.utils.RegUtils
import cn.vove7.executorengine.ExecutorImpl
import cn.vove7.rhino.RhinoHelper
import cn.vove7.rhino.api.RhinoApi

/**
 * # MultiExecutorEngine
 *
 * @author 17719247306
 * 2018/8/28
 */
class MultiExecutorEngine : ExecutorImpl() {
    private val bridgeManager = BridgeManager(this, GlobalActionExecutor,
            SystemBridge, serviceBridge)

    /**
     * Rhino impl
     */
//    private var tHandler: HandlerThread? = null
    private var rhinoHelper: RhinoHelper? = null

    override fun onRhinoExec(script: String, args: Array<String>?): PartialResult {

        rhinoHelper?.stop()
        if (currentActionIndex <= 1) {
            rhinoHelper = RhinoHelper(bridgeManager)
        }
        val sc = RegUtils.replaceRhinoHeader(script)
        return try {
            rhinoHelper?.evalString(sc, *(args ?: arrayOf())) ?: GlobalApp.toastWarning("执行器未就绪")
            RhinoApi.doLog("主线程执行完毕\n")
            PartialResult.success()
        } catch (e: Exception) {
            GlobalLog.err(e)
            RhinoApi.doLog(e.message)
            PartialResult.fatal(e.message ?: "no message")
        }
    }

    //didn't work fixme
    fun runActionSilent(action: Action, args: Array<String>? = null) {//静默
        currentAction = action
        runScript(action.actionScript, args)
    }

    /**
     * Lua Impl
     */
    private var luaHelper: LuaHelper? = null
//    private val luaFunHelper = LuaFunHelper(luaHelper, luaHelper.L)

    //可提取ExecutorHelper 接口 handleMessage
    override fun onLuaExec(script: String, args: Array<String>?): PartialResult {
//        if (currentActionIndex <= 1) {//fixme ?????
        luaHelper = LuaHelper(context, bridgeManager)
//        }
        val newScript = RegUtils.replaceLuaHeader(script)
        return try {
            luaHelper?.evalString(newScript, args)
            luaHelper?.handleMessage(OnPrint.INFO, "主线程执行完毕\n")
            PartialResult.success()

        } catch (me: MessageException) {//异常消息
            PartialResult.fatal(me.message)
        } catch (e: Throwable) {
            GlobalLog.err(e)
            PartialResult.fatal(e.message)
        }
    }


    override fun interrupt() {
        super.interrupt()
        rhinoHelper?.stop()
        luaHelper?.stop()
    }
}