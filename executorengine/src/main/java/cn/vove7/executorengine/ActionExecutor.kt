package cn.vove7.executorengine

import cn.vove7.executorengine.bridge.AccessibilityBridge
import cn.vove7.executorengine.bridge.SpeechBridge
import cn.vove7.executorengine.bridge.SystemBridge
import cn.vove7.executorengine.model.PartialResult
import cn.vove7.parseengine.model.Action
import cn.vove7.vtp.log.Vog
import java.util.*
import kotlin.concurrent.thread

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
class ActionExecutor(
        getAccessibilityBridge: GetAccessibilityBridge,
        private val systemBridge: SystemBridge,
        private val speechBridge: SpeechBridge,
        val onExecutorResult: OnExecutorResult
) : Executor(getAccessibilityBridge, systemBridge, speechBridge) {
    var bridge: AccessibilityBridge? = null

    private lateinit var actionQueue: PriorityQueue<Action>
    private var thread: Thread? = null

    private fun runnable() {
        var result = "执行成功"
        while (actionQueue.isNotEmpty()) {
            val action = actionQueue.poll()
            val sin = Scanner(action.actionScript)
            var line: String
            var partialResult: PartialResult
            while (sin.hasNext()) {
                line = sin.nextLine()
                partialResult = execAction(line, action)
                when {
                    partialResult.needTerminal -> {//出错
                        val msg = "执行出错 on $line"
                        Vog.d(this, msg)
                        onExecutorResult.onExecutorFailed(msg)
                        return
                    }
                    !partialResult.isSuccess -> {
                        result = "中途执行失败"
                        Vog.d(this, result)
                    }
                }
            }
        }
        onExecutorResult.onExecutorSuccess(result)
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
            lock = null
        }
    }

    fun resume() {
        if (lock != null)
            synchronized(lock!!) {
                lock!!.notify()
            }
    }

    /**
     * 检查无障碍服务
     */
    private fun bridgeIsAvailable(): Boolean {
        if (bridge == null) {
            bridge = getAccessibilityBridge.getBridge()
            return bridge != null
        }
        return true

    }


    /**
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
                val res = openSomething(action, p)
                return if (res.repeat)
                    execAction(cmd, action)
                else res
            }
            ACTION_CLICK_TEXT -> {//
                if (!bridgeIsAvailable())
                    return PartialResult(false, true, "无障碍未开启")


            }
            ACTION_CLICK_ID -> {
                if (!bridgeIsAvailable())
                    return PartialResult(false, true, "无障碍未开启")


            }
            ACTION_BACK -> {
                if (!bridgeIsAvailable())
                    return PartialResult(false, true, "无障碍未开启")

                getAccessibilityBridge.getBridge()?.back()
            }
            ACTION_RECENT -> {
                if (!bridgeIsAvailable())
                    return PartialResult(false, true, "无障碍未开启")


            }
            ACTION_PULL_NOTIFICATION -> {
                if (!bridgeIsAvailable())
                    return PartialResult(false, true, "无障碍未开启")
            }
            ACTION_CALL -> {
                if (p != null && p != "") {
                    return systemBridge.call(p)
                }
            }
        }
        return PartialResult(false)
    }

    companion object {
        const val ACTION_OPEN = "open"
        const val ACTION_CLICK_TEXT = "clickByText"
        const val ACTION_CLICK_ID = "clickById"
        const val ACTION_BACK = "back"
        const val ACTION_RECENT = "recent"
        const val ACTION_PULL_NOTIFICATION = "pullNotification"
        const val ACTION_CALL = "call"
    }

}

interface GetAccessibilityBridge {
    fun getBridge(): AccessibilityBridge?
}

interface OnExecutorResult {
    fun onExecutorSuccess(result: String)
    fun onExecutorFailed(errMsg: String)
}