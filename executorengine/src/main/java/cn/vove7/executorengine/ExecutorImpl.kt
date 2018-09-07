package cn.vove7.executorengine

import android.content.Context
import android.support.annotation.CallSuper
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.*
import cn.vove7.common.bridges.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedContact
import cn.vove7.common.datamanager.executor.entity.MarkedOpen
import cn.vove7.common.datamanager.executor.entity.MarkedOpen.*
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.Action.SCRIPT_TYPE_JS
import cn.vove7.common.datamanager.parse.model.Action.SCRIPT_TYPE_LUA
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.executor.PartialResult
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.ScreenAdapter
import cn.vove7.common.view.finder.ViewFindBuilder
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
open class ExecutorImpl(
        val context: Context,
        val serviceBridge: ServiceBridge
) : CExecutorI {
    val systemBridge = SystemBridge()
    var accessApi: AccessibilityApi? = null
    private var lock = Object()
    var currentAction: Action? = null
    private val contactHelper = ContactHelper(context)
    private val markedOpenDao = DAO.daoSession.markedOpenDao
    val globalActionExecutor = GlobalActionExecutor()

    init {
        ScreenAdapter.init(systemBridge)
    }

    private lateinit var actionQueue: PriorityQueue<Action>

    override var actionCount: Int = -1

    override var currentActionIndex: Int = -1
    override var actionScope: Int? = null

    override val currentApp: ActionScope?
        get() {
            val r = checkAccessibilityService(false)
            return if (r) {
                accessApi?.currentScope
            } else null
        }

    override fun isGlobal(): Boolean = globalScopeType.contains(actionScope)

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
            serviceBridge.onExecuteStart(cmdWords)
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
                actionScope = currentAction?.actionScopeType
                Vog.d(this, "pollActionQueue ---> $actionScope")
                r = runScript(currentAction!!.actionScript, currentAction!!.param.value)
                when {
                    r.needTerminal -> {//出错
                        currentAction = null
                        actionQueue.clear()
                        serviceBridge.onExecuteFailed(r.msg)
                        return
                    }
                }
            } else {
                Vog.i(this, "pollActionQueue 终止")
                actionQueue.clear()
                serviceBridge.onExecuteFailed("强行终止")
                break
            }
        }
    }

    override fun runScript(script: String, arg: String?): PartialResult {
        return when (currentAction?.scriptType) {
            SCRIPT_TYPE_LUA -> {
                onLuaExec(script, arg)
            }
            SCRIPT_TYPE_JS -> {
                onRhinoExec(script, arg)
            }
            else ->
                PartialResult.fatal("未知脚本类型: " + currentAction?.scriptType)
        }
    }

    open fun onLuaExec(script: String, arg: String? = null): PartialResult {
        return PartialResult.fatal("not implement onLuaExec")
    }

    open fun onRhinoExec(script: String, arg: String? = null): PartialResult {
        return PartialResult.fatal("not implement onRhinoExec")
    }


    override fun onFinish() {
        serviceBridge.onExecuteFinished("执行结束")
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
        return smartOpen(data, null)
    }

    private fun smartOpen(data: String, follow: String?): Boolean {
        Vog.d(this, "smartOpen pkg:$data follow:$follow")
        //包名
        if (PACKAGE_REGEX.matches(data)) {
            systemBridge.openAppByPkg(data).also {
                return if (it.ok) {
                    follow == null || parseAppInnerOperation(follow, data)
                } else {
                    globalActionExecutor.toast(context.getString(R.string.text_app_not_install))
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
    private fun openByIdentifier(it: MarkedOpen, follow: String?): Boolean {
        return when (it.type) {
            MARKED_TYPE_APP -> {
                smartOpen(it.value, follow)
            }
//            MARKED_TYPE_SYS_FUN -> {
//                when (it.value) {
//                    "openFlash" -> {//手电
//                        systemBridge.openFlashlight().ok
//                    }
//                    else -> false
//                }
//            }
            MARKED_TYPE_SCRIPT_LUA -> {
                onLuaExec(it.value).isSuccess
            }
            MARKED_TYPE_SCRIPT_JS -> {
                onRhinoExec(it.value).isSuccess
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
//                        serviceBridge.onExecuteFailed("等待超时")
                        accessApi?.removeAllNotifier(this)//移除监听器
                        return false
                    }
                    return true
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                //必须强行stop
                Vog.d(this, "被迫强行停止")
                serviceBridge.onExecuteFailed("终止执行")
                accessApi?.removeAllNotifier(this)
                Thread.currentThread().interrupt()
                Thread.currentThread().stop()//???
                return false
            }
        }
    }

    /**
     * 等待指定App出现，解锁
     */
    override fun waitForApp(pkg: String, activityName: String?, m: Long): Boolean {
        Vog.d(this, "waitForApp $pkg $activityName")
        if (!checkAccessibilityService()) {
            return false
        }

        accessApi?.waitForActivity(this, ActionScope(pkg, activityName))
        return waitForUnlock(m)
    }

    override fun waitForViewId(id: String, m: Long): ViewNode? {
        Vog.d(this, "waitForViewId $id")
        if (!checkAccessibilityService()) {
            return null
        }
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
        if (!checkAccessibilityService()) {
            return null
        }
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
//                PartialResult.fatal("获取语音参数失败")
        } else {
            currentAction!!.param.value
        }
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

            val bundle = currentAction!!.responseBundle
            (bundle.getSerializable("data") as ChoiceData).also {
                Vog.d(this, "结果： ${it.title}")
            }
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
    fun pressBack(): Boolean = globalActionExecutor.back()

    /**
     * 最近界面
     */
    fun openRecent(): Boolean = globalActionExecutor.recents()

    /**
     * 主页
     */
    fun goHome(): Boolean = globalActionExecutor.home()

    /**
     * 拨打
     */
    fun smartCallPhone(s: String): PartialResult {
        Vog.d(this, "smartCallPhone $s")
        val result = systemBridge.call(s)
        return if (!result.ok) {
            if (!alert("未识别该联系人", "选择是否标记该联系人: $s")) {
                return PartialResult.failed()
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
                    marked.regexStr = data.contactName

                    marked.phone = choiceData.subtitle
                    marked.from = DataFrom.FROM_USER
                    contactHelper.addMark(marked)
                }
                val sss = systemBridge.call(choiceData.subtitle!!)
                PartialResult(sss.ok, if (!sss.ok) sss.errMsg else "")
            } else {
                PartialResult(false, "取消")
            }
        } else PartialResult.success()
    }

    override fun setScreenSize(width: Int, height: Int) {
        globalActionExecutor.screenAdapter.setScreenSize(width, height)
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
        private val globalScopeType = arrayListOf(ActionNode.NODE_SCOPE_GLOBAL, ActionNode.NODE_SCOPE_GLOBAL_2)

    }

    override fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            //必须强行stop
            Thread.currentThread().interrupt()
//            Thread.currentThread().stop()
        }
    }

    override fun checkAccessibilityService(jump: Boolean): Boolean {
        return if (!bridgeIsAvailable()) {
            if (jump) {
                AppBus.post(RequestPermission("无障碍服务"))
            }
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
                globalActionExecutor.setService(accessApi!!.getService())
                true
            } else {
                GlobalLog.log(context.getString(R.string.text_acc_service_not_running))
                false
            }
        }
        return true
    }
}
