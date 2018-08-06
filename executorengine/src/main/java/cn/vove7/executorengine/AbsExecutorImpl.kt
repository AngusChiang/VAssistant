package cn.vove7.executorengine

import android.content.Context
import android.os.Handler
import android.os.Looper
import cn.vove7.appbus.AppBus
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.bridges.*
import cn.vove7.common.bridges.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.executor.PartialResult
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.executor.entity.MarkedContact
import cn.vove7.datamanager.executor.entity.MarkedOpen
import cn.vove7.datamanager.executor.entity.MarkedOpen.*
import cn.vove7.datamanager.greendao.MarkedOpenDao
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.ContactHelper
import cn.vove7.executorengine.v1.OnExecutorResult
import cn.vove7.executorengine.v1.SimpleActionScriptExecutor
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.contact.ContactInfo
import cn.vove7.vtp.log.Vog
import java.util.*
import kotlin.concurrent.thread

/**
 *
 * 基础执行器
 * Created by Vove on 2018/6/20
 */
abstract class AbsExecutorImpl(
        val context: Context,
        val systemBridge: SystemBridge,
        val serviceBridge: ServiceBridge,
        val onExecutorResult: OnExecutorResult
) : CExecutorI {
    var accessApi: AccessibilityApi? = null
    var lock = Object()
    var currentAction: Action? = null
    private val contactHelper = ContactHelper(context)
    private val markedOpenDao = DAO.daoSession.markedOpenDao
    val globalAutomator = GlobalActionAutomator(Handler(Looper.myLooper()))

    lateinit var actionQueue: PriorityQueue<Action>


    private var thread: Thread? = null
    override fun execQueue(actionQueue: PriorityQueue<Action>) {
        this.actionQueue = actionQueue
        if (thread != null && thread!!.isAlive) {
            thread!!.interrupt()
        }
        lock = Object()
        thread = thread(start = true, isDaemon = true, priority = Thread.MAX_PRIORITY) {
            pollActionQueue()
            currentAction = null
            onExecutorResult.onExecutorSuccess("执行完毕 ")
        }
    }

    override fun stop() {
        if (thread != null) {
            thread!!.interrupt()
        }
    }

    /**********Function*********/

    /**
     * 打开: 应用 -> 其他额外
     */
    override fun openSomething(data: String): PartialResult {
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
                pollActionQueue()
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
                        PartialResult(systemBridge.openFlashlight())
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


    override fun notifyShow() {
        notifySync()
//        accessApi?.removeAllNotifier(this)
    }

    override fun notifySync() {
        synchronized(lock) {
            lock.notify()
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
    override fun waitForUnlock(millis: Long): Boolean {
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
        if (!checkAccessibilityService().isSuccess)
            return PartialResult(false, true, SimpleActionScriptExecutor.ACCESSIBILITY_DONT_OPEN)

        accessApi?.waitForActivity(this, pkg, activityName)
        return if (waitForUnlock())
            PartialResult(true)
        else PartialResult(false, true, "被迫强行停止")
    }

    override fun waitForViewId(id: String): PartialResult {
        Vog.d(this, "waitForViewId $id")
        if (!checkAccessibilityService().isSuccess)
            return PartialResult(false, true, SimpleActionScriptExecutor.ACCESSIBILITY_DONT_OPEN)


        accessApi?.waitForView(this, ViewFindBuilder(this).id(id).viewFinderX)
        return if (waitForUnlock())
            PartialResult(true)
        else PartialResult(false, true, "被迫强行停止")
    }

    override fun waitForDesc(desc: String): PartialResult {
        Vog.d(this, "waitForDesc $desc")

        accessApi?.waitForView(this, ViewFindBuilder(this).desc(desc).viewFinderX)
        return if (waitForUnlock())
            PartialResult(true)
        else PartialResult(false, true, "被迫强行停止")
    }

    override fun waitForText(text: String): PartialResult {
        Vog.d(this, "waitForText $text")
        if (!checkAccessibilityService().isSuccess)
            return PartialResult(false, true, SimpleActionScriptExecutor.ACCESSIBILITY_DONT_OPEN)
        accessApi?.waitForView(this, ViewFindBuilder(this).containsText(text).viewFinderX)
        return if (waitForUnlock())
            PartialResult(true)
        else PartialResult(false, true, "被迫强行停止")
    }

    /**
     * 等待语音参数
     */
    override fun waitForVoiceParam(action: Action): PartialResult {
        serviceBridge.getVoiceParam(action)
        return if (waitForUnlock())
        //得到结果 -> action.param
            if (!action.responseResult) {
                PartialResult(false, true, "获取语音参数失败")
            } else
                PartialResult(true, "等到参数重新执行")//重复执行
        else PartialResult(false, true, "被迫强行停止")
    }

    /**
     * 等待单选结果
     * @return if 调用成功 true else false
     */
    private fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): Boolean {
        //通知显示单选框
        AppBus.post(ShowDialogEvent(WHICH_SINGLE, currentAction!!, askTitle, choiceData))
        return if (waitForUnlock())
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
        AppBus.post(ShowAlertEvent("确认以继续",msg, currentAction !!))
        return if (waitForUnlock()) {
            val r = currentAction!!.responseResult
            PartialResult(r, !r, "$msg -> $r")
        } else PartialResult(false, true, "被迫强行停止")
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
     * 拨打
     */
    fun smartCallPhone(s: String): PartialResult {
        val result = systemBridge.call(s)
        return if (result == null) {
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
                val s = systemBridge.call(choiceData.subtitle!!)
                PartialResult(s == null, s ?: "")
            } else {
                PartialResult(false, true, "取消")
            }
        } else PartialResult(true)
    }

    companion object {
        /**
         * @return 参数可用
         */
        fun checkParam(p: String?): Boolean = (p != null && p.trim() != "")

    }

    override fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {

        }
    }

    /**
     * 执行队列
     */
    private fun pollActionQueue() {
        var r: PartialResult
        while (actionQueue.isNotEmpty()) {
            currentAction = actionQueue.poll()
            r = runScript(currentAction!!.actionScript, currentAction!!.param.value)
            when {
                r.needTerminal -> {//出错
                    currentAction = null
                    onExecutorResult.onExecutorFailed(r.msg)
                    return
                }
            }
        }
    }

    override fun checkAccessibilityService(jump: Boolean): PartialResult {
        return if (!bridgeIsAvailable()) {
            if (jump)
                AppBus.post(RequestPermission("无障碍服务"))
            PartialResult(false, true, SimpleActionScriptExecutor.ACCESSIBILITY_DONT_OPEN)
        } else PartialResult(true)
    }

    /**
     * 检查无障碍服务
     */
    private fun bridgeIsAvailable(): Boolean {
        if (accessApi == null) {
            accessApi = AccessibilityApi.accessibilityService
            return if (accessApi != null) {
                globalAutomator.setService(accessApi!!.getService())

                true
            } else false
        }
        return true
    }
}
