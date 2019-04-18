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
import cn.vove7.common.interfaces.ScriptEngine
import cn.vove7.common.utils.RegUtils
import cn.vove7.executorengine.ExecutorImpl
import cn.vove7.rhino.RhinoHelper
import cn.vove7.rhino.api.RhinoApi
import java.util.concurrent.ConcurrentSkipListSet

/**
 * # MultiExecutorEngine
 *
 * @author 17719247306
 * 2018/8/28
 */
class ExecutorEngine : ExecutorImpl() {
    private val bridgeManager = BridgeManager(this, GlobalActionExecutor,
            SystemBridge, serviceBridge)


    override fun onRhinoExec(script: String, argMap: Map<String, Any?>?): PartialResult {

        val rhinoHelper = RhinoHelper(bridgeManager)
        engines.add(rhinoHelper)

        val sc = RegUtils.replaceRhinoHeader(script)
        return try {
            rhinoHelper.evalString(sc, argMap)
            RhinoApi.doLog("主线程执行完毕\n")
            PartialResult.success()
        } catch (we: MessageException) {
            GlobalLog.err(we)
            RhinoApi.doLog(we.message)
            PartialResult.fatal(we.message)
        } catch (e: Throwable) {
            GlobalLog.err(e)
            RhinoApi.doLog(e.message)
            PartialResult.fatal(e.message)
        }
    }

    //didn't work fixme
    fun runActionSilent(action: Action, argMap: Map<String, Any?>?) {//静默
        currentAction = action
        runScript(action.actionScript, argMap)
    }

    /**
     * 执行器
     */
    private val engines = ConcurrentSkipListSet<ScriptEngine>()

    //可提取ExecutorHelper 接口 handleMessage
    override fun onLuaExec(script: String, argMap: Map<String, Any?>?): PartialResult {
        val luaHelper = LuaHelper(context, bridgeManager)
        engines.add(luaHelper)
        val newScript = RegUtils.replaceLuaHeader(script)
        return try {
            luaHelper.evalString(newScript, argMap)
            luaHelper.handleMessage(OnPrint.INFO, "主线程执行完毕\n")

            PartialResult.success()
        } catch (me: MessageException) {//异常消息
            luaHelper.handleMessage(OnPrint.ERROR, me.message ?: "")
            PartialResult.fatal(me.message)
        } catch (e: Throwable) {
            luaHelper.handleMessage(OnPrint.ERROR, e.message ?: "")
            GlobalLog.err(e)
            PartialResult.fatal(e.message)
        }
    }


    override fun interrupt() {
        super.interrupt()
        release()
    }

    @Synchronized
    private fun release() {
        engines.forEach {
            it.release()
        }
        engines.clear()
    }

    override fun onFinish(result: Boolean?) {
        super.onFinish(result)
        release()
    }
}