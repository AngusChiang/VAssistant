package cn.vove7.executorengine

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.CallSuper
import cn.vove7.common.NeedAccessibilityException
import cn.vove7.common.NotSupportException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.model.ExecutorStatus
import cn.vove7.common.bridges.*
import cn.vove7.common.bridges.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_SCRIPT_JS
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_SCRIPT_LUA
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.Action.SCRIPT_TYPE_JS
import cn.vove7.common.datamanager.parse.model.Action.SCRIPT_TYPE_LUA
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.executor.CExecutorI.Companion.DEBUG_SCRIPT
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_EMPTY_QUEUE
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_FAILED
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_INTERRUPT
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_NOT_FINISH
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_NOT_SUPPORT
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_SUCCESS
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.helper.startable
import cn.vove7.common.model.MatchedData
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.*
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.common.view.notifier.ActivityShowListener
import cn.vove7.executorengine.model.ActionParseResult
import cn.vove7.executorengine.parse.OpenAppAction
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.quantumclock.QuantumClock
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import java.io.Closeable
import java.util.*
import kotlin.concurrent.thread

/**
 *
 * 基础执行器
 * 负责: waitFoeApp
 * sleep()
 * currentAction
 * command
 * DEBUG
 * focusView
 * userInterrupt
 * TODO api 提取
 * Created by Vove on 2018/6/20
 */
open class ExecutorImpl(
        val context: Context = GlobalApp.APP,
        val serviceBridge: ServiceBridge = ServiceBridge.instance
) : CExecutorI, Closeable {

    private val systemBridge = SystemBridge
    private val accessApi: AccessibilityApi?
        get() = AccessibilityApi.accessibilityService
    private var lock = Object()
    var currentAction: Action? = null
    override var command: String? = null
    override var DEBUG: Boolean = false
    override val focusView: ViewNode?
        get() = AccessibilityApi.accessibilityService?.currentFocusedEditor
    override var running: Boolean = false

    /**
     * 存放运行传递参数
     */
    private val tmpMap = hashMapOf<String, Any?>()

    override var userInterrupted: Boolean = false
        set(value) {
            Vog.d("终止标志 ---> $value")
            field = value
        }

    var userInterrupt: Boolean
        get() = userInterrupted
        set(value) {
            userInterrupted = value
        }

    override fun requireAccessibility() {
        if (accessApi == null) {
            PermissionUtils.gotoAccessibilitySetting2(context, Class.forName("cn.vove7.jarvis.services.MyAccessibilityService"))
            GlobalApp.toastError("此操作需要VAssist基础服务，请开启后继续", 1)
            throw NeedAccessibilityException()
        }
    }

    fun waitAccessibility(): Boolean {
        return waitAccessibility(30000)
    }

    override fun waitAccessibility(waitMillis: Long): Boolean {
        return AccessibilityApi.waitAccessibility(waitMillis)
    }

    /**
     * # 等待app|Activity表
     * - [CExecutorI] 执行器
     * - pair.first pkg
     * - pair.second activity
     */
    private val locksWaitForActivity = mutableMapOf<ActivityShowListener, ActionScope>()

    /**
     *  Activity监听器
     */
    private val activityNotifier by lazy { AppChangNotifier(locksWaitForActivity) }

    init {
        ScreenAdapter.init(systemBridge)
        activityNotifier.register()
    }

    private lateinit var actionQueue: PriorityQueue<Action>

    override var actionCount: Int = -1
    override var commandType: Int = 0

    override var currentActionIndex: Int = -1
    override var actionScope: Int? = null

    override val currentApp: ActionScope?
        get() = accessApi?.currentScope

    override fun isGlobal(): Boolean =
            globalScopeType.contains(actionScope)

    override val currentActivity: String?
        get() {
            val r = checkAccessibilityService(false)
            return if (r) {
                accessApi?.currentActivity
            } else {
                ""
            }
        }

    /**
     * 脚本线程
     */
    private var thread: Thread? = null

    override fun execQueue(cmdWords: String, actionQueue: PriorityQueue<Action>, sync: Boolean): Int {
        Vog.d("执行队列 ${actionQueue.size}")
        if (actionQueue.isEmpty()) return EXEC_CODE_EMPTY_QUEUE

        thread?.apply {
            if (isAlive) this.interrupt()
        }
        this.command = cmdWords
        DEBUG = (cmdWords == DEBUG_SCRIPT) //DEBUG

        this.actionQueue = actionQueue
        lock = Object()
        val waiter = ResultBox<Int>()
        if (!sync) waiter.setAndNotify(EXEC_CODE_NOT_FINISH)
        thread = thread(start = true, name = "脚本线程：$cmdWords", isDaemon = true, priority = Thread.MAX_PRIORITY) {
            var er = EXEC_CODE_FAILED
            var startTime = SystemClock.elapsedRealtime()
            try {
                AppBus.post(ExecutorStatus.begin(cmdWords))
                LooperHelper.prepareIfNeeded()
                running = true
                userInterrupt = false
                commandType = 0
                currentActionIndex = 0
                actionCount = actionQueue.size
                er = pollActionQueue(actionQueue)
                onFinish(er)
                currentAction = null
            } finally {
                if (sync) waiter.setAndNotify(er)
                else AppBus.post(ExecutorStatus.finish(er))
            }
            var endTime = SystemClock.elapsedRealtime()
            Vog.d("指令[$cmdWords] 用时: ${(endTime - startTime)}ms")
            Looper.myLooper()?.quitSafely()
        }
        return waiter.blockedGet(true) ?: EXEC_CODE_FAILED
    }

    internal val currentQueue = PriorityQueue<Action>()

    override fun addQueue(act: Action) {
        Vog.d("addQueue $act")
        currentQueue.add(act)
    }

    override fun addQueue(act: PriorityQueue<Action>) {
        Vog.d("addQueue ${act.size}")
        currentQueue.addAll(act)
    }

    /**
     * 执行队列
     *
     */
    private fun pollActionQueue(q: PriorityQueue<Action>): Int {
        currentQueue.clear()
        currentQueue.addAll(q)
        var r: Pair<Int, String?>
        while (currentQueue.isNotEmpty()) {
            currentActionIndex++
            if (!userInterrupt) {
                currentQueue.poll().apply {
                    currentAction = this

                    actionScope = this.actionScopeType
                    Vog.d("pollActionQueue ---> $actionScope")
                    r = runScript(this.actionScript, scriptType, this.param)// 清除参数缓存
                    if (r.first != EXEC_CODE_SUCCESS) {
                        return r.first
                    }
                }
            } else {
                Vog.i("pollActionQueue 终止")
                currentQueue.clear()
                return EXEC_CODE_INTERRUPT
            }
        }
        return if (userInterrupt) EXEC_CODE_INTERRUPT else EXEC_CODE_SUCCESS
    }

    override fun runScript(script: String, type: String, argMap: Map<String, Any?>?): Pair<Int, String?> {
        Vog.d("runScript arg : $argMap\n$script")
        return when (type) {
            SCRIPT_TYPE_LUA -> {
                onLuaExec(script, argMap)
            }
            SCRIPT_TYPE_JS -> {
                onRhinoExec(script, argMap)
            }
            else -> EXEC_CODE_NOT_SUPPORT to "未知脚本类型: "
        }
    }

    open fun onLuaExec(script: String, argMap: Map<String, Any?>?): Pair<Int, String?> = throw NotSupportException()


    open fun onRhinoExec(script: String, argMap: Map<String, Any?>?): Pair<Int, String?> = throw NotSupportException()


    fun executeFailed() {
        executeFailed(null)
    }

    override fun executeFailed(msg: String?) {
        Vog.d("executeFailed ---> $msg")
//        userInterrupt = true //设置用户中断标志
        //pollActionQueue -> false
    }

    /**
     * 资源释放
     */
    override fun onFinish(resultCode: Int) {
        running = false
        thread = null
        ScreenAdapter.reSet()
        SystemBridge.release()
        InputMethodBridge.restore()
    }

    /**
     * 中断操作
     */
    @CallSuper
    override fun interrupt() {
        userInterrupt = true
        thread?.interrupt()//打破wait
        Vog.d("外部终止运行")
    }

    /**
     * 程序内及脚本api
     * @param data String 包名 应用名 标记功能
     * 同时负责解析应用内跟随操作
     * 往往: 打开网易云 | 打开QQ扫一扫 | 酷安下载微信
     *
     * @return Boolean 是否解析成功
     */
    override fun smartOpen(data: String): Boolean {
        Vog.d("smartOpen: $data")
        //包名
        if (RegUtils.isPackage(data)) {//包名
            return systemBridge.openAppByPkg(data).also {
                if (!it) GlobalApp.toastError(R.string.text_app_not_install)
            }
        }
        val pkg = SystemBridge.getPkgByName(data)
        if (pkg != null) {
            SystemBridge.openAppByPkg(pkg)
            return true
        }
        //By App Name
        //确定有跟随操作，clear task
        //解析应用内操作

        //先解析名称
        val o = parseAppInCommand(data)
        return if (o != null) {
            //执行
            pollActionQueue(o.actionQueue ?: PriorityQueue())
            true
        } else {
            //检查标记功能
            commandType = 1
            parseOpenOrCloseAction(data)
        }
    }

    /**
     * 返回解析成功
     * 脚本api
     * @param data String
     * @return Boolean
     */
    override fun smartClose(data: String): Boolean {
        val pkg =
                if (RegUtils.isPackage(data)) data
                else SystemBridge.getPkgByWord(data)
        return if (pkg != null) {
            SystemBridge.killApp(pkg)
//            onLuaExec(CloseAppScript, arrayOf(pkg)).isSuccess//强制停止App
        } else {
            //Marked打开
            commandType = -1
            parseOpenOrCloseAction(data)
        }
    }


    /**
     * 根据本地应用,解析用户指令内包含的应用信息
     * 匹配机制：标识 -> 按匹配率排序
     * 预解析跟随操作 ：  QQ扫一扫 (指令与APP名称) QQ浏览器  网易云播放(标记APP)
     * @param command String 用户指令
     * @return ActionParseResult
     */
    private fun parseAppInCommand(command: String): ActionParseResult? {
        val list = mutableListOf<MatchedData<ActionParseResult>>()
        //标记应用预解析匹配
        AdvanAppHelper.MARKED_APP_PKG.forEach {
            val scope = ActionScope(it.value)//

            val rs = TextHelper.matchValues(command, it.regexString())
            if (rs != null) {//匹配成功
                val follow = rs[rs.size - 1]
                Vog.d("标记预解析---> $follow")

                val aq = if (follow.isNotEmpty()) {
                    ParseEngine.parseAppActionWithScope(follow, scope, false)
                } else null
                if (aq != null) {
                    list.add(MatchedData(0.9f, aq))
                } else {//无跟随操作
                    val q = PriorityQueue<Action>()

                    //加入打开操作
                    val openAction by OpenAppAction(it.value)
                    q.add(openAction)

                    list.add(MatchedData(0.6f, ActionParseResult(true, q)))
                }
            }
        }

        //应用列表预解析匹配
        synchronized(AdvanAppHelper.ALL_APP_LIST) {

            AdvanAppHelper.ALL_APP_LIST.values.forEach {
                if (!it.startable) return@forEach
                try {
                    val name = it.name ?: ""
                    if (command.startsWith(name, ignoreCase = true)) { //如果startWith 并且解析到跟随操作. get it
                        val follow = command.substring(name.length)
                        Vog.d("预解析---> $follow")
                        val scope = ActionScope(it.packageName)
                        val aq = if (follow.isNotEmpty()) {
                            ParseEngine.parseAppActionWithScope(follow, scope, false)
                        } else null

                        if (aq?.isSuccess == true) {
                            list.add(MatchedData(0.91f, aq))
                        } else {//无跟随操作
                            val q = PriorityQueue<Action>()

                            //加入打开操作
                            val openAction by OpenAppAction(it.packageName)
                            q.add(openAction)
                            ActionParseResult(true, q)
                            list.add(MatchedData(0.6f, ActionParseResult(true, q)))
                        }
                    }
                } catch (e: Exception) {
                    GlobalLog.err(e.message) //记录
                    e.printStackTrace()
                }
            }
        }
        if (list.isEmpty()) return null
        list.sort()
        return list[0].data
    }

    /**
     * 匹配率下限
     */
    private var limitRate = 0.75f

    @Override
    fun finalize() {
        activityNotifier.unregister()
    }

    override fun close() {
        finalize()
    }

    /**
     * 解析标记功能
     * 脚本内根据[commandType]分别 打开/关闭
     */
    private val markedOpenDao get() = DAO.daoSession.markedDataDao

    private fun parseOpenOrCloseAction(p: String): Boolean {
        /*val marked = markedOpenDao.queryBuilder()
                .where(MarkedOpenDao.Properties.Key.like(p)).unique()
         if (marked != null) {//通过Key查询到
            //处理follow命令
            p.indexOf()
           return openByIdentifier(marked)
        } else {*/
        markedOpenDao.queryBuilder().where(MarkedDataDao.Properties.Type.`in`(
                MARKED_TYPE_SCRIPT_LUA,
                MARKED_TYPE_SCRIPT_JS
        )).list().forEach {
            val result = it.rawRegex().matchEntire(p)//标记功能 严格匹配
            if (result != null) {
                return openByIdentifier(it)//执行
            }
        }
        Vog.d("parseOpenOrCloseAction ---> 未知操作: $p")
        return false
    }

    /**
     * 解析标识符
     */
    private fun openByIdentifier(it: MarkedData): Boolean {
        return when (it.type) {
            MARKED_TYPE_SCRIPT_LUA -> {
                onLuaExec(it.value, null)
                true
            }
            MARKED_TYPE_SCRIPT_JS -> {
                onRhinoExec(it.value, null)
                true
            }
            else -> {
                GlobalLog.err("openByIdentifier -> 未知Type $it")
                false
            }
        }
    }

    override fun notifyActivityShow(scope: ActionScope) {
        Vog.d("notifyActivityShow $scope")
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
        serviceBridge.getVoiceParam()
        waitForUnlock()
        //得到结果 -> action.param
        return tmpMap[VOICE_RESULT] as String?
    }

    /**
     * 得到语音参数,赋值
     */
    override fun onGetVoiceParam(param: String?) {
        Vog.d("onGetVoiceParam -> $param")
        //设置参数
        tmpMap[VOICE_RESULT] = param
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
            val begin = QuantumClock.currentTimeMillis // 开始等待时间
            Vog.d("执行器-等待 time: $mm   begin: $begin")

            //等待结果
            try {
                lock.wait(mm)
                val end = QuantumClock.currentTimeMillis
                Vog.d("执行器-解锁")
                if (end - begin >= mm) {//自动超时 终止执行
                    Vog.d("等待超时")
                    return false
                }
                return true

            } catch (e: InterruptedException) {
                e.printStackTrace()
                //必须强行stop
                Vog.d("被迫强行停止")
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
        Vog.d("waitForApp $pkg $activityName")
        if (!checkAccessibilityService()) {
            return false
        }

        waitForActivity(ActionScope(pkg, activityName))
        return waitForUnlock(m)
    }

    private fun waitForActivity(scope: ActionScope) {
        locksWaitForActivity[this] = scope
        CoroutineExt.launch {
            //开线程
            Thread.sleep(200)
            try {//主动调用一次
                activityNotifier.onAppChanged(accessApi?.currentScope!!)
            } catch (e: Exception) {
            }
        }
    }

    override fun waitForViewId(id: String, m: Long): ViewNode? {
        Vog.d("waitForViewId $id $m")
        return ViewFindBuilder().id(id).waitFor(m)
    }

    override fun waitForDesc(desc: String, m: Long): ViewNode? {
        Vog.d("waitForDesc $desc")
        return ViewFindBuilder().desc(desc).waitFor(m)
    }

    override fun waitForText(text: String, m: Long): ViewNode? {
        Vog.d("waitForText $text")
        return ViewFindBuilder().containsText(text).waitFor(m)
//        waitForUnlock(m)
//         getViewNode()
    }

    override fun waitForText(text: Array<String>, m: Long): ViewNode? {
        Vog.d("waitForText $text")
        if (!checkAccessibilityService()) {
            return null
        }
        return ViewFindBuilder().containsText(*text).waitFor(m)
    }

    /**
     * 等待单选结果
     * @return if 调用成功 true else false
     */
    override fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): ChoiceData? {
        //通知显示单选框
        serviceBridge.showChoiceDialog(ShowDialogEvent(WHICH_SINGLE, askTitle, choiceData))
        waitForUnlock()

        return (tmpMap.getAndRemove(SINGLE_CHOICE_RESULT) as ChoiceData?).also {
            Vog.d("结果： ${it?.title}")
        }
    }

    override fun onSingleChoiceResult(index: Int, data: ChoiceData?) {
        tmpMap[SINGLE_CHOICE_RESULT] = data
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
        serviceBridge.showAlert(title, msg)
        waitForUnlock()

        return (tmpMap[ALERT_RESULT] as Boolean?).also {
            Vog.d("alert result > $it")
        } ?: false
    }

    override fun notifyAlertResult(result: Boolean) {
        tmpMap[ALERT_RESULT] = result
        notifySync()
    }

    override fun speak(text: String) {
        serviceBridge.speak(text)
    }

    override fun speakSync(text: String): Boolean {
        val resultBox = ResultBox<String?>()
        serviceBridge.speakWithCallback(text) {
            resultBox.setAndNotify(it)
        }
        return resultBox.blockedGet() != null
    }

    override fun removeFloat() {
        serviceBridge.removeFloat()
    }

    /**
     * 实现SpeakCallback
     * @param p1 String?
     */
    override fun invoke(p1: String?) {
        tmpMap[SPEAK_CALLBACK] = p1
        notifySync()
    }

    override fun setScreenSize(width: Int, height: Int) {
        ScreenAdapter.setScreenSize(width, height)
    }

    companion object {

        /**
         * 检测包名正则
         */
        private val globalScopeType = arrayListOf(ActionNode.NODE_SCOPE_GLOBAL/*, ActionNode.NODE_SCOPE_GLOBAL_2*/)

        const val ALERT_RESULT = "ALERT_RESULT"
        const val SPEAK_CALLBACK = "SPEAK_CALLBACK"
        const val VOICE_RESULT = "VOICE_RESULT"
        const val SINGLE_CHOICE_RESULT = "SINGLE_CHOICE_RESULT"

    }

    override fun sleep(millis: Long) {
        Thread.sleep(millis)
    }

    override fun checkAccessibilityService(jump: Boolean): Boolean {
        return if (!AccessibilityApi.isBaseServiceOn) {
            if (jump) {
                AppBus.post(RequestPermission("基础无障碍服务"))
            }
            false
        } else true
    }

}


