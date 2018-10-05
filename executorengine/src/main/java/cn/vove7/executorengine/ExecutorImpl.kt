package cn.vove7.executorengine

import android.content.Context
import android.support.annotation.CallSuper
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.*
import cn.vove7.common.bridges.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_SCRIPT_JS
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_SCRIPT_LUA
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.Action.SCRIPT_TYPE_JS
import cn.vove7.common.datamanager.parse.model.Action.SCRIPT_TYPE_LUA
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.executor.PartialResult
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.RegUtils
import cn.vove7.common.utils.ScreenAdapter
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.AdvanContactHelper
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
        val serviceBridge: ServiceBridge?
) : CExecutorI {

    private val systemBridge = SystemBridge
    var accessApi: AccessibilityApi? = null
    private var lock = Object()
    var currentAction: Action? = null
    private val markedOpenDao = DAO.daoSession.markedDataDao
    //    val globalActionExecutor = GlobalActionExecutor()
    override var command: String? = null
    override var DEBUG: Boolean = false

    override var running: Boolean = false

    init {
        ScreenAdapter.init(systemBridge)
    }

    private lateinit var actionQueue: PriorityQueue<Action>

    override var actionCount: Int = -1
    override var commandType: Int = 0

    override var currentActionIndex: Int = -1
    override var actionScope: Int? = null

    override var currentApp: ActionScope? = null
        get() {
            val r = checkAccessibilityService(false)
            return if (r) {
                accessApi?.currentScope
            } else null
        }

    override fun isGlobal(): Boolean =
        globalScopeType.contains(actionScope)

    override var currentActivity: String = ""
        get() {
            val r = checkAccessibilityService(false)
            return if (r) {
                accessApi?.currentActivity ?: ""
            } else {
                ""
            }
        }

    /**
     * enter
     */
    private var thread: Thread? = null

    override fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>) {
        if (thread?.isAlive == true) {
            thread!!.interrupt()
        }
        this.command = cmdWords
        if (cmdWords == DEBUG_SCRIPT) {//DEBUG
            DEBUG = true
        }
        ScreenAdapter.reSet()
        this.actionQueue = actionQueue
        lock = Object()
        thread = thread(start = true, isDaemon = true, priority = Thread.MAX_PRIORITY) {
            running = true
            commandType = 0
            serviceBridge?.onExecuteStart(cmdWords)
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
                        serviceBridge?.onExecuteInterrupt(r.msg)
                        return
                    }
                }
            } else {
                Vog.i(this, "pollActionQueue 终止")
                actionQueue.clear()
                serviceBridge?.onExecuteInterrupt("强行终止")
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
        running = false
        serviceBridge?.onExecuteFinished("执行结束")
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
     * 接着打开应用
     */
    private fun parseAppInnerOperation(cmd: String, pkg: String): Boolean {
        val appQ = ParseEngine.matchAppAction(cmd, ActionScope(pkg))
        actionQueue = appQ.second
        //打开应用
        if (actionQueue.isNotEmpty()) {//todo 检查App页面?
            systemBridge.openAppByPkg(pkg, true)
            val his = CommandHistory(UserInfo.getUserId(), cmd, "打开$pkg -> ${appQ.first}")
            AppBus.post(his)
            Vog.d(this, "parseAppInnerOperation app内操作")
        } else {
            systemBridge.openAppByPkg(pkg, false)
            Vog.d(this, "parseAppInnerOperation 无后续操作")
            return true
        }

        val r = checkAccessibilityService()
        return if (r) {
            //等待App打开
            val waitR = waitForApp(pkg, null, 5000)
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

    override fun smartClose(data: String): Boolean {
        val pkg =
            if (RegUtils.isPackage(data)) data
            else systemBridge.getPkgByWord(data)
        return if (pkg != null)
            onLuaExec(CloseAppScript, pkg).isSuccess//强制停止App
        else {
            //Marked打开
            commandType = -1
            parseOpenOrCloseAction(data)
        }
    }

    private fun smartOpen(data: String, follow: String?): Boolean {
        Vog.d(this, "smartOpen: $data follow:$follow")
        //包名
        if (RegUtils.isPackage(data)) {
            systemBridge.openAppByPkg(data).also {
                return if (it) {
                    follow == null || parseAppInnerOperation(follow, data)
                } else {
                    GlobalApp.toastShort(R.string.text_app_not_install)
                    false
                }
            }
        }
        //By App Name
        //确定有跟随操作，clear task
        val o = systemBridge.getPkgByWord(data)
        return if (o != null) {//打开App 解析跟随操作
            parseAppInnerOperation(data, o)
        } else {//其他操作,打开网络,手电，网页
            commandType = 1
            parseOpenOrCloseAction(data)
        }
    }

    /**
     * 解析打开动作
     */
    private fun parseOpenOrCloseAction(p: String): Boolean {
        /*val marked = markedOpenDao.queryBuilder()
                .where(MarkedOpenDao.Properties.Key.like(p)).unique()
         if (marked != null) {//通过Key查询到
            //处理follow命令
            p.indexOf()
           return openByIdentifier(marked)
        } else {*/
        markedOpenDao.queryBuilder().where(MarkedDataDao.Properties.Type.`in`(
                MarkedData.MARKED_TYPE_SCRIPT_LUA,
                MarkedData.MARKED_TYPE_SCRIPT_JS
        )).list().forEach {
            val result = it.regex.matchEntire(p)
            if (result != null) {
                return openByIdentifier(it, ParseEngine.getLastParam(result.groups))//执行
            }
        }
        AppBus.post(CommandHistory(UserInfo.getUserId(), "打开|关闭 $p", null))
        GlobalLog.err("parseOpenOrCloseAction 未知操作: $p")
        return false
//        }
    }

    /**
     * 解析标识符
     */
    private fun openByIdentifier(it: MarkedData, follow: String?): Boolean {
        return when (it.type) {
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
                serviceBridge?.onExecuteInterrupt("终止执行")
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

    override fun waitForText(text: Array<String>, m: Long): ViewNode? {
        Vog.d(this, "waitForText $text")
        if (!checkAccessibilityService()) {
            return null
        }
        accessApi?.waitForView(this, ViewFindBuilder(this).containsText(*text).viewFinderX)
        waitForUnlock(m)
        return getViewNode()
    }

    /**
     * 等待语音参数
     */
    override fun waitForVoiceParam(askWord: String?): String? {

        serviceBridge?.getVoiceParam(currentAction!!)
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
        serviceBridge?.speak(text)
    }

    override fun speakSync(text: String): Boolean {
        serviceBridge?.speakSync(text)
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
    fun pressBack(): Boolean = GlobalActionExecutor.back()

    /**
     * 最近界面
     */
    fun openRecent(): Boolean = GlobalActionExecutor.recents()

    /**
     * 主页
     */
    fun goHome(): Boolean = GlobalActionExecutor.home()

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
                waitForSingleChoice("选择要标识的联系人", AdvanContactHelper.getChoiceData())
            if (choiceData != null) {
                //开启线程
                thread {
                    //保存标记
                    val data = choiceData.originalData as ContactInfo
                    val marked = MarkedData(s, MarkedData.MARKED_TYPE_CONTACT, s, choiceData.subtitle, DataFrom.FROM_USER)
//                    marked.key = s
//                    marked.regStr = s
//                    marked.value = choiceData.subtitle
//                    marked.from = DataFrom.FROM_USER
//                    marked.type = MarkedData.MARKED_TYPE_CONTACT
                    AdvanContactHelper.addMark(marked)
                }
                val sss = systemBridge.call(choiceData.subtitle!!)
                PartialResult(sss.ok, if (!sss.ok) sss.errMsg else "")
            } else {
                PartialResult(false, "取消")
            }
        } else PartialResult.success()
    }

    override fun setScreenSize(width: Int, height: Int) {
        ScreenAdapter.setScreenSize(width, height)
    }

    companion object {
        /**
         * @return 参数可用
         */
        fun checkParam(p: String?): Boolean = (p != null && p.trim() != "")

        val DEBUG_SCRIPT = "DEBUG"
        /**
         * 检测包名正则
         */
        private val globalScopeType = arrayListOf(ActionNode.NODE_SCOPE_GLOBAL/*, ActionNode.NODE_SCOPE_GLOBAL_2*/)

        private const val CloseAppScript = "require 'accessibility'\n" +
                "system.openAppDetail(args[1])\n" +
                "s = ViewFinder().equalsText({'强行停止','force stop'}).waitFor(3000)\n" +
                "a=s.tryClick()\n" +
                "if(not a) then \n" +
                "speak('应用未运行')\n" +
                "else\n" +
                "ViewFinder().equalsText({'确定','OK'}).waitFor(2000).tryClick()\n" +
                "home()" +
                "end\n"

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
                GlobalActionExecutor.setService(accessApi!!.getService())
                true
            } else {
                GlobalLog.log(context.getString(R.string.text_acc_service_not_running))
                false
            }
        }
        return true
    }
}
