package cn.vove7.executorengine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.CallSuper
import cn.vove7.appbus.AppBus
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.*
import cn.vove7.common.bridges.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.executor.OnExecutorResult
import cn.vove7.common.executor.PartialResult
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.common.viewnode.ViewNode
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.executor.entity.MarkedContact
import cn.vove7.datamanager.executor.entity.MarkedOpen
import cn.vove7.datamanager.executor.entity.MarkedOpen.*
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.datamanager.parse.model.ActionScope
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.ContactHelper
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
        val serviceBridge: ServiceBridge,
        val onExecutorResult: OnExecutorResult
) : CExecutorI {
    val systemBridge = SystemBridge()
    var accessApi: AccessibilityApi? = null
    private var lock = Object()
    var currentAction: Action? = null
    private val contactHelper = ContactHelper(context)
    private val markedOpenDao = DAO.daoSession.markedOpenDao
    val globalAutomator = GlobalActionAutomator(context, Handler(Looper.myLooper()))

    private lateinit var actionQueue: PriorityQueue<Action>

    override var actionCount: Int = 0

    override var currentActionIndex: Int = 0

    override val currentScope: ActionScope?
        get() {
            val r = checkAccessibilityService(false)
            return if (r) {
                accessApi?.currentScope
            } else null
        }

    override var currentActivity: String = ""
        get() {
            val r = checkAccessibilityService(false)
            return if (r) {
                accessApi?.currentActivity ?: ""
            } else {
                ""
            }
        }

    private var thread: Thread? = null
    override fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>) {
        if (thread?.isAlive == true) {
            thread!!.interrupt()
        }
        this.actionQueue = actionQueue
        lock = Object()
        thread = thread(start = true, isDaemon = true, priority = Thread.MAX_PRIORITY) {
            onExecutorResult.onExecuteStart(cmdWords)
            actionCount = actionQueue.size
            currentActionIndex = 0
            pollActionQueue()
            currentAction = null
            onFinish()
        }
    }

    /**
     * 执行队列
     */
    private fun pollActionQueue() {
        var r: PartialResult
        while (actionQueue.isNotEmpty()) {
            currentActionIndex++
            if (Thread.currentThread().isInterrupted.not()) {
                currentAction = actionQueue.poll()
                r = runScript(currentAction!!.actionScript, currentAction!!.param.value)
                when {
                    r.needTerminal -> {//出错
                        currentAction = null
                        onExecutorResult.onExecuteFailed(r.msg)
                        return
                    }
                }
            } else {
                Vog.i(this, "pollActionQueue 终止")
                actionQueue.clear()
                onExecutorResult.onExecuteFailed("强行终止")
                break
            }
        }
    }

    override fun onFinish() {
        onExecutorResult.onExecuteFinished("执行结束")
    }

    @CallSuper
    override fun interrupt() {
        if (thread != null) {
            try {
                thread!!.checkAccess()
                thread!!.interrupt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        accessApi?.removeAllNotifier(this)
    }

    /**
     * 跟随打开解析App内操作
     */
    private fun parseAppInnerOperation(cmd: String, pkg: String): Boolean {
        actionQueue = ParseEngine.matchAppAction(cmd, pkg)
        if (actionQueue.isNotEmpty()) Vog.d(this, "smartOpen app内操作")
        else {
            Vog.d(this, "smartOpen 无后续操作")
            return true
        }

        val r = checkAccessibilityService()
        return if (r) {
            //等待App打开
            val waitR = waitForApp(pkg)
            if (!waitR) return false
            pollActionQueue()
            true
            //解析App内操作
        } else
            false

    }

    override fun smartOpen(data: String): Boolean {
        return smartOpen(data, "")
    }

    private fun smartOpen(data: String, follow: String): Boolean {
        Vog.d(this, "smartOpen pkg:$data follow:$follow")
        //包名
        if (PACKAGE_REGEX.matches(data)) {
            systemBridge.openAppByPkg(data).also {
                return if (it.ok)
                    parseAppInnerOperation(follow, data)
                else{
                    globalAutomator.toast(context.getString(R.string.text_app_not_install))
                    false
                }
            }
        }
        //By App Name
        val o = systemBridge.openAppByWord(data)
        return if (o.ok) {//打开App 解析跟随操作
            parseAppInnerOperation(data, o.returnValue!!)
        } else {//其他操作,打开网络,手电，网页
            parseOpenAction(data)
        }
    }

    /**
     * 解析打开动作
     */
    private fun parseOpenAction(p: String): Boolean {
        /*val marked = markedOpenDao.queryBuilder()
                .where(MarkedOpenDao.Properties.Key.like(p)).unique()
         if (marked != null) {//通过Key查询到
            //处理follow命令
            p.indexOf()
           return openByIdentifier(marked)
        } else {*/
        markedOpenDao.loadAll().forEach {
            val result = it.regex.matchEntire(p)
            if (result != null) {
                return openByIdentifier(it, ParseEngine.getLastParam(result.groups))//执行
            }
        }
        GlobalLog.err("parseOpenAction 未知操作: $p")
        return false
//        }
    }

    /**
     * 解析标识符
     */
    private fun openByIdentifier(it: MarkedOpen, follow: String): Boolean {
        return when (it.type) {
            MARKED_TYPE_APP -> {
                smartOpen(it.value, follow)
            }
            MARKED_TYPE_SYS_FUN -> {
                when (it.value) {
                    "openFlash" -> {//手电
                        systemBridge.openFlashlight().ok
                    }
                    else -> false
                }
            }
            MARKED_TYPE_SCRIPT -> {
                runScript(it.value).isSuccess
            }
            else -> {
                GlobalLog.err("openByIdentifier -> 未知Type $it")
                false
            }
        }
    }

    private var waitNode: ViewNode? = null
    override fun notifyShow(node: ViewNode) {
        Vog.d(this, "notifyShow node: $node")
        waitNode = node
        notifySync()
    }

    override fun getViewNode(): ViewNode? {
        val n = waitNode
        waitNode = null
        return n
    }

    override fun notifyShow(scope: ActionScope) {
        Vog.d(this, "notifyShow $scope")
        notifySync()
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
     * @param millis 等待时限 -1 无限
     */
    override fun waitForUnlock(millis: Long): Boolean {
        synchronized(lock) {
            val begin = System.currentTimeMillis() // 开始等待时间
            Vog.d(this, "执行器-等待 time: $millis   begin: $begin")

            //等待结果
            try {
                if (millis < 0) {
                    lock.wait()
                    Vog.d(this, "执行器-解锁")
                    return true
                } else {
                    lock.wait(millis)
                    val end = System.currentTimeMillis()
                    Vog.d(this, "执行器-解锁")
                    if (end - begin >= millis) {//自动超时 终止执行
//                        onExecutorResult.onExecuteFailed("等待超时")
                        accessApi?.removeAllNotifier(this)//移除监听器
                        return false
                    }
                    return true
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                //必须强行stop
                Vog.d(this, "被迫强行停止")
                onExecutorResult.onExecuteFailed("终止执行")
                accessApi?.removeAllNotifier(this)
                Thread.currentThread().interrupt()
                Thread.currentThread().stop()
                return false
            }
        }
    }

    /**
     * 等待指定App出现，解锁
     */
    override fun waitForApp(pkg: String, activityName: String?, m: Long): Boolean {
        Vog.d(this, "waitForApp $pkg $activityName")
        if (!checkAccessibilityService())
            return false

        accessApi?.waitForActivity(this, ActionScope(pkg, activityName))
        return waitForUnlock(m)
    }

    override fun waitForViewId(id: String, m: Long): ViewNode? {
        Vog.d(this, "waitForViewId $id")
        if (!checkAccessibilityService())
            return null

        accessApi?.waitForView(this, ViewFindBuilder(this).id(id).viewFinderX)
        waitForUnlock(m)
        return getViewNode()

    }

    override fun waitForDesc(desc: String, m: Long): ViewNode? {
        Vog.d(this, "waitForDesc $desc")

        accessApi?.waitForView(this, ViewFindBuilder(this).desc(desc).viewFinderX)
        waitForUnlock(m)
        return getViewNode()
    }

    override fun waitForText(text: String, m: Long): ViewNode? {
        Vog.d(this, "waitForText $text")
        if (!checkAccessibilityService())
            return null
        accessApi?.waitForView(this, ViewFindBuilder(this).containsText(text).viewFinderX)
        waitForUnlock(m)
        return getViewNode()
    }

    /**
     * 等待语音参数
     */
    override fun waitForVoiceParam(askWord: String?): String? {

        serviceBridge.getVoiceParam(currentAction!!)
        waitForUnlock()
        //得到结果 -> action.param
        return if (!currentAction!!.responseResult) {
            null
//                PartialResult(false, true, "获取语音参数失败")
        } else
            currentAction!!.param.value

    }

    /**
     * 等待单选结果
     * @return if 调用成功 true else false
     */
    override fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): ChoiceData? {
        //通知显示单选框
        AppBus.post(ShowDialogEvent(WHICH_SINGLE, currentAction!!, askTitle, choiceData))
        waitForUnlock()
        return if (currentAction!!.responseResult) {
            Vog.d(this, "结果： have data")
            val bundle = currentAction!!.responseBundle
            bundle.getSerializable("data") as ChoiceData
        } else {
            Vog.d(this, "结果： 取消")
            null
        }.also { Vog.d(this, "waitForSingleChoice result : $it") }
    }

    override fun singleChoiceDialog(askTitle: String, choiceData: Array<String>): String? {
        return waitForSingleChoice(askTitle, array2ChoiceData(choiceData))?.title
    }

    private fun array2ChoiceData(arr: Array<String>): List<ChoiceData> {
        val l = mutableListOf<ChoiceData>()
        arr.forEach {
            l.add(ChoiceData(title = it, originalData = it))
        }
        return l
    }


    /**
     * 等待确认结果
     * @param msg 提示信息
     */
    override fun alert(title: String, msg: String): Boolean {
        AppBus.post(ShowAlertEvent(title, msg, currentAction!!))
        waitForUnlock()
        return currentAction!!.responseResult.also {
            Vog.d(this, "alert result > $it")
        }
    }

    override fun speak(text: String) {
        serviceBridge.speak(text)
    }

    override fun speakSync(text: String): Boolean {
        serviceBridge.speakSync(text)
        waitForUnlock()
        return if (callbackVal == null) true
        else {
            Vog.d(this, "回调结果失败 speakSync $callbackVal")
            false
        }
    }

    private var callbackVal: Any? = null
    override fun speakCallback(result: String?) {
        callbackVal = result
        notifySync()
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
        Vog.d(this, "smartCallPhone $s")
        val result = systemBridge.call(s)
        return if (!result.ok) {
            if (!alert("未识别该联系人", "选择是否标记该联系人: $s")) {
                return PartialResult(false)
            }
            //标识联系人
            val choiceData =
                waitForSingleChoice("选择要标识的联系人", contactHelper.getChoiceData())
            if (choiceData != null) {
                //开启线程
                thread {
                    //保存标记
                    val data = choiceData.originalData as ContactInfo
                    val marked = MarkedContact()
                    marked.key = s
                    marked.contactName = data.contactName
                    marked.phone = choiceData.subtitle
                    contactHelper.addMark(marked)
                }
                val sss = systemBridge.call(choiceData.subtitle!!)
                PartialResult(sss.ok, if (!sss.ok) sss.errMsg else "")
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

        /**
         * 检测包名正则
         */
        val PACKAGE_REGEX = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)+".toRegex()

    }

    override fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            //必须强行stop
            Thread.currentThread().interrupt()
            Thread.currentThread().stop()
//            Thread.currentThread().stop()
        }
    }

    override fun checkAccessibilityService(jump: Boolean): Boolean {
        return if (!bridgeIsAvailable()) {
            if (jump)
                AppBus.post(RequestPermission("无障碍服务"))
            false
        } else true
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
            } else {
                GlobalLog.log(context.getString(R.string.text_acc_service_not_running))
                false
            }
        }
        return true
    }
}
