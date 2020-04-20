package cn.vove7.jarvis.services

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.SystemClock
import cn.vove7.common.MessageException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.ACTION_BEGIN_SCREEN_PICKER
import cn.vove7.common.appbus.AppBus.ACTION_BEGIN_SCREEN_PICKER_TRANSLATE
import cn.vove7.common.appbus.AppBus.ACTION_CANCEL_RECOG
import cn.vove7.common.appbus.AppBus.ACTION_RELOAD_SYN_CONF
import cn.vove7.common.appbus.AppBus.ACTION_START_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.ACTION_START_RECOG
import cn.vove7.common.appbus.AppBus.ACTION_START_RECOG_SILENT
import cn.vove7.common.appbus.AppBus.ACTION_START_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP_TIMER
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH
import cn.vove7.common.appbus.AppBus.ACTION_STOP_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.ACTION_STOP_EXEC
import cn.vove7.common.appbus.AppBus.ACTION_STOP_RECOG
import cn.vove7.common.appbus.AppBus.ACTION_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP_TIMER
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP_WITHOUT_SWITCH
import cn.vove7.common.appbus.model.ExecutorStatus
import cn.vove7.common.appbus.model.ExecutorStatus.Companion.ON_EXECUTE_FINISHED
import cn.vove7.common.appbus.model.ExecutorStatus.Companion.ON_EXECUTE_START
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.bridges.ShowDialogEvent
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_EMPTY_QUEUE
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_FAILED
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_INTERRUPT
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_NOT_SUPPORT
import cn.vove7.common.executor.CExecutorI.Companion.EXEC_CODE_SUCCESS
import cn.vove7.common.interfaces.SpeakCallback
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VoiceRecogResult
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.utils.*
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.common.utils.RegUtils.checkCancel
import cn.vove7.common.utils.RegUtils.checkConfirm
import cn.vove7.common.view.finder.ScreenTextFinder
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.executorengine.exector.ExecutorEngine
import cn.vove7.executorengine.model.ActionParseResult
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PermissionManagerActivity
import cn.vove7.jarvis.activities.TextOcrActivity
import cn.vove7.jarvis.chat.ChatSystem
import cn.vove7.jarvis.chat.TulingChatSystem
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.shs.ISmartHomeSystem
import cn.vove7.jarvis.speech.*
import cn.vove7.jarvis.speech.baiduspeech.BaiduSpeechRecogService
import cn.vove7.jarvis.speech.baiduspeech.BaiduSpeechSynService
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.tools.DataCollector
import cn.vove7.jarvis.tools.baiduaip.model.Point
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.tools.setFloat
import cn.vove7.jarvis.view.dialog.MultiChoiceDialog
import cn.vove7.jarvis.view.dialog.OnMultiSelectListener
import cn.vove7.jarvis.view.dialog.OnSelectListener
import cn.vove7.jarvis.view.dialog.SingleChoiceDialog
import cn.vove7.jarvis.view.floatwindows.*
import cn.vove7.jarvis.view.statusbar.ExecuteAnimation
import cn.vove7.jarvis.view.statusbar.ListeningAnimation
import cn.vove7.jarvis.view.statusbar.ParseAnimation
import cn.vove7.vtp.log.Vog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.SubscriberExceptionEvent
import org.greenrobot.eventbus.ThreadMode
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


/**
 * 主服务
 */
object MainService : ServiceBridge, OnSelectListener, OnMultiSelectListener {

    fun start() {
        GlobalLog.log("主服务上线")
    }

    /**
     * 正常语音模式
     */
    const val MODE_VOICE = 858

    /**
     * 执行期间获取"语音参数"
     */
    const val MODE_GET_PARAM = 72

    /**
     * 确认对话框语音模式
     */
    const val MODE_ALERT = 27

    /**
     * 语音输入
     */
    const val MODE_INPUT = 127

    /**
     * 语音事件数据类型
     */
    val context = GlobalApp.APP

    private lateinit var floatyPanel: IFloatyPanel

    //正在解析指令
    private val parsingCommand get() = parseJob != null

//    override val serviceId: Int
//        get() = 126

    /**
     * 执行器
     */
    private val cExecutor: CExecutorI by lazy { ExecutorEngine() }

    private lateinit var chatSystem: ChatSystem

    //启动过慢  lateinit 导致未初始化异常
    var speechRecogService: SpeechRecogService? = null
    var speechSynService: SpeechSynService? = null
    var homeControlSystem: ISmartHomeSystem? = null

    /**
     * 当前语音使用方式
     */
    private var voiceMode = MODE_VOICE

    //识别过程动画
    private val listeningAni: ListeningAnimation by lazy { ListeningAnimation() }
    private val parseAnimation: ParseAnimation by lazy { ParseAnimation() }
    val executeAnimation: ExecuteAnimation by lazy { ExecuteAnimation() }

    init {
        ServiceBridge.instance = this
        runOnNewHandlerThread("load_speech_engine") {
            init()
        }
    }

    var speechEngineLoaded = false

    fun init() {
        AppBus.reg(this)
        loadFloatPanel()
        loadChatSystem()
        loadSpeechService()
        speechEngineLoaded = true
        loadHomeSystem()
    }

    fun loadFloatPanel(type: Int = AppConfig.panelStyle) {
        floatyPanel = when (type) {
            0 -> DefaultPanel()
            1 -> OldFloatPanel()
            2 -> CustomPanel()
            3 -> GooglePanel()
            4 -> AlignPanel()
            else -> DefaultPanel()
        }
    }

    fun loadHomeSystem(type: Int? = AppConfig.homeSystem) {
        homeControlSystem = ISmartHomeSystem.load(type)?.also {
            runOnNewHandlerThread { it.init() }
        }
    }

    /**
     * 加载语音识别/合成服务
     */
    fun loadSpeechService(type: Int? = null, notify: Boolean = false) {
        val st = if (type == 1 && !AppLogic.canXunfei()) 0
        else type ?: AppConfig.speechEngineType

        val loaded = releaseSpeechService()

        when (st) {
            0 -> {//百度
                speechRecogService = BaiduSpeechRecogService(RecogEventListener())
                speechSynService = BaiduSpeechSynService(SynthesisEventListener())
            }
//            1 -> {//讯飞
//                speechRecogService = XunfeiSpeechRecogService(RecogEventListener())
//                speechSynService = XunfeiSpeechSynService(SynthesisEventListener())
//            }
            else -> {
                speechRecogService = BaiduSpeechRecogService(RecogEventListener())
                speechSynService = BaiduSpeechSynService(SynthesisEventListener())
            }
        }
        if (loaded && notify) {
            GlobalApp.toastInfo("重载语音引擎完成")
        }
    }

    private fun releaseSpeechService(): Boolean {
        val load = speechRecogService != null
        speechRecogService?.release()
        speechSynService?.release()
        if (load) sleep(1500)
        return load
    }

    /**
     * 加载对话系统
     */
    fun loadChatSystem(open: Boolean = AppConfig.openChatSystem) {

        if (!open) return
//        chatSystem = when (AppConfig.chatSystem) {
//            type[0] -> QykChatSystem()
//            type[1] -> TulingChatSystem()
//            else -> QykChatSystem()
//        }
        chatSystem = TulingChatSystem()
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSubException(e: SubscriberExceptionEvent) {
        Vog.d("onSubException ---> $e")
//        e.throwable?.printStackTrace()
        GlobalLog.err(e.throwable)
    }

    /**
     * 继续执行确认框
     */
    private var alertDialog: AlertDialog? = null

    override fun showAlert(title: String?, msg: String?) {
        if (AppConfig.lastingVoiceCommand)
            afterSpeakResumeListen = recogIsListening//记录原状态
        runOnUi {
            alertDialog = AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(R.string.text_continue) { _, _ ->
                        notifyAlertResult(true)
                    }.setNegativeButton(R.string.text_cancel) { _, _ ->
                        notifyAlertResult(false)
                    }.create()
            try {
                alertDialog?.setFloat()
                alertDialog?.show()
                //语音控制
                if (AppConfig.voiceControlDialog) {
                    voiceMode = MODE_ALERT
                    speechRecogService?.startRecog()
                }
            } catch (e: Exception) {
                GlobalLog.err(e)
                onRequestPermission(RequestPermission("悬浮窗权限"))
                throw MessageException("无悬浮窗权限")
            }
        }
    }

    /**
     * 控制Alert对话框
     * @param result Boolean
     */
    fun performAlertClick(result: Boolean) {
        resumeMusicIf()
        runOnUi {
            alertDialog?.getButton(
                    if (result) DialogInterface.BUTTON_POSITIVE
                    else DialogInterface.BUTTON_NEGATIVE
            )?.performClick()
            alertDialog = null
        }
        executeAnimation.begin()
    }

    /**
     * Alert同步
     * 停止语音
     */
    private fun notifyAlertResult(r: Boolean) {
        voiceMode = MODE_VOICE
        if (AppConfig.voiceControlDialog) {
            //取消识别，也会关闭长语音
            speechRecogService?.cancelRecog()
        }
        cExecutor.notifyAlertResult(r)
    }

    /**
     * 长语音后台识别时 speak或AlertDialog 会关闭长语音
     * 聊天对话说完(speak)后 继续标志（可能有其他调用speak，则不继续）
     *
     * 标志更改 ：开始聊天对话|showAlert set 识别状态recogIsListening  取消识别时 set false
     *
     */
    var afterSpeakResumeListen: Boolean = false

    /**
     * 长语音下继续语音识别
     */
    private fun resumeListenCommandIfLasting() {//开启长语音时
        Vog.d("检查长语音 $afterSpeakResumeListen")
        if (afterSpeakResumeListen) {//防止长语音识别 继续
            speechRecogService?.startIfLastingVoice()
        }
    }

    /**
     * speak时暂时关闭 长语音识别（长语音）
     */
    private fun stopLastingRecogTemp(cancelRecog: Boolean = true) {
        if (isSpeakingResWord) {
            afterSpeakResumeListen = false
            return
        }
        if (AppConfig.lastingVoiceCommand) {
            Vog.d("speak临时 ${speechRecogService?.lastingStopped}")
            //没有手动停止 即认为处于长语音状态
            afterSpeakResumeListen = !(speechRecogService?.lastingStopped
                ?: true)
            if (cancelRecog && recogIsListening) {
                speechRecogService?.cancelRecog(false)
            }
        }
    }

    /**
     * 选择框
     */
    private var choiceDialog: Dialog? = null

    private fun hideDialog() {
        listeningAni.hideDelay()
        if (choiceDialog?.isShowing == true) {
            choiceDialog?.dismiss()
            choiceDialog = null
        }
    }


    /**
     * 选择对话框
     */
    //TODO 分离 DialogBridge
//    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun showChoiceDialog(event: ShowDialogEvent) {
        runOnUi {
            choiceDialog = when (event.whichDialog) {
                ShowDialogEvent.WHICH_SINGLE -> {
                    SingleChoiceDialog(context, event.askTitle, event.choiceDataSet, this)
                }
                ShowDialogEvent.WHICH_MULTI -> {
                    MultiChoiceDialog(context, event.askTitle, event.choiceDataSet, this)
                }
                else -> null
            }?.show()
        }
    }

    /**
     * 单选回调
     */
    override fun onSingleSelect(pos: Int, data: ChoiceData?, msg: String) {
        Vog.d("单选回调 $data")
        hideDialog()
        cExecutor.onSingleChoiceResult(pos, data)
    }


    /**
     * 多选回调
     */
    //未使用 todo 结果回调
    override fun onMultiSelect(data: List<ChoiceData>?, msg: String) {
//        messengerAction?.responseResult = data != null
        Vog.d("多选回调 $data")
//        messengerAction?.responseBundle?.putSerializable("data", data)
        hideDialog()
        cExecutor.notifySync()
    }

    /**
     * 中途获取参数
     */
    override fun getVoiceParam() {
        voiceMode = MODE_GET_PARAM
        speechRecogService?.startRecog()
    }

    private var speakSync = false
    override fun speak(text: String?) {
        speakSync = false
        speechSynService?.speak(text)
    }

    private val speakCallbacks = mutableListOf<SpeakCallback>()

    /**
     * 同步 响应词
     * @param text String?
     * @param call SpeakCallback
     */
    override fun speakWithCallback(text: String?, call: SpeakCallback) {
        speakCallbacks.add(call)
        speakSync = true
        speechSynService?.speak(text) ?: notifySpeakFinish(text, false)
    }

    override fun removeFloat() {
        floatyPanel.hideImmediately()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onExecutorStatus(es: ExecutorStatus) {
        when (es.what) {
            ON_EXECUTE_START -> onExecuteStart(es.data as? String? ?: "")
            ON_EXECUTE_FINISHED -> onExecuteFinished(es.data as? Int? ?: 0)
        }
    }

    private fun onExecuteStart(tag: String) {//
        Vog.d("开始执行 -> $tag")
        executeAnimation.begin()
        executeAnimation.show(tag)
    }

    /**
     * 执行结果回调
     * from executor 线程
     */
    private fun onExecuteFinished(resultCode: Int) {//
        Vog.d("执行器执行结束 --> $resultCode")
        if (!speaking) {//执行完毕后，在未speak时启动长语音
            Vog.d("执行器执行结束 移除悬浮窗")
            floatyPanel.hideImmediately()
            speechRecogService?.startIfLastingVoice()
        } else {
            Vog.d("执行器执行结束 speaking")
        }
        when (resultCode) {
            EXEC_CODE_SUCCESS -> {
                executeAnimation.success()
            }
            EXEC_CODE_FAILED -> onExecuteFailed()
            EXEC_CODE_INTERRUPT -> onExecuteInterrupt()
            else -> executeAnimation.hideDelay()
        }
    }

    //from executor 线程
    private fun onExecuteFailed() {//错误信息
        GlobalApp.toastError("执行失败")
        executeAnimation.failedAndHideDelay()
        floatyPanel.hideImmediately()
    }

    private fun onExecuteInterrupt() {
        executeAnimation.failedAndHideDelay()
    }

    /**
     * 需检查 pkg 是否安装
     * @param pkg String
     * @param cmd String
     * @param argMap Map<String, Any?>?
     * @return Boolean
     */
    override fun runAppCommand(pkg: String, cmd: String, argMap: Map<String, Any?>?): Boolean {
        val node = DaoHelper.getInAppActionNodeByCmd(pkg, cmd)
        return if (node == null) {
            false
        } else {
            val action = node.action
            val q = PriorityQueue<Action>()
            q += action
            if (node.autoLaunchApp) {//自启
                ActionParseResult.insertOpenAppAction(pkg,
                        AccessibilityApi.currentScope ?: ActionScope("-", "-"), q)
            }
            action.param = argMap ?: emptyMap()
            cExecutor.addQueue(q)
            true
        }
    }

    /**
     * 测试文本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runActionQue(que: PriorityQueue<Action>) {
        runActionQue(CExecutorI.DEBUG_SCRIPT, que)
    }

    fun runActionQue(cmd: String, que: PriorityQueue<Action>) {
        cExecutor.execQueue(cmd, que, false)
    }

    /**
     * 测试脚本
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun runAction(ac: Action) {
        val q = PriorityQueue<Action>()
        q.add(ac)
        runActionQue(CExecutorI.DEBUG_SCRIPT, q)
    }

    fun getScreenText(): List<TextOcrItem>? {
        if (!AppConfig.haveTextPickPermission()) {//免费次数
            GlobalApp.toastWarning("今日次数达到上限")
            return null
        }
        if (!AccessibilityApi.isBaseServiceOn) {
            AppBus.post(RequestPermission("无障碍服务"))
            return null
        }
        DataCollector.buriedPoint("sa_3")

        return ScreenTextFinder().findAll().mapNotNull {
            val rect = it.bounds
            val points = listOf(
                    Point(rect.left, rect.top),
                    Point(rect.right, rect.top),
                    Point(rect.right, rect.bottom),
                    Point(rect.left, rect.bottom)
            )
            val t = it.text ?: it.desc()
            return@mapNotNull if (t.isNullOrBlank()) {
                null
            } else TextOcrItem(t, points, 1.0)
        }
    }

    /**
     * 立即执行的指令
     * 可使用AppBus(延迟)
     * @param order String
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onCommand(order: String) {//外部命令
        Vog.d("onCommand ---> $order")
        if (!speechEngineLoaded) {
            GlobalApp.toastWarning("引擎未就绪")
            return
        }
        thread(priority = Thread.MAX_PRIORITY) {
            when (order) {
                ACTION_STOP_EXEC -> {
                    speechRecogService?.cancelRecog()
                    speechSynService?.stop(true)
                    cExecutor.interrupt()
                    hideAll(true)
                }
                ACTION_STOP_RECOG -> {
                    speechRecogService?.stopRecog()
                }
                ACTION_CANCEL_RECOG -> {
                    speechRecogService?.cancelRecog()
                }
                ACTION_START_RECOG -> {
                    speechSynService?.stopIfSpeaking(false)
                    speechRecogService?.startRecog()
                }
                ACTION_START_RECOG_SILENT -> {
                    speechRecogService?.startRecog(notify = false)
                }
                ACTION_START_VOICE_WAKEUP_WITHOUT_NOTIFY -> {//不重新计时
                    speechRecogService?.startWakeUp(notify = false, resetTimer = false)
                }
                ACTION_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY -> {
                    speechRecogService?.stopWakeUp(notify = false, stopTimer = false)
                }
                ACTION_START_DEBUG_SERVER -> {
                    RemoteDebugServer.start()
                }
                ACTION_STOP_DEBUG_SERVER -> {
                    RemoteDebugServer.stop()
                }
                //文字提取 需要无遮挡
                ACTION_BEGIN_SCREEN_PICKER -> {
                    val list = getScreenText() ?: return@thread

                    if (list.isEmpty()) {
                        GlobalApp.toastInfo("未提取到任何内容")
                    } else {
                        TextOcrActivity.start(context, list as ArrayList<TextOcrItem>)
                    }
                }
                ACTION_BEGIN_SCREEN_PICKER_TRANSLATE -> {
                    val list = getScreenText() ?: return@thread

                    if (list.isEmpty()) {
                        GlobalApp.toastInfo("未提取到任何内容")
                    } else {
                        TextOcrActivity.start(context, list as ArrayList<TextOcrItem>, bundle("t" to "t"))
                    }
                }
                ACTION_START_WAKEUP -> {
                    AppConfig.voiceWakeup = true
                    speechRecogService?.startWakeUp()
                }
                ACTION_START_WAKEUP_WITHOUT_SWITCH -> {
                    speechRecogService?.startWakeUp()
                }
                ACTION_STOP_WAKEUP -> {
                    AppConfig.voiceWakeup = false
                    speechRecogService?.stopWakeUp()
                }
                ACTION_STOP_WAKEUP_WITHOUT_SWITCH -> {
                    speechRecogService?.stopWakeUp()
                }
                ACTION_RELOAD_SYN_CONF -> {
                    speechSynService?.reload()
                }
                ACTION_START_WAKEUP_TIMER -> {
                    speechRecogService?.startAutoSleepWakeup()
                }
                ACTION_STOP_WAKEUP_TIMER -> {
                    speechRecogService?.stopAutoSleepWakeup()
                }

            }
        }
    }

    /**
     * 请求权限
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onRequestPermission(r: RequestPermission) {
        val intent = Intent(context, PermissionManagerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("pName", r.permissionName)
        intent.putExtra("removeFromTask", true)
        startActivity(intent)
    }

    val wpTimerEnd
        get() = (speechRecogService?.wakeupTimerEnd == true).also {
            Vog.d("语音唤醒定时器状态：${speechRecogService?.wakeupTimerEnd}")
        }
    val wakeupOpen
        get() = (speechRecogService?.wakeupI?.opened == true).also {
            Vog.d("语音唤醒状态：$it")
        }


    val recogIsListening: Boolean
        get() {
            return if (!speechEngineLoaded) {//未加载
                GlobalApp.toastWarning("引擎未就绪")
                false
            } else speechRecogService?.isListening == true
        }
    val exEngineRunning: Boolean
        get() {
            return if (!speechEngineLoaded) {//未加载
                GlobalApp.toastWarning("引擎未就绪")
                false
            } else cExecutor?.running
        }

    /**
     * 语音合成speaking
     */
    val speaking: Boolean
        get() {
            return if (!speechEngineLoaded) {//未加载
                GlobalApp.toastWarning("引擎未就绪")
                false
            } else speechSynService?.speaking == true
        }

    /**
     * 切换识别
     */
    fun switchRecog() {
        //等待时防止阻塞主线程
        launch {
            if (!speechEngineLoaded) {//未加载成
                GlobalApp.toastWarning("正在启动")

                //在未启动时，等待5s加载完
                val b = whileWaitTime(5000) {
                    if (speechEngineLoaded) Unit
                    else {
                        sleep(200)
                        null
                    }
                }
                if (b == null) {
                    GlobalApp.toastWarning("引擎未就绪")
                    return@launch
                }
            }
            if (isSpeakingResWord && speaking) {
                Vog.d("正在响应词")
                return@launch
            } else if (parsingCommand) {
                Vog.d("正在解析指令")
                stopParse()
                return@launch
            } else {
                isSpeakingResWord = false
            }
            if (recogIsListening) {
                onCommand(ACTION_CANCEL_RECOG)
            } else {
                //防抖动
                val now = SystemClock.uptimeMillis()
                if (now - lastStartRecog > 2000) {
                    lastStartRecog = now
                } else {
                    Vog.d("switchRecog 防抖动")
                    return@launch
                }
                DataCollector.buriedPoint("wakeup")
                onCommand(ACTION_START_RECOG)
            }
        }
    }

    private var lastStartRecog = 0L

    /**
     * 供插件调用
     * @param cmd String
     */
    fun parseCommand(cmd: String, chat: Boolean = true) {
        if (!speechEngineLoaded) {//未加载
            GlobalApp.toastWarning("引擎未就绪")
            return
        }
        onParseCommand(cmd, false, chat)
    }

    /**
     * 开启语音识别输入
     */
    fun startVoiceInput() {
        if (recogIsListening) return
        voiceMode = MODE_INPUT
        onCommand(ACTION_START_RECOG)
    }

    internal fun hideAll(immediately: Boolean = false) {
        if (immediately) {
            floatyPanel.hideImmediately()
            listeningAni.hideDelay(0)
        } else {
            floatyPanel.hideDelay()
            listeningAni.hideDelay()
        }
    }


    /**
     * 解析唤醒词
     * @param w String
     * @return Boolean 是否继续识别 null 不继续 非null 继续
     */
    fun parseWakeUpCommand(w: String): Boolean? {
        if (recogIsListening) {
            Vog.d("parseWakeUpCommand ---> 正在识别")
            return null
        }
        when (w) {
            "你好小V", "你好小v", "小V同学", "小v同学" -> { //唤醒词
                return true
            }
            in AppConfig.userWakeupWord.split('#') -> { //用户唤醒词
                Vog.d("parseWakeUpCommand ---> 用户唤醒词")
                return true
            }
            "增大音量" -> {
                SystemBridge.volumeUp()
            }
            "减小音量" -> {
                SystemBridge.volumeDown()
            }
            "播放" -> runInCatch { SystemBridge.mediaResume() }
            "停止" -> runInCatch { SystemBridge.mediaStop() }
            "暂停" -> runInCatch { SystemBridge.mediaPause() }
            "上一首" -> runInCatch { SystemBridge.mediaPre() }
            "下一首" -> runInCatch { SystemBridge.mediaNext() }
            //打开电灯、关闭电灯、增大亮度、减小亮度
            //打开手电筒、关闭手电筒
            "打开手电筒", "打开电灯" -> SystemBridge.openFlashlight()
            "关闭手电筒", "关闭电灯" -> SystemBridge.closeFlashlight()
            else -> {//"截屏分享", "文字提取" 等命令
                onParseCommand(w)
            }
        }
        return null
    }


    //是否在播放响应词
    var isSpeakingResWord = false

    /**
     * 语音识别事件监听
     */
    class RecogEventListener : RecogEvent {
        override fun onWakeup(word: String?) {
            if (AppConfig.wakeupScreenWhenVw && !ScreenStatusListener.screenOn) {
                SystemBridge.screenOn()
            }
            Vog.d("onWakeup ---> 唤醒 -> $word")
            if (!ScreenStatusListener.screenOn) {
                SystemBridge.screenOn()
            }
            //解析成功  不再唤醒
            parseWakeUpCommand(word ?: "") ?: return
            Vog.d("onWakeup ---> 开始聆听 -> $word")
            //唤醒词 你好小V，小V同学 ↓
            speechRecogService?.cancelRecog(false)
            speechRecogService?.startRecog(true)
            return
        }

        private fun speakResponseWord() {
            resumeMusicLock = false //不继续播放后台，
            Vog.d("speakResponseWord 响应词 ---> ${AppConfig.responseWord}")
            val l = CountDownLatch(1)
            isSpeakingResWord = true
            speakWithCallback(AppConfig.responseWord) { result ->
                Vog.d("speakWithCallback ---> $result")
                isSpeakingResWord = false
                sleep(200)
                l.countDown()
            }

            l.await()
            Vog.d("speak finish")
        }

        /**
         * 识别反馈  响应词
         * @param byVoice Boolean
         */
        private fun recogEffect(byVoice: Boolean) {
            if (voiceMode != MODE_VOICE) {
                return
            }
            if (byVoice) {//语音唤醒
                if (AppConfig.openResponseWord && AppConfig.speakResponseWordOnVoiceWakeup) {
                    speakResponseWord()
                }
            } else {//按键 快捷 等其他方式
                if (AppConfig.openResponseWord && !AppConfig.speakResponseWordOnVoiceWakeup) {
                    speakResponseWord()
                }
            }
            Vog.d("recogEffect ---> 结束")
        }

        /**
         * 响应词，停止背景音乐
         * @param byVoice Boolean
         */
        override fun onPreStartRecog(byVoice: Boolean) {
            if (byVoice) {
                DataCollector.buriedPoint("voice_wakeup")
            }
            speechSynService?.stop()
            checkMusic()//检查后台播放
            recogEffect(byVoice)
            listeningAni.begin()//
        }

        override fun onRecogReady(silent: Boolean) {
            if (silent) {
                Vog.d("静默识别")
                return
            }
            //震动
            if (AppConfig.vibrateWhenStartRecog || voiceMode != MODE_VOICE) {//询问参数时，震动
                SystemBridge.vibrate(80L)
            }
            floatyPanel.showUserWord("开始聆听")
            Vog.d("onPreStartRecog ---> 开始识别")
        }

        override fun onResult(voiceResult: String) {//解析完成再 resumeMusicIf()?
            Vog.d("结果 --------> $voiceResult")

            when (voiceMode) {
                MODE_VOICE -> {
                    onParseCommand(voiceResult)
                }
                MODE_GET_PARAM -> {//中途参数
                    resumeMusicIf()
                    if (voiceResult == "") {//失败
                        //询问重新
                        cExecutor.onGetVoiceParam(null)
                    } else {//通知
                        cExecutor.onGetVoiceParam(voiceResult)
                    }
                    voiceMode = MODE_VOICE
                }
                MODE_ALERT -> {
                    when {
                        checkConfirm(voiceResult) -> {
                            performAlertClick(true)
                        }
                        checkCancel(voiceResult) -> {
                            performAlertClick(false)
                        }
                        else -> {
                            floatyPanel.showUserWord("重新识别")
                            onCommand(ACTION_START_RECOG)  //继续????
                        }
                    }
                }
                MODE_INPUT -> {
                    hideAll()
                    AppBus.post(VoiceRecogResult(0, voiceResult))
                    voiceMode = MODE_VOICE
                    if (AppConfig.lastingVoiceCommand)
                        onCommand(ACTION_CANCEL_RECOG)
                }

            }
        }

        private var temResult: String? = null

        override fun onTempResult(temp: String) {
            temResult = temp
            Vog.d("onTempResult ---> 临时结果 $temp")
            floatyPanel.showUserWord(temp)
            listeningAni.show(temp)
        }

        override fun onStopRecog() {
            Vog.d("onStopRecog ---> ")
            resumeMusicIf()
            //fix 百度长语音 在无结果stop，无回调
            if (AppConfig.lastingVoiceCommand &&
                    speechRecogService is BaiduSpeechRecogService && temResult == null) {
                Vog.d("onStopRecog ---> 长语音无结果")
                speechRecogService?.cancelRecog()
            }
            temResult = null
        }

        override fun onCancelRecog() {
            Vog.d("onCancelRecog ---> ")
            resumeMusicLock = true
            afterSpeakResumeListen = false
            resumeMusicIf()
            hideAll(true)
            when (voiceMode) {
                MODE_GET_PARAM -> {
                    cExecutor.onGetVoiceParam(null)
                    executeAnimation.begin()//continue
                }
                MODE_ALERT -> {
                    //do nothing
                }
                MODE_VOICE -> {
                }

                MODE_INPUT -> {
                    AppBus.post(VoiceRecogResult(-1))
                    voiceMode = MODE_VOICE
                }
            }
        }

        override fun onRecogFailed(errCode: Int) {
            floatyPanel.showAndHideDelay(RecogEvent.codeString(errCode))
            when (voiceMode) {
                MODE_VOICE -> {
                    listeningAni.hideDelay()
                    resumeMusicIf()
                }
                MODE_GET_PARAM -> {//获取参数失败
                    cExecutor.onGetVoiceParam(null)
                    voiceMode = MODE_VOICE
                    executeAnimation.begin()//continue
                }
                MODE_ALERT -> {
                    if (errCode != RecogEvent.CODE_NET_ERROR)
                        onCommand(ACTION_START_RECOG)  //继续????
                }

                MODE_INPUT -> {
                    hideAll()
                    AppBus.post(VoiceRecogResult(-1))
                    voiceMode = MODE_VOICE
                }
            }
        }

        override fun onVolume(data: VoiceData) {
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun upHis(his: CommandHistory) {
        WrapperNetHelper.uploadUserCommandHistory(his)
    }

    private var isContinousDialogue = false

    private var parseJob: Job? = null

    /**
     * 解析指令
     */
    private fun onParseCommand(
            result: String, needCloud: Boolean = true,
            chat: Boolean = true, from: Int = 0): Boolean {
        Vog.d("解析命令：$result")
        if (parsingCommand) {
            Vog.d("正在解析命令:::::::")
            return false
        }

        isContinousDialogue = false
        floatyPanel.showUserWord(result)
        parseAnimation.begin()
        floatyPanel.showParseAni()

        resumeMusicIf()

        parseJob = GlobalScope.launch {
            //优先智能家居控制系统
            if (homeControlSystem?.isSupport(result) == true) {
                homeControlSystem?.doAction(result)
                hideAll()
                return@launch
            }
            //执行状态码码
            var execCode = EXEC_CODE_NOT_SUPPORT
            var lastPosition = 0

            do {
                delay(1)
                val parseResult: ActionParseResult = ParseEngine
                        .parseAction(result, AccessibilityApi.currentScope, smartOpen, onClick, lastPosition)
                lastPosition = parseResult.lastGlobalPosition
                Vog.d("onParseCommand lastGlobalPosition: ${parseResult.msg} $lastPosition")
                delay(1)
                if (parseResult.isSuccess) {//actionQueue == null 指smartOpen, onClick 操作成功
                    val actionQueue = parseResult.actionQueue
                    if (actionQueue != null) {
                        floatyPanel.hideDelay()//执行时 消失
                        execCode = cExecutor.execQueue(result, actionQueue)
                        Vog.d("onParseCommand execCode: ${parseResult.msg} $execCode")
                    } else {//smartOpen, onClick 操作成功
                        execCode = EXEC_CODE_SUCCESS
                    }
                } else break
            } while (execCode == EXEC_CODE_NOT_SUPPORT)

            when (execCode) {
                EXEC_CODE_NOT_SUPPORT, EXEC_CODE_EMPTY_QUEUE ->//不支持的指令
                    afterInstParse(result, needCloud, chat && AppConfig.openChatSystem)
                else -> onExecuteFinished(execCode)
            }

        }.also {
            it.invokeOnCompletion {
                parseJob = null
            }
        }
        return true
    }

    private fun stopParse() {
        parseJob?.cancel()
        hideAll(true)
    }

    /**
     * 指令解析不支持后
     * 云解析、对话
     */
    private suspend fun afterInstParse(result: String, needCloud: Boolean, chat: Boolean) {
        //云解析
        if (needCloud && AppConfig.cloudServiceParseIfLocalFailed) {
            Vog.d("onParseCommand ---> 失败云解析")
            WrapperNetHelper.cloudParse(result) {
                runFromCloud(result, it)
            }
        } else if (chat) {//聊天
            doChat(result)
        } else {
            onCommandParseFailed(result)
        }
    }

    /**
     * 点击操作
     */
    private val onClick: (String) -> ActionParseResult = { cmdWord ->
        var r: ActionParseResult? = null
        if (AppConfig.useSmartOpenIfParseFailed && AccessibilityApi.isBaseServiceOn) {//失败,默认点击
            Vog.d("查找点击 $cmdWord")
            if (ViewFindBuilder().similaryText(cmdWord).findFirst()?.tryClick() == true)
                r = ActionParseResult(true, null, "smart点击 $cmdWord")

        }
        r ?: ActionParseResult(false)
    }

    /**
     * 使用smartOpen操作
     */
    private val smartOpen: (String) -> ActionParseResult = { cmdWord ->
        var r: ActionParseResult? = null
        if (AppConfig.useSmartOpenIfParseFailed) {//智能识别打开操作
            Vog.d("开启 -- smartOpen")
            if (cmdWord.isNotBlank()) {//使用smartOpen
                //设置command
                val engine = ExecutorEngine()
                val result = engine.use {
                    it.command = cmdWord
                    it.smartOpen(cmdWord)
                }
                if (result) {//成功打开
                    Vog.d("parseAction ---> MultiExecutorEngine().smartOpen(cmdWord) 成功打开")
                    hideAll(true)
                    speechRecogService?.startIfLastingVoice()
                    r = ActionParseResult(true, null, "smartOpen $cmdWord")
                } else {
                    Vog.d("smartOpen --无匹配")
                }
            }
        }
        r ?: ActionParseResult(false)
    }

    /**
     * 聊天
     * @param result String
     */
    private suspend fun doChat(result: String) {
        DataCollector.buriedPoint("chat")
        resumeMusicLock = true
        if (chatSystem.onChat(result, floatyPanel)) {

        } else {
            floatyPanel.showAndHideDelay("获取失败,详情查看日志")
            parseAnimation.failedAndHideDelay()
            resumeMusicIf()
            //检查长语音
            speechRecogService?.startIfLastingVoice()
        }

    }

    fun startActivity(intent: Intent) {
        context.startActivity(intent)
    }


    /**
     * 命令解析失败
     * @param cmd String
     */
    private fun onCommandParseFailed(cmd: String) {
        WrapperNetHelper.uploadUserCommandHistory(CommandHistory(UserInfo.getUserId(), cmd, null))
        floatyPanel.showAndHideDelay("解析失败")
        parseAnimation.failedAndHideDelay()
    }

    private fun runFromCloud(command: String, actions: List<Action>?): Boolean {
        if (actions == null || actions.isEmpty()) {
            floatyPanel.showAndHideDelay("解析失败")
            parseAnimation.failedAndHideDelay()
            return false
        }
        val que = PriorityQueue<Action>()
        actions.withIndex().forEach {
            it.value.priority = it.index
            que.add(it.value)
        }
        parseAnimation.finish()
        runActionQue(command, que)
        return true
    }


    /**
     * 语音合成事件监听
     */
    class SynthesisEventListener : SyntheEvent {

        /**
         * 出错时，根据text长度指定展示时间
         * 1000 + text.len * 500
         * @param text String?
         */
        override fun onError(text: String?) {
            thread {
                sleep(((text?.length ?: 0) * 100).toLong())
                notifySpeakFinish(text, true)
                resumeMusicIf()
            }
        }

        override fun onFinish(text: String?) {
            Vog.v("onSynData 结束")
            notifySpeakFinish(text, true)
            if (resumeMusicLock) {//
                resumeMusicIf()
            }
        }

        override fun onUserInterrupt(text: String?) {
            isSpeakingResWord = false
        }

        override fun onStart(text: String?) {
            Vog.d("onSynData 开始")
            floatyPanel.showTextResult(text ?: "")
            stopLastingRecogTemp()
            checkMusic()
        }
    }

    fun notifySpeakFinish(text: String?, isSpeak: Boolean) {
        if (speakSync) {
            speakCallbacks.forEach {
                it.invoke(text)
            }
            speakCallbacks.clear()
        }
        Vog.d("通知speak结束 $isSpeak")
        hideAll()
        if (isSpeak) {
            resumeListenCommandIfLasting()
        } else {//未说话，直接
            speechRecogService?.startIfLastingVoice()
        }
    }

    /**
     * 检测后台音乐
     * 无响应词 ：唤醒 -> (check)开始识别 -> 识别结束 -> 继续播放ifNeed
     * 合成 ：(check) 开始合成 -> 结束 -> 继续播放ifNeed
     *
     * 带响应词 ：唤醒 -> (check) [合成 响应词](不再继续) -> (不再check)开始识别 -> 识别结束 -> 继续播放ifNeed
     * 连续对话 ： 最后不再继续
     *
     * 连续 加上 speak 会误判
     */
    fun checkMusic() {
        synchronized(isMusicFocus) {
            if (!isMusicFocus) {
                SystemBridge.requestMusicFocus()
                isMusicFocus = true
            }
        }
    }

    private var isMusicFocus = false

    var resumeMusicLock = true//在获取后音频焦点后 是否 释放（speak后）

    fun resumeMusicIf() {
        synchronized(isMusicFocus) {
            if (isMusicFocus) {
                SystemBridge.removeMusicFocus()
                isMusicFocus = false
            }
        }
    }

    fun showPanelSettings(act: Activity) {
        floatyPanel.showSettings(act)
    }

}
