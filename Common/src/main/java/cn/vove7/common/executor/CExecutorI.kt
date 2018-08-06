package cn.vove7.common.executor

import cn.vove7.common.view.finder.ViewShowListener
import cn.vove7.datamanager.parse.model.Action
import java.util.*

/**
 *
 *
 */
interface CExecutorI : ViewShowListener {
    fun execQueue(actionQueue: PriorityQueue<Action>)
    fun stop()
    fun runScript(script: String, voiceArg: String? = null): PartialResult

    fun checkAccessibilityService(jump: Boolean = true): PartialResult

    fun waitForVoiceParam(action: Action): PartialResult
    fun waitForAlertResult(msg: String): PartialResult
    fun waitForApp(pkg: String, activityName: String? = null): PartialResult
    fun waitForViewId(id: String): PartialResult
    fun waitForDesc(desc: String): PartialResult
    fun waitForText(text: String): PartialResult
    fun notifySync()
    fun sleep(millis: Long)
    fun onGetVoiceParam(param: String?)
    fun waitForUnlock(millis: Long = -1): Boolean
    fun openSomething(data: String): PartialResult
}