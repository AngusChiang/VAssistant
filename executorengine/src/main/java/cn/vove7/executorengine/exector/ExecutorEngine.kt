package cn.vove7.executorengine.exector

import cn.vove7.androlua.LuaHelper
import cn.vove7.common.BridgeManager
import cn.vove7.common.MessageException
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
class ExecutorEngine : ExecutorImpl() {
    private val bridgeManager = BridgeManager(this, GlobalActionExecutor,
            SystemBridge, serviceBridge)

    private var rhinoHelper: RhinoHelper? = null

    override fun onRhinoExec(script: String, argMap: Map<String, Any?>?): PartialResult {
        rhinoHelper?.stop()
        if (currentActionIndex <= 1) {
            rhinoHelper = RhinoHelper(bridgeManager)
        }
        val sc = RegUtils.replaceRhinoHeader(script)
        return try {
            rhinoHelper?.evalString(sc, argMap)
            RhinoApi.doLog("主线程执行完毕\n")
            PartialResult.success()
        } catch (we: MessageException) {
            GlobalLog.err(we)
            PartialResult.fatal(we.message)
        }
    }

    //didn't work fixme
    fun runActionSilent(action: Action, argMap: Map<String, Any?>?) {//静默
        currentAction = action
        runScript(action.actionScript, argMap)
    }

    /**
     * Lua Impl
     */
    private var luaHelper: LuaHelper? = null

    //可提取ExecutorHelper 接口 handleMessage
    override fun onLuaExec(script: String, argMap: Map<String, Any?>?): PartialResult {
//        if (currentActionIndex <= 1) {//fixme ?????
        luaHelper = LuaHelper(context, bridgeManager)
//        }
        val newScript = RegUtils.replaceLuaHeader(script)
        return try {
            luaHelper?.evalString(newScript, argMap)
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
        rhinoHelper = null
        luaHelper = null
    }
}