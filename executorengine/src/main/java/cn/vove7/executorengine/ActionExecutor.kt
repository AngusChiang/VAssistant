package cn.vove7.executorengine

import android.content.Context
import cn.vove7.appbus.Bus
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.bridge.AccessibilityBridge
import cn.vove7.executorengine.bridge.ServiceBridge
import cn.vove7.executorengine.bridge.SystemBridge
import cn.vove7.executorengine.model.PartialResult
import cn.vove7.executorengine.model.RequestPermission
import cn.vove7.vtp.log.Vog
import java.util.*
import kotlin.concurrent.thread

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
class ActionExecutor(
        context: Context,
        getAccessibilityBridge: GetAccessibilityBridge,
        private val systemBridge: SystemBridge,
        serviceBridge: ServiceBridge,
        private val onExecutorResult: OnExecutorResult
) : Executor(context, getAccessibilityBridge, systemBridge, serviceBridge) {

    private lateinit var actionQueue: PriorityQueue<Action>
    private var thread: Thread? = null

    private fun runnable() {
        var execLog = ""
        while (actionQueue.isNotEmpty()) {
            currentAction = actionQueue.poll()
            val sin = Scanner(currentAction!!.actionScript)
            var line: String
            var partialResult: PartialResult
            while (sin.hasNext()) {
                line = sin.nextLine()
                partialResult = execAction(line, currentAction!!)
                when {
                    partialResult.needTerminal -> {//出错
                        val msg = "执行终止-- on $line ${partialResult.msg}"
                        Vog.d(this, msg)
                        currentAction = null
                        onExecutorResult.onExecutorFailed(msg)
                        return
                    }
                    !partialResult.isSuccess -> {
                        execLog += "失败-- on $line ${partialResult.msg}\n"
                        Vog.d(this, execLog)
                    }
                }
            }
        }
        currentAction = null
        onExecutorResult.onExecutorSuccess("执行完毕 - $execLog")
    }


    fun exec(actionQueue: PriorityQueue<Action>) {
        this.actionQueue = actionQueue
        if (thread != null && thread!!.isAlive) {
            thread!!.interrupt()
        }
        lock = Object()
        thread = thread(start = true, isDaemon = true, priority = Thread.MAX_PRIORITY) {
            runnable()
        }
    }

    fun interrupt() {
        if (thread != null) {
            thread!!.interrupt()
        }
    }

    /**
     * 检查无障碍服务
     */
    private fun bridgeIsAvailable(): Boolean {
        if (accessBridge == null) {
            accessBridge = getAccessibilityBridge.getBridge()
            return accessBridge != null
        }
        return true
    }

    /**
     * 执行动作
     * @return 执行结果,
     */
    private fun execAction(cmd: String, action: Action): PartialResult {
        val cs = cmd.split(':')
        val c = cs[0]

        //优先带参数
        val p = if (cs.size > 1) {
            cs[cs.size - 1]
        } else {
            action.param?.value
        }

        when (c) {
            ACTION_OPEN -> {//打开应用/其他额外
                return if (checkParam(p)) openSomething(p!!)
                else {
                    if (waitForParam(action).isSuccess)
                        execAction(cmd, action)
                    else PartialResult(false, true, "无参数")
                }
            }
            ACTION_CLICK_TEXT -> {//
                if (!checkAccessibilityService().isSuccess)
                    return PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)

                return if (checkParam(p)) clickByText(p!!)
                else {
                    if (waitForParam(action).isSuccess)
                        execAction(cmd, action)
                    else PartialResult(false, true, "无参数")
                }
            }
            ACTION_CLICK_ID -> {
                if (!checkAccessibilityService().isSuccess)
                    return PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)
                return if (checkParam(p)) clickById(p!!)
                else {
                    if (waitForParam(action).isSuccess)
                        execAction(cmd, action)
                    else PartialResult(false, true, "无参数")
                }

            }
            ACTION_BACK -> {
                return if (!bridgeIsAvailable())
                    PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)
                else PartialResult(pressBack())
            }
            ACTION_HOME -> {
                return if (!bridgeIsAvailable())
                    PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)
                else PartialResult(goHome())
            }
            ACTION_RECENT -> {
                return if (!bridgeIsAvailable())
                    PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)
                else PartialResult(openRecent())
            }
            ACTION_PULL_NOTIFICATION -> {
                return if (!bridgeIsAvailable())
                    PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)
                else PartialResult(accessBridge?.showNotification() ?: false)
            }
            ACTION_CALL -> {
                return if (checkParam(p)) callPhone(p!!)
                else {
                    if (waitForParam(action).isSuccess)
                        execAction(cmd, action)
                    else PartialResult(false, true, "无参数")
                }
            }
        }
        Vog.d(this, "未知操作")
        //调用聊天
        return PartialResult(false)
    }

    private fun checkAccessibilityService(): PartialResult {
        return if (!bridgeIsAvailable()) {
            Bus.post(RequestPermission("无障碍服务"))
            PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)
        } else PartialResult(true)
    }

    companion object {
        const val ACTION_OPEN = "open"
        const val ACTION_CLICK_TEXT = "clickByText"
        const val ACTION_CLICK_ID = "clickById"
        const val ACTION_BACK = "back"
        const val ACTION_HOME = "home"
        const val ACTION_RECENT = "recent"
        const val ACTION_PULL_NOTIFICATION = "pullNotification"
        const val ACTION_CALL = "call"
        const val ACCESSIBILITY_DONT_OPEN = "无障碍未开启"

    }

}

interface GetAccessibilityBridge {
    fun getBridge(): AccessibilityBridge?
}

interface OnExecutorResult {
    fun onExecutorSuccess(result: String)
    fun onExecutorFailed(errMsg: String)
}
