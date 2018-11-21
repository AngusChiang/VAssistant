package cn.vove7.executorengine

import android.content.Context
import android.support.annotation.CallSuper
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.common.bridges.GlobalActionExecutor
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.bridges.ShowDialogEvent
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
import cn.vove7.common.executor.CExecutorI.Companion.DEBUG_SCRIPT
import cn.vove7.common.executor.PartialResult
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.LooperHelper
import cn.vove7.common.utils.RegUtils
import cn.vove7.common.utils.ScreenAdapter
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.AdvanContactHelper
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.vtp.log.Vog
import java.util.*
import kotlin.concurrent.thread

/**
 *
 * 基础执行器
 * Created by Vove on 2018/6/20
 */
open class ExecutorImpl(
        val context: Context = GlobalApp.APP,
        val serviceBridge: ServiceBridge? = GlobalApp.serviceBridge
) : CExecutorI {

    private val systemBridge = SystemBridge
    val accessApi: AccessibilityApi?
        get() = AccessibilityApi.accessibilityService
    private var lock = Object()
    var currentAction: Action? = null
    private val markedOpenDao = DAO.daoSession.markedDataDao
    //    val globalActionExecutor = GlobalActionExecutor()
    override var command: String? = null
    override var DEBUG: Boolean = false
    override val focusView: ViewNode?
        get() = AccessibilityApi.accessibilityService?.currentFocusedEditor
    override var running: Boolean = false
    override var userInterrupted: Boolean = false

    init {
        ScreenAdapter.init(systemBridge)
    }

    private lateinit var actionQueue: PriorityQueue<Action>

    override var actionCount: Int = -1
    override var commandType: Int = 0

    override var currentActionIndex: Int = -1
    override var actionScope: Int? = null

    override var currentApp: ActionScope? = null
        get() = accessApi?.currentScope

    override fun isGlobal(): Boolean =
        globalScopeType.contains(actionScope)

    override var currentActivity: String? = null
        get() {
            val r = checkAccessibilityService(false)
            return if (r) {
                accessApi?.currentActivity
            } else {
                ""
            }
        }

    /**
     * enter
     */
    private var thread: Thread? = null

    override fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>?) {
        if (actionQueue == null) return
        if (thread?.isAlive == true) {
            thread!!.interrupt()
        }
        this.command = cmdWords
        DEBUG = (cmdWords == DEBUG_SCRIPT) //DEBUG

        this.actionQueue = actionQueue
        lock = Object()
        thread = thread(start = true, isDaemon = true, priority = Thread.MAX_PRIORITY) {
            LooperHelper.prepareIfNeeded()
            running = true
            userInterrupted = false
            commandType = 0
            serviceBridge?.onExecuteStart(cmdWords)
            actionCount = actionQueue.size
            currentActionIndex = 0
            onFinish(pollActionQueue())
            currentAction = null
        }
    }

    /**
     * 执行队列
     */
    private fun pollActionQueue(): Boolean? {
        var r: PartialResult
        while (actionQueue.isNotEmpty()) {
            currentActionIndex++
            if (Thread.currentThread().isInterrupted.not()) {
                currentAction = actionQueue.poll()
                actionScope = currentAction?.actionScopeType
                Vog.d(this, "pollActionQueue ---> $actionScope")
                r = runScript(currentAction!!.actionScript, currentAction!!.param.valueWithClear
                    ?: arrayOf())// 清除参数缓存
                when {
                    r.needTerminal -> {//出错
                        currentAction = null
                        actionQueue.clear()
                        serviceBridge?.onExecuteFailed(r.msg)
                        return null
                    }
                }
            } else {
                Vog.i(this, "pollActionQueue 终止")
                actionQueue.clear()
                serviceBridge?.onExecuteInterrupt("强行终止")
                return false
            }
        }
        return !userInterrupted
    }

    override fun runScript(script: String, args: Array<String>?): PartialResult {
        Vog.d(this, "runScript arg : ${Arrays.toString(args)}")
        return when (currentAction?.scriptType) {
            SCRIPT_TYPE_LUA -> {
                onLuaExec(script, args)
            }
            SCRIPT_TYPE_JS -> {
                onRhinoExec(script, args)
            }
            else ->
                PartialResult.fatal("未知脚本类型: " + currentAction?.scriptType)
        }
    }

    open fun onLuaExec(script: String, args: Array<String>? = null): PartialResult {
        return PartialResult.fatal("not implement onLuaExec")
    }

    open fun onRhinoExec(script: String, arg: Array<String>? = null): PartialResult {
        return PartialResult.fatal("not implement onRhinoExec")
    }

    fun executeFailed() {
        executeFailed(null)
    }

    override fun executeFailed(msg: String?) {
        Vog.d(this, "executeFailed ---> $msg")
        userInterrupted = true //设置用户中断标志
        //pollActionQueue -> false
    }

    override fun onFinish(result: Boolean?) {
        running = false
        ScreenAdapter.reSet()
        if (result != null)
            serviceBridge?.onExecuteFinished(result)
        accessApi?.removeAllNotifier(this)
    }

    /**
     * 中断操作
     */
    @CallSuper
    override fun interrupt() {
        userInterrupted = true
        try {
            thread?.checkAccess()
            thread?.interrupt()//打破wait
        } catch (e: Exception) {
            e.printStackTrace()
        }
        accessApi?.removeAllNotifier(this)
    }

    /**
     * 跟随打开解析App内操作
     * 接着打开应用
     * always return true
     */
    private fun parseAppInnerOperation(cmd: String, pkg: String): Boolean {
        //解析跟随
        val appQ = ParseEngine.matchAppAction(cmd, ActionScope(pkg), true)
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
        if (r) {
            //等待App打开
            AppBus.post(AppBus.EVENT_HIDE_FLOAT)//关闭助手dialog
            val waitR = waitForApp(pkg, null, 5000)
            if (!waitR) return false
            pollActionQueue()
        }
        return true
    }

    /**
     * 返回解析成功
     * @param data String
     * @return Boolean
     */
    override fun smartOpen(data: String): Boolean {
        return smartOpen(data, null)
    }

    /**
     * 返回解析成功
     * @param data String
     * @return Boolean
     */
    override fun smartClose(data: String): Boolean {
        val pkg =
            if (RegUtils.isPackage(data)) data
            else systemBridge.getPkgByWord(data)
        return if (pkg != null)
            onLuaExec(CloseAppScript, arrayOf(pkg)).isSuccess//强制停止App
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
            val result = it.rawRegex().matchEntire(p)//标记功能 严格匹配
            if (result != null) {
                return openByIdentifier(it, ParseEngine.getLastParam(result.groups))//执行
            }
        }
        AppBus.post(CommandHistory(UserInfo.getUserId(), "打开|关闭 $p", null))
        GlobalLog.log("parseOpenOrCloseAction 未知操作: $p")
        return false
//        }
    }

    /**
     * 解析标识符
     */
    private fun openByIdentifier(it: MarkedData, follow: String?): Boolean {
        return when (it.type) {
            MARKED_TYPE_SCRIPT_LUA -> {
                actionQueue = PriorityQueue()
                actionQueue.add(Action(it.value, Action.SCRIPT_TYPE_LUA))
                pollActionQueue()
                true
//                onLuaExec(it.value).isSuccess
            }
            MARKED_TYPE_SCRIPT_JS -> {
//                onRhinoExec(it.value).isSuccess
                actionQueue = PriorityQueue()
                actionQueue.add(Action(it.value, Action.SCRIPT_TYPE_JS))
                pollActionQueue()
                true
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
     * 等待语音参数
     */
    override fun waitForVoiceParam(askWord: String?): String? {
        serviceBridge?.getVoiceParam()
        waitForUnlock()
        //得到结果 -> action.param
        return if (!currentAction!!.responseResult) {
            null
//                PartialResult.fatal("获取语音参数失败")
        } else {
            currentAction!!.param.valueWithClear[0]
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
            currentAction?.param?.value = arrayOf(param)
        }
        //继续执行
        notifySync()
    }

    /**
     * 锁定线程
     * @param millis 等待时限 -1 默认30s
     */
    override fun waitForUnlock(millis: Long): Boolean {
        val mm = if (millis < 0) 30000 else millis
        synchronized(lock) {
            val begin = System.currentTimeMillis() // 开始等待时间
            Vog.d(this, "执行器-等待 time: $mm   begin: $begin")

            //等待结果
            try {
                lock.wait(mm)
                val end = System.currentTimeMillis()
                Vog.d(this, "执行器-解锁")
                if (end - begin >= mm) {//自动超时 终止执行
                    Vog.d(this, "等待超时")
                    accessApi?.removeAllNotifier(this)//移除监听器
                    return false
                }
                return true

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
     * 等待单选结果
     * @return if 调用成功 true else false
     */
    override fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): ChoiceData? {
        //通知显示单选框
        serviceBridge?.showChoiceDialog(ShowDialogEvent(WHICH_SINGLE, askTitle, choiceData))
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

    override fun onSingleChoiceResult(index: Int, data: ChoiceData?) {
        currentAction?.responseResult = data != null
        currentAction?.responseBundle?.putSerializable("data", data)
        notifySync()
    }

    override fun singleChoiceDialog(askTitle: String, choiceData: Array<String>): Int? {
        return waitForSingleChoice(askTitle, array2ChoiceData(choiceData))?.index
    }

    fun singleChoiceDialog(askTitle: String, choiceData: Array<Pair<String, String?>>): Int? {
        return waitForSingleChoice(askTitle, pair2ChoiceData(choiceData))?.index
    }

    fun pair2ChoiceData(choiceDatas: Array<Pair<String, String?>>): List<ChoiceData> {
        val list = mutableListOf<ChoiceData>()
        choiceDatas.forEach {
            list.add(ChoiceData(it.first, subtitle = it.second))
        }
        return list
    }

    /**
     * 数组转
     * @param arr Array<String>
     * @return List<ChoiceData>
     */
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
        serviceBridge?.showAlert(title, msg)
        waitForUnlock()
        return currentAction!!.responseResult.also {
            Vog.d(this, "alert result > $it")
        }
    }

    override fun notifyAlertResult(result: Boolean) {
        currentAction!!.responseResult = result
        notifySync()
    }

    override fun speak(text: String) {
        serviceBridge?.speak(text)
    }

    override fun speakSync(text: String): Boolean {
        if (serviceBridge?.speakSync(text) == false) {
            return true
        }
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
    fun smartCallPhone(s: String): Boolean {//todo 脚本内实现
        Vog.d(this, "smartCallPhone $s")
        val result = systemBridge.call(s)
        return if (!result) {
            if (!alert("未识别该联系人", "选择是否标记该联系人: $s")) {
                return false
            }
            //标识联系人
            val choiceData =
                waitForSingleChoice("选择要标识的联系人", AdvanContactHelper.getChoiceData())
            if (choiceData != null) {
                //开启线程
                runOnPool {
                    //保存标记
//                    val data = choiceData.originalData as ContactInfo
                    val marked = MarkedData(s, MarkedData.MARKED_TYPE_CONTACT, s, choiceData.subtitle, DataFrom.FROM_USER)
                    AdvanContactHelper.addMark(marked)
                }
                val sss = systemBridge.call(choiceData.subtitle!!)
                sss
            } else {
                false
            }
        } else true
    }

    override fun setScreenSize(width: Int, height: Int) {
        ScreenAdapter.setScreenSize(width, height)
    }

    companion object {
        /**
         * @return 参数可用
         */
        fun checkParam(p: String?): Boolean = (p != null && p.trim() != "")

        /**
         * 检测包名正则
         */
        private val globalScopeType = arrayListOf(ActionNode.NODE_SCOPE_GLOBAL/*, ActionNode.NODE_SCOPE_GLOBAL_2*/)

        //todo 脚本内实现
        private const val CloseAppScript = "require 'accessibility'\n" +
                "system.openAppDetail(args[1])\n" +
                "s = ViewFinder().equalsText({'强行停止','force stop'}).waitFor(3000)\n" +
                "if (s and s.tryClick()) then\n" +
                "    ok = ViewFinder().containsText({\"确定\", \"OK\"}).waitFor(600)\n" +
                "    if (ok) then\n" +
                "        sleep(200)\n" +
                "        print(ok.tryClick())\n" +
                "    else\n" +
                "        ok = ViewFinder().containsText(\"强行停止\").waitFor(600)\n" +
                "        if(ok) then\n" +
                "            ok.tryClick()\n" +
                "        end\n" +
                "    end\n" +
                "else\n" +
                "    speak(\"应用未运行\")\n" +
                "end\n" +
                "sleep(500)\nhome()\n"

    }

    override fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            //必须强行stop
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
        val ac = accessApi
        return if (ac != null) {
            true
        } else {
            GlobalLog.log(context.getString(R.string.text_acc_service_not_running))
            false
        }
    }
}
