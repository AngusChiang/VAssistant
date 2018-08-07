package cn.vove7.common.executor

import cn.vove7.common.view.finder.ActivityShowListener
import cn.vove7.common.view.finder.ViewShowListener
import cn.vove7.common.viewnode.ViewNode
import cn.vove7.datamanager.parse.model.Action
import java.util.*

/**
 *
 *
 */
interface CExecutorI : ViewShowListener, ActivityShowListener {
    fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>)
    fun stop()
    fun runScript(script: String, voiceArg: String? = null): PartialResult

    fun checkAccessibilityService(jump: Boolean = true): PartialResult

    fun getViewNode(): ViewNode
    fun alert(title: String, msg: String): Boolean
    /**
     * 等待语音参数
     * @return 语音参数
     */
    fun waitForVoiceParam(askWord: String? = null): String?

    fun waitForApp(pkg: String, activityName: String? = null): PartialResult
    fun waitForViewId(id: String): ViewNode?
    fun waitForDesc(desc: String): ViewNode?
    fun waitForText(text: String): ViewNode?
    fun openSomething(data: String): PartialResult
    fun notifySync()
    fun sleep(millis: Long)
    fun onGetVoiceParam(param: String?)
    fun waitForUnlock(millis: Long = -1): Boolean
    fun onFinish()
}