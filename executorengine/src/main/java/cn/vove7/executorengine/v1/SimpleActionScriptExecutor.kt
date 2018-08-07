package cn.vove7.executorengine.v1

import android.content.Context
import android.os.Build
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.executor.OnExecutorResult
import cn.vove7.common.executor.PartialResult
import cn.vove7.executorengine.AbsExecutorImpl
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.vtp.log.Vog
import java.util.*

/**
 * 顺序简易执行器
 *
 * Created by Vove on 2018/6/18
 */
class SimpleActionScriptExecutor(
        context: Context,
        systemBridge: SystemBridge,
        serviceBridge: ServiceBridge,
        onExecutorResult: OnExecutorResult
) : AbsExecutorImpl(context, systemBridge, serviceBridge, onExecutorResult) {

    override fun runScript(script: String, voiceArg: String?): PartialResult {

        var execLog = ""
        val sin = Scanner(script)
        var line: String
        var partialResult: PartialResult
        while (sin.hasNext()) {
            line = sin.nextLine()
            partialResult = execAction(line)
            when {
                partialResult.needTerminal -> {//出错
                    val msg = "执行终止-- on $line ${partialResult.msg}"
                    Vog.d(this, msg)
                    currentAction = null
                    onExecutorResult.onExecuteFailed(msg)
                    return partialResult
                }
                !partialResult.isSuccess -> {
                    execLog += "失败-- on $line ${partialResult.msg}\n"
                    Vog.d(this, execLog)
                }
            }
        }
        return PartialResult(true)
    }


    private fun replaceVar(ps: MutableList<String>, value: String) {
        for (i in 0 until ps.size) {
            if (ps[i] == "%") {
                ps[i] = value
            }
        }
    }

    /**
     * 执行动作
     * @return 执行结果,
     */
    private fun execAction(cmd: String): PartialResult {
        if (currentAction == null) {
            Vog.d(this, "execAction currentAction is null")
            return PartialResult(false, true, "出错")
        }
        val action = currentAction!!
        // 解析命令/参数 open(...)
        val mResult = getParamsReg.matchEntire(cmd)
        val ps: MutableList<String>
        val c =
            if (mResult != null) {
                val param = mResult.groupValues[1]
                ps = param.substring(1, param.length - 1).split(",").toMutableList()//变量数组
                if (action.param != null && action.param!!.value != null) {//替换脚本变量
                    Vog.d(this, "execAction 替换变量${action.param!!.value}")
                    replaceVar(ps, action.param!!.value)
                }
                cmd.substring(0, mResult.groups[1]?.range?.first ?: cmd.length)
            } else {
                ps = mutableListOf(action.param?.value ?: "")
                cmd
            }
        Vog.d(this, "执行 - $c $ps")

        val p = ps[0]
        when (c) {
            ACTION_OPEN -> {//打开应用/其他额外
                return if (checkParam(p)) openSomething(p)
                else {
                    if (waitForVoiceParam() != null)
                        execAction(cmd)
                    else PartialResult(false, true, "无参数")
                }
            }
            ACTION_CALL -> {
                return if (checkParam(p)) smartCallPhone(p)
                else {
                    if (waitForVoiceParam() != null)
                        execAction(cmd)
                    else PartialResult(false, true, "无参数")
                }
            }
            ACTION_SLEEP -> {
                try {
                    sleep(p.toLong())
                } catch (e: NumberFormatException) {
                    sleep(1000)
                }
                Vog.d(this, "休眠结束")
                return PartialResult(true)
            }
            ACTION_SWIPE -> {
                return try {
                    if (ps.size >= 5) {
                        if (globalAutomator.swipe(ps[0].toInt(), ps[1].toInt(), ps[2].toInt(), ps[3].toInt(), ps[4].toInt())) {
                            PartialResult(true)
                        } else
                            PartialResult(false)
                    } else PartialResult(false, true, "参数数量应为5")
                } catch (e: Exception) {
                    e.printStackTrace()
                    PartialResult(false, true, "参数错误")
                }
            }
            ACTION_OPEN_ACTIVITY -> {
                return if (ps.size >= 2) PartialResult(systemBridge.startActivity(p, ps[1]))
                else PartialResult(false, true, "参数数量应为5")
            }
        }

        /**
         * 操作View
         * @param v (id,desc,)
         * @param by `BY_ID BY_TEXT BY_DESC BY_ID_AND_TEXT`
         * @param op OPERATION__
         * @param ep 额外参数 such as [ViewNode.setText()]
         * @param stopIfFail 失败是否终止
         */
        //
        fun operateViewOperation(v: String, by: Int, op: Int, v1: String = "",
                                 ep: String? = null, stopIfFail: Boolean = true): PartialResult {
            val node = when (by) {
                BY_ID -> accessApi?.findFirstNodeById(v)
                BY_TEXT -> accessApi?.findFirstNodeByTextWhitFuzzy(v)
                BY_DESC -> accessApi?.findFirstNodeByDesc(v)
                BY_ID_AND_TEXT -> accessApi?.findFirstNodeByIdAndText(v, v1)//id/text

                else -> null
            }
            return if (node != null) {
                if (when (op) {
                            OPERATION_CLICK -> node.tryClick()
                            OPERATION_LONG_CLICK -> node.tryLongClick()
                            OPERATION_SET_TEXT -> node.setText(v1, ep)
                            OPERATION_FOCUS -> node.focus()
                            OPERATION_DOUBLE_CLICK -> node.doubleClick()
                            OPERATION_SCROLL_UP -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    node.scrollUp()
                                } else {
                                    Vog.d(this, "operateViewOperation 版本M")
                                    false
                                }
                            }
                            OPERATION_SCROLL_DOWN -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    node.scrollDown()
                                } else {
                                    Vog.d(this, "operateViewOperation 版本M")
                                    false
                                }
                            }
                            else -> false
                        }) {
                    Thread.sleep(100)
                    PartialResult(true)

                } else PartialResult(false, stopIfFail, "操作失败 - $op")
            } else PartialResult(false, stopIfFail, "未找到View: $v")
        }


        /**
         * @param op 滚动方向 [OPERATION_SCROLL_UP] / [OPERATION_SCROLL_DOWN]
         */
        fun scroll(op: Int): PartialResult {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (when (op) {
                            OPERATION_SCROLL_DOWN -> globalAutomator.scrollDown()
                            OPERATION_SCROLL_UP -> globalAutomator.scrollUp()
                            else -> false
                        }
                ) PartialResult(true)
                else PartialResult(false, true, "Scroll失败")
            } else {
                PartialResult(false, true, "需版本M")
            }
        }

        if (needAccess.contains(c)) {
            //需无障碍服务
            if (!checkAccessibilityService().isSuccess)
                return PartialResult(false, true, ACCESSIBILITY_DONT_OPEN)
            //不需要检查参数
            when (c) {
                ACTION_SCROLL_UP -> {
                    return scroll(OPERATION_SCROLL_UP)
                }
                ACTION_SCROLL_DOWN -> {
                    return scroll(OPERATION_SCROLL_DOWN)
                }
                ACTION_BACK -> {
                    return PartialResult(pressBack())
                }
                ACTION_HOME -> {
                    return PartialResult(goHome())
                }
                ACTION_POWER_DIALOG -> {
                    return PartialResult(globalAutomator.powerDialog())
                }
                ACTION_RECENT -> {
                    return PartialResult(openRecent())
                }
                ACTION_PULL_NOTIFICATION -> {
                    return PartialResult(globalAutomator.notifications())
                }
            }

            if (needParamsAction.contains(c)) {
                //检查参数
                if (!checkParam(p)) {
                    return if (waitForVoiceParam() != null)
                        execAction(cmd)
                    else PartialResult(false, true, "无参数")
                }
            } else {
                Vog.d(this, "未知操作 - $c")
                //调用聊天
                PartialResult(false)
            }
            //
            return when (c) {
                ACTION_CLICK -> {
                    if (ps.size >= 2) {
                        PartialResult(globalAutomator.click(ps[0].toInt(), ps[1].toInt()))
                    } else PartialResult(false, true, "参数数量应为2")
                }
                ACTION_LONG_CLICK -> {
                    if (ps.size >= 2) {
                        PartialResult(globalAutomator.longClick(ps[0].toInt(), ps[1].toInt()))
                    } else PartialResult(false, true, "参数数量应为2")
                }

                ACTION_CLICK_TEXT -> {//
                    operateViewOperation(p, BY_TEXT, OPERATION_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_CLICK_DESC -> {
                    operateViewOperation(p, BY_DESC, OPERATION_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_CLICK_ID -> {
                    operateViewOperation(p, BY_ID, OPERATION_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_CLICK_ID_AND_TEXT -> {
                    if (ps.size >= 2)
                        operateViewOperation(p, BY_ID_AND_TEXT, OPERATION_CLICK, ps[1], stopIfFail = needStop(ps, 2))
                    else PartialResult(false, true, "参数数量应为2")
                }
                ACTION_DOUBLE_CLICK_TEXT -> {
                    operateViewOperation(p, BY_TEXT, OPERATION_DOUBLE_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_DOUBLE_CLICK_DESC -> {
                    operateViewOperation(p, BY_DESC, OPERATION_DOUBLE_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_DOUBLE_CLICK_ID -> {
                    operateViewOperation(p, BY_ID, OPERATION_DOUBLE_CLICK, stopIfFail = needStop(ps, 1))

                }
                ACTION_DOUBLE_CLICK_ID_AND_TEXT -> {
                    if (ps.size >= 2)
                        operateViewOperation(p, BY_ID_AND_TEXT, OPERATION_DOUBLE_CLICK, ps[1], stopIfFail = needStop(ps, 2))
                    else PartialResult(false, true, "参数数量应为2")
                }
                ACTION_LONG_CLICK_TEXT -> {
                    operateViewOperation(p, BY_TEXT, OPERATION_LONG_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_LONG_CLICK_ID -> {
                    operateViewOperation(p, BY_ID, OPERATION_LONG_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_LONG_CLICK_DESC -> {
                    operateViewOperation(p, BY_DESC, OPERATION_LONG_CLICK, stopIfFail = needStop(ps, 1))
                }
                ACTION_LONG_CLICK_ID_AND_TEXT -> {
                    if (ps.size >= 2)
                        operateViewOperation(p, BY_ID_AND_TEXT, OPERATION_LONG_CLICK, ps[1], stopIfFail = needStop(ps, 2))
                    else PartialResult(false, true, "参数数量应为2")
                }
                ACTION_SET_TEXT_BY_ID -> {//失败终止
                    if (ps.size >= 2) {
                        val ep = if (ps.size >= 3) ps[2] else null
                        operateViewOperation(p, BY_ID, OPERATION_SET_TEXT, ps[1], ep)
                    } else PartialResult(false, true, "参数数量应为2")
                }
                ACTION_FOCUS_TEXT -> {
                    operateViewOperation(p, BY_TEXT, OPERATION_FOCUS, stopIfFail = needStop(ps, 1))
                }
                ACTION_FOCUS_ID -> {
                    operateViewOperation(p, BY_ID, OPERATION_FOCUS, stopIfFail = needStop(ps, 1))
                }
                ACTION_ALERT -> {
                    val r = alert("确认以继续", p)
                    PartialResult(r, !r, if (r) "" else "被迫停止")
                }
                ACTION_WAIT_FOR_ACTIVITY -> {
                    if (ps.size >= 2) waitForApp(p, ps[1])
                    else PartialResult(false, true, "参数数量应为2")
                }
//                ACTION_WAIT_FOR_APP_VIEW_ID -> {
//                    if (ps.size >= 2) waitForAppViewId(ps[0], ps[1])
//                    else PartialResult(false, true, "参数数量应为2")
//                }
                ACTION_WAIT_FOR_VIEW_ID -> {
                    PartialResult(waitForViewId(p) != null)
                }
                ACTION_WAIT_FOR_VIEW_DESC -> {
                    PartialResult(waitForDesc(p) != null)
                }
                ACTION_WAIT_FOR_VIEW_Text -> {
                    PartialResult(waitForText(p) != null)
                }
                ACTION_SCROLL_UP_BY_ID -> {
                    operateViewOperation(p, BY_ID, OPERATION_SCROLL_UP, stopIfFail = needStop(ps, 1))
                }
                ACTION_SCROLL_DOWN_BY_ID -> {
                    operateViewOperation(p, BY_ID, OPERATION_SCROLL_DOWN, stopIfFail = needStop(ps, 1))
                }
                else -> PartialResult(false)
            }
        } else return PartialResult(false)
    }

    /**
     * 根据最后一位参数判断是否 需要失败终止
     * @param pNum 固定参数数量
     * @return 执行失败终止
     */
    private fun needStop(ps: List<String>, pNum: Int): Boolean {
        return if (ps.size > pNum) {
            ps[ps.size - 1] != "ds"//dont stop
        } else true
    }


    companion object {

        //全局
        const val ACTION_CLICK = "click"//(x,y)
        const val ACTION_LONG_CLICK = "longClick"//(x,y)
        const val ACTION_OPEN = "open"
        const val ACTION_OPEN_ACTIVITY = "openActivity"
        const val ACTION_BACK = "back"
        const val ACTION_HOME = "home"
        const val ACTION_RECENT = "recent"
        const val ACTION_PULL_NOTIFICATION = "notifications"
        const val ACTION_CALL = "call"
        const val ACTION_SLEEP = "sleep"
        const val ACTION_ALERT = "alert"
        const val ACTION_POWER_DIALOG = "showPowerDialog"
        const val ACTION_SWIPE = "swipe"//(x1,y1,x2,y2,delay)

        //全局、应用内
        const val ACTION_CLICK_TEXT = "clickByText"
        const val ACTION_CLICK_DESC = "clickByDesc"
        const val ACTION_CLICK_ID = "clickById"
        const val ACTION_CLICK_ID_AND_TEXT = "clickByIdAndText"
        const val ACTION_DOUBLE_CLICK_TEXT = "dClickByText"
        const val ACTION_DOUBLE_CLICK_DESC = "dClickByDesc"
        const val ACTION_DOUBLE_CLICK_ID = "dClickById"
        const val ACTION_DOUBLE_CLICK_ID_AND_TEXT = "dClickByIdAndText"
        const val ACTION_LONG_CLICK_TEXT = "lClickByText"
        const val ACTION_LONG_CLICK_ID = "lClickById"
        const val ACTION_LONG_CLICK_DESC = "lClickByDesc"
        const val ACTION_LONG_CLICK_ID_AND_TEXT = "lClickByIdAndText"
        const val ACTION_SET_TEXT_BY_ID = "setTextById"
        const val ACTION_SCROLL_UP = "scrollUp"
        const val ACTION_SCROLL_DOWN = "scrollDown"
        const val ACTION_SCROLL_UP_BY_ID = "scrollUpById"
        const val ACTION_SCROLL_DOWN_BY_ID = "scrollDownById"
        const val ACTION_FOCUS_TEXT = "focusByText"
        const val ACTION_FOCUS_ID = "focusById"

        const val ACTION_WAIT_FOR_ACTIVITY = "waitForApp"
        //        const val ACTION_WAIT_FOR_APP_VIEW_ID = "waitForAppAndId"
        const val ACTION_WAIT_FOR_VIEW_ID = "waitForId"
        const val ACTION_WAIT_FOR_VIEW_DESC = "waitForDesc"
        const val ACTION_WAIT_FOR_VIEW_Text = "waitForText"

        const val ACCESSIBILITY_DONT_OPEN = "无障碍未开启"

        //BY
        const val BY_ID = 986
        const val BY_TEXT = 345
        const val BY_DESC = 227
        const val BY_ID_AND_TEXT = 500
        const val OPERATION_CLICK = 0
        const val OPERATION_LONG_CLICK = 1
        const val OPERATION_DOUBLE_CLICK = 110
        const val OPERATION_SET_TEXT = 2
        const val OPERATION_SCROLL_UP = 3
        const val OPERATION_SCROLL_DOWN = 4
        const val OPERATION_FOCUS = 5
        const val OPERATION_SCROLL_LEFT = 6
        const val OPERATION_SCROLL_RIGHT = 7


        //需参数
        val needParamsAction = arrayOf(
                ACTION_FOCUS_TEXT
                , ACTION_CLICK_TEXT
                , ACTION_CLICK_DESC
                , ACTION_CLICK_ID
                , ACTION_LONG_CLICK_TEXT
                , ACTION_LONG_CLICK_ID
                , ACTION_DOUBLE_CLICK_TEXT
                , ACTION_DOUBLE_CLICK_DESC
                , ACTION_DOUBLE_CLICK_ID
                , ACTION_DOUBLE_CLICK_ID_AND_TEXT
                , ACTION_LONG_CLICK_DESC
                , ACTION_CLICK_ID_AND_TEXT
                , ACTION_LONG_CLICK_ID_AND_TEXT
                , ACTION_SET_TEXT_BY_ID
                , ACTION_FOCUS_ID
                , ACTION_ALERT
                , ACTION_WAIT_FOR_ACTIVITY
//                , ACTION_WAIT_FOR_APP_VIEW_ID
                , ACTION_WAIT_FOR_VIEW_ID
                , ACTION_WAIT_FOR_VIEW_DESC
                , ACTION_WAIT_FOR_VIEW_Text
        )
        val needAccess = mutableListOf(
                ACTION_SCROLL_UP
                , ACTION_SCROLL_DOWN
                , ACTION_BACK
                , ACTION_HOME
                , ACTION_RECENT
                , ACTION_PULL_NOTIFICATION
        )

        init {
            needAccess.addAll(needParamsAction)
        }

        /**
         * 匹配参数
         */
        private val getParamsReg = "[\\S]*(\\([\\S\\s]*\\))".toRegex()
    }

}

