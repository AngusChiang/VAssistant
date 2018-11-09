package cn.vove7.common.executor

import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.interfaces.SpeakCallback
import cn.vove7.common.view.notifier.ActivityShowListener
import cn.vove7.common.view.notifier.ViewShowListener
import java.util.*

/**
 *
 *
 */
interface CExecutorI : ViewShowListener, ActivityShowListener, RuntimeArgs, SpeakCallback {
    companion object {
        val DEBUG_SCRIPT = "DEBUG"
    }

    fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>?)
    fun interrupt()
    fun runScript(script: String, args: Array<String>? = null): PartialResult
    fun setScreenSize(width: Int, height: Int)

    fun checkAccessibilityService(jump: Boolean = true): Boolean

    fun alert(title: String, msg: String): Boolean
    /**
     * 等待单选结果
     * @return if 调用成功 ChoiceData else null
     */
    fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): ChoiceData?

    //Api使用
    fun singleChoiceDialog(askTitle: String, choiceData: Array<String>): Int?
//    fun multiChoiceDialog(askTitle: String, choiceData: Array<String>)

    /**
     * 等待语音参数
     * @return 语音参数 ,null if failed
     */
    fun waitForVoiceParam(askWord: String? = null): String?


    fun onGetVoiceParam(param: String?)

    fun waitForApp(pkg: String, activityName: String? = null, m: Long = -1): Boolean
    fun waitForViewId(id: String, m: Long = -1): ViewNode?
    fun waitForDesc(desc: String, m: Long = -1): ViewNode?
    fun waitForText(text: String, m: Long = -1): ViewNode?
    fun waitForText(text: Array<String>, m: Long = -1): ViewNode?
    //返回
    fun getViewNode(): ViewNode?

    /**
     * @return true 等待成功 ; false 失败(超时,停止
     */
    fun waitForUnlock(millis: Long = -1): Boolean

    fun notifySync()
    fun sleep(millis: Long)
    fun onFinish(result: Boolean)

    fun smartOpen(data: String): Boolean
    fun smartClose(data: String): Boolean
    /**
     * 语音合成
     * 异步
     */
    fun speak(text: String)

    //同步
    fun speakSync(text: String): Boolean

}

interface RuntimeArgs {
    var DEBUG: Boolean
    var userInterrupted: Boolean//用户中断
    var command: String?
    //Runtime
    var currentActionIndex: Int
    var actionCount: Int

    val focusView: ViewNode?
    var currentApp: ActionScope?
    var currentActivity: String?

    var actionScope: Int?

    fun isGlobal(): Boolean
    var running: Boolean

    /**
     * 打开 1
     * 关闭-1
     */
    var commandType: Int
}