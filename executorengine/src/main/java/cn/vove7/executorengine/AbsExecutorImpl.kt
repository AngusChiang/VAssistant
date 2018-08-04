package cn.vove7.executorengine

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import cn.vove7.appbus.AppBus
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.executor.entity.MarkedContact
import cn.vove7.datamanager.executor.entity.MarkedOpen
import cn.vove7.datamanager.executor.entity.MarkedOpen.*
import cn.vove7.datamanager.greendao.MarkedOpenDao
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.bridges.*
import cn.vove7.executorengine.bridges.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.executorengine.model.PartialResult
import cn.vove7.executorengine.helper.ContactHelper
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.contact.ContactInfo
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread

/**
 *
 *
 * Created by Vove on 2018/6/20
 */
abstract class AbsExecutorImpl(
        val context: Context,
        val getAccessibilityBridge: GetAccessibilityBridge,
        private val systemBridge: SystemBridge,
        private val serviceBridge: ServiceBridge
) : Executor {
    var accessApi: AccessibilityApi? = null
    var lock = Object()
    var currentAction: Action? = null
    private val contactHelper = ContactHelper(context)
    private val markedOpenDao = DAO.daoSession.markedOpenDao
    val globalAutomator = GlobalActionAutomator(Handler(Looper.myLooper()))

    lateinit var actionQueue: PriorityQueue<Action>
    /**
     * 打开: 应用 -> 其他额外
     */
    fun openSomething(data: String): PartialResult {
        val pkg = systemBridge.openAppByWord(data)
         if (pkg != null) {//打开App 解析跟随操作
            actionQueue = ParseEngine.matchAppAction(data, pkg)
            if (actionQueue.isNotEmpty()) Vog.d(this, "openSomething app内操作")
            else {
                Vog.d(this, "openSomething 无后续操作")
                return PartialResult(true)
            }

            val r = checkAccessibilityService()
            return if (r.isSuccess) {
                //等待App打开
                val waitR = waitForApp(pkg)
                if (!waitR.isSuccess) return waitR
                runnable()
                PartialResult(true)
                //解析App内操作
            } else
                r
        } else {//其他操作,打开网络,手电，网页
            return parseOpenAction(data)
        }
    }

    /**
     * 解析打开动作
     */
    private fun parseOpenAction(p: String): PartialResult {
        val marked = markedOpenDao.queryBuilder()
                .where(MarkedOpenDao.Properties.Key.like(p)).unique()
        return if (marked != null) {//通过Key查询到
            openByIdentifier(marked)
        } else {
            markedOpenDao.loadAll().forEach {
                if (it.regStr.toRegex().matches(p))
                    openByIdentifier(it)//执行
            }
            PartialResult(false)
        }
    }

    /**
     * 解析标识符
     */
    private fun openByIdentifier(it: MarkedOpen): PartialResult {
        return when (it.type) {
            MARKED_TYPE_APP -> {
                PartialResult(systemBridge.openAppByPkg(it.value))
            }
            MARKED_TYPE_SYS_FUN -> {
                when (it.value) {
                    "openFlash" -> {//手电
                        systemBridge.openFlashlight()
                    }
                    else -> {
                        PartialResult(false)
                    }
                }
            }
            MARKED_TYPE_SCRIPT -> {
                runScript(it.value)
            }
            else -> {
                Vog.d(this, "parseOpenAction -> 未知Type")
                PartialResult(false)
            }
        }
    }

    /**
     * 线程同步通知
     */
    override fun notifySync() {
        synchronized(lock) {
            lock.notify()
            accessApi?.removeAllNotifier(this)
        }
    }

    /**
     * 得到语音参数,赋值
     */
    override fun onGetVoiceParam(param: String?) {
        Vog.d(this, "onGetVoiceParam -> $param")
        //设置参数
        if (param == null) {
            currentAction?.responseResult = false
        } else {
            currentAction?.responseResult = true
            currentAction?.param?.value = param
        }
        //继续执行
        notifySync()
    }

    /**
     * 锁定线程
     * @return 解锁是否成功
     */
    override fun waitFor(millis: Long): Boolean {
        synchronized(lock) {
            Vog.d(this, "进入等待")
            //等待结果
            return try {
                if (millis < 0) lock.wait() else lock.wait(millis)
                Vog.d(this, "执行器-解锁")
                true
            } catch (e: InterruptedException) {
                Vog.d(this, "被迫强行停止")
                accessApi?.removeAllNotifier(this)
                false
            }
        }
    }


    /**
     * 等待指定App出现，解锁
     */
    override fun waitForApp(pkg: String, activityName: String?): PartialResult {
        Vog.d(this, "waitForApp $pkg $activityName")
        accessApi?.waitForActivity(this, pkg, activityName)
        return if (waitFor())
            PartialResult(true)
        else PartialResult(false, true)
    }

    override fun waitForAppViewId(pkg: String, id: String): PartialResult {
        Vog.d(this, "waitForAppViewId $id")
        accessApi?.waitForAppViewId(this, pkg, id)
        return if (waitFor())
            PartialResult(true)
        else PartialResult(false, true)
    }

    override fun waitForViewId(id: String): PartialResult {
        Vog.d(this, "waitForViewId $id")
        accessApi?.waitForViewId(this, id)
        return if (waitFor())
            PartialResult(true)
        else PartialResult(false, true)
    }

    override fun waitForViewDesc(desc: String): PartialResult {
        Vog.d(this, "waitForViewDesc $desc")
        accessApi?.waitForViewDesc(this, desc)
        return if (waitFor())
            PartialResult(true)
        else PartialResult(false, true)
    }

    override fun waitForViewText(text: String): PartialResult {
        Vog.d(this, "waitForViewText $text")
        accessApi?.waitForViewText(this, text)
        return if (waitFor())
            PartialResult(true)
        else PartialResult(false, true)
    }

    /**
     * 等待语音参数
     */
    override fun waitForVoiceParam(action: Action): PartialResult {
        serviceBridge.getVoiceParam(action)
        return if (waitFor())
        //得到结果 -> action.param
            if (!action.responseResult) {
                PartialResult(false, true, msg = "获取语音参数失败")
            } else
                PartialResult(true, msg = "等到参数重新执行")//重复执行
        else PartialResult(false, true)
    }

    /**
     * 等待单选结果
     * @return if 调用成功 true else false
     */
    private fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): Boolean {
        //通知显示单选框
        AppBus.post(ShowDialogEvent(WHICH_SINGLE, currentAction!!, askTitle, choiceData))
        return if (waitFor())
            if (currentAction!!.responseResult) {
                Vog.d(this, "结果： have data")
                true
            } else {
                Vog.d(this, "结果： 取消")
                false
            }
        else false
    }

    /**
     * 等待确认结果
     * @param msg 提示信息
     */
    override fun waitForAlertResult(msg: String): PartialResult {
        AppBus.post(ShowAlertEvent(msg, currentAction!!))
        return if (waitFor()) {
            val r = currentAction!!.responseResult
            PartialResult(r, !r, "$msg -> $r")
        } else PartialResult(false, true)
    }

    /**
     * 返回操作
     */
    fun pressBack(): Boolean = globalAutomator.back()

    /**
     * 最近界面
     */
    fun openRecent(): Boolean = globalAutomator.recents()

    /**
     * 主页
     */
    fun goHome(): Boolean = globalAutomator.home()


    /**
     * 操作View
     * @param v (id,desc,)
     * @param by `BY_ID BY_TEXT BY_DESC BY_ID_AND_TEXT`
     * @param op OPERATION__
     * @param ep 额外参数 such as [ViewNode.setText()]
     * @param stopIfFail 失败是否终止
     */
    //TODO : SDK版本
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
                sleep(100)
                PartialResult(true)

            } else PartialResult(false, stopIfFail, "操作失败 - $op")
        } else PartialResult(false, stopIfFail, "未找到View: $v")
    }

    /**
     * @param op 滚动方向 [OPERATION_SCROLL_UP] / [OPERATION_SCROLL_DOWN]
     */
    fun scroll(op: Int): PartialResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return if (when (op) {
                        OPERATION_SCROLL_DOWN -> globalAutomator.scrollDown()
                        OPERATION_SCROLL_UP -> globalAutomator.scrollUp()
                        else -> false
                    }
            ) PartialResult(true)
            else PartialResult(false, true, "Scroll失败")
        } else {
            return PartialResult(false, true, "需版本M")
        }
    }

    /**
     * 拨打
     */
    fun callPhone(s: String): PartialResult {
        val result = systemBridge.call(s)
        return if (!result.isSuccess) {
            //标识联系人
            if (waitForSingleChoice("选择要标识的联系人", contactHelper.getChoiceData())) {
                val bundle = currentAction!!.responseBundle
                val choiceData = bundle.getSerializable("data") as ChoiceData
                //开启线程
                thread {
                    val data = choiceData.originalData as ContactInfo
                    val marked = MarkedContact()
                    marked.key = s
                    marked.contactName = data.contactName
                    marked.phone = choiceData.subtitle
                    contactHelper.addMark(marked)//保存
                }
                systemBridge.call(choiceData.subtitle!!)
            } else {
                PartialResult(false, true, msg = "取消")
            }
        } else result
    }

    companion object {
        /**
         * @return 参数可用
         */
        fun checkParam(p: String?): Boolean = (p != null && p.trim() != "")

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

    }
    abstract fun runnable()
}

interface Executor {
    fun exec(actionQueue: PriorityQueue<Action>)
    fun stop()
    fun runScript(script: String): PartialResult
    fun checkAccessibilityService(jump: Boolean = true): PartialResult

    fun waitForVoiceParam(action: Action): PartialResult
    fun waitForAlertResult(msg: String): PartialResult
    fun waitForApp(pkg: String, activityName: String? = null): PartialResult
    fun waitForAppViewId(pkg: String, id: String): PartialResult
    fun waitForViewId(id: String): PartialResult
    fun waitForViewDesc(desc: String): PartialResult
    fun waitForViewText(text: String): PartialResult
    fun notifySync()
    fun onGetVoiceParam(param: String?)
    fun waitFor(millis: Long = -1): Boolean
}