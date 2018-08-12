package cn.vove7.common.executor

import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.view.notifier.ActivityShowListener
import cn.vove7.common.view.notifier.ViewShowListener
import cn.vove7.common.viewnode.ViewNode
import cn.vove7.datamanager.parse.model.Action
import java.util.*

/**
 *
 *
 */
interface CExecutorI : ViewShowListener, ActivityShowListener {
    fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>)
    fun interrupt()
    fun runScript(script: String, voiceArg: String? = null): PartialResult

    fun checkAccessibilityService(jump: Boolean = true): Boolean

    fun alert(title: String, msg: String): Boolean
    /**
     * 等待单选结果
     * @return if 调用成功 ChoiceData else null
     */
    fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): ChoiceData?

    /**
     * 等待语音参数
     * @return 语音参数 ,null if failed
     */
    fun waitForVoiceParam(askWord: String? = null): String?

    fun onGetVoiceParam(param: String?)

    fun waitForApp(pkg: String, activityName: String? = null): Boolean
    fun waitForViewId(id: String): ViewNode?
    fun waitForDesc(desc: String): ViewNode?
    fun waitForText(text: String): ViewNode?
    fun getViewNode(): ViewNode?

    fun waitForUnlock(millis: Long = -1)
    fun notifySync()
    fun sleep(millis: Long)
    fun onFinish()

    fun smartOpen(data: String): Boolean
    /**
     * 语音合成
     */
    fun speak(text: String)

    fun speakSync(text: String): Boolean
    fun speakCallback(result: String? = null)
}