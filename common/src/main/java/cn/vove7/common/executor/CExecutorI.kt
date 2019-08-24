package cn.vove7.common.executor

import cn.vove7.common.NotSupportException
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.interfaces.SpeakCallback
import cn.vove7.common.view.notifier.ActivityShowListener
import java.util.*

/**
 *
 *
 */
interface CExecutorI : ActivityShowListener, RuntimeArgs, SpeakCallback {
    companion object {
        const val DEBUG_SCRIPT = "DEBUG"
        const val EXEC_CODE_SUCCESS = 0
        const val EXEC_CODE_FAILED = -1
        const val EXEC_CODE_NOT_SUPPORT = -2
        const val EXEC_CODE_INTERRUPT = -3
        const val EXEC_CODE_NOT_FINISH = -4
        const val EXEC_CODE_EMPTY_QUEUE = -5
        const val EXEC_CODE_REQUIRE = -6
    }

    /**
     * 脚本标记需要使用无障碍
     * 若未开启，抛出异常终止
     */
    @Throws
    fun requireAccessibility()

    /**
     * 等待无障碍开启
     * @return Boolean 是否开启
     */
    fun waitAccessibility(waitMillis: Long): Boolean

    fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>, sync: Boolean = true): Int
    fun interrupt()
    fun runScript(script: String, args: Array<String>? = null): PartialResult = PartialResult(false)

    fun runScript(script: String, argMap: Map<String, Any?>? = null): Pair<Int, String?>
    fun setScreenSize(width: Int, height: Int)

    fun executeFailed(msg: String?)

    fun checkAccessibilityService(jump: Boolean = true): Boolean

    fun alert(title: String, msg: String): Boolean

    fun notifyAlertResult(result: Boolean)

    /**
     * 等待单选结果
     * @return if 调用成功 ChoiceData else null
     */
    fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): ChoiceData?

    fun onSingleChoiceResult(index: Int, data: ChoiceData?)
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

    /**
     * @return true 等待成功 ; false 失败(超时,停止
     */
    fun waitForUnlock(millis: Long = -1): Boolean

    fun notifySync()
    fun sleep(millis: Long)
    fun onFinish(resultCode: Int)

    fun smartOpen(data: String): Boolean
    fun smartClose(data: String): Boolean
    /**
     * 语音合成
     * 异步
     */
    fun speak(text: String)

    //同步
    fun speakSync(text: String): Boolean

    /**
     * 移除语音悬浮窗
     */
    fun removeFloat()

    /**
     * 当指令无法完成请求时，抛出该异常
     */
    fun notSupport() {
        throw NotSupportException()
    }

}

interface RuntimeArgs {
    var DEBUG: Boolean
    var userInterrupted: Boolean//用户中断
    var command: String?
    //Runtime
    var currentActionIndex: Int
    var actionCount: Int

    val focusView: ViewNode?
    val currentApp: ActionScope?
    val currentActivity: String?

    var actionScope: Int?

    fun isGlobal(): Boolean
    var running: Boolean

    /**
     * 打开 1
     * 关闭-1
     */
    var commandType: Int
}