package cn.vove7.jarvis.services

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import cn.vove7.common.MessageException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.ACTION_BEGIN_SCREEN_PICKER
import cn.vove7.common.appbus.AppBus.ACTION_BEGIN_SCREEN_PICKER_TRANSLATE
import cn.vove7.common.appbus.AppBus.ACTION_CANCEL_RECOG
import cn.vove7.common.appbus.AppBus.ACTION_RELOAD_SYN_CONF
import cn.vove7.common.appbus.AppBus.ACTION_START_RECOG
import cn.vove7.common.appbus.AppBus.ACTION_START_RECOG_SILENT
import cn.vove7.common.appbus.AppBus.ACTION_START_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP_TIMER
import cn.vove7.common.appbus.AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH
import cn.vove7.common.appbus.AppBus.ACTION_STOP_EXEC
import cn.vove7.common.appbus.AppBus.ACTION_STOP_RECOG
import cn.vove7.common.appbus.AppBus.ACTION_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP_TIMER
import cn.vove7.common.appbus.AppBus.ACTION_STOP_WAKEUP_WITHOUT_SWITCH
import cn.vove7.common.appbus.AppBus.EVENT_FORCE_OFFLINE
import cn.vove7.common.appbus.AppBus.EVENT_START_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.EVENT_STOP_DEBUG_SERVER
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.bridges.ShowDialogEvent
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.interfaces.SpeakCallback
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VoiceRecogResult
import cn.vove7.common.netacc.WrapperNetHelper
import cn.vove7.common.utils.RegUtils.checkCancel
import cn.vove7.common.utils.RegUtils.checkConfirm
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.runWithClock
import cn.vove7.common.utils.startActivityOnNewTask
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.executorengine.exector.ExecutorEngine
import cn.vove7.executorengine.model.ActionParseResult
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.jarvis.App
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PermissionManagerActivity
import cn.vove7.jarvis.activities.ResultPickerActivity
import cn.vove7.jarvis.activities.ScreenPickerActivity
import cn.vove7.jarvis.chat.ChatSystem
import cn.vove7.jarvis.chat.TulingChatSystem
import cn.vove7.jarvis.speech.SpeechEvent
import cn.vove7.jarvis.speech.SpeechRecogService
import cn.vove7.jarvis.speech.VoiceData
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.tools.setFloat
import cn.vove7.jarvis.view.dialog.MultiChoiceDialog
import cn.vove7.jarvis.view.dialog.OnMultiSelectListener
import cn.vove7.jarvis.view.dialog.OnSelectListener
import cn.vove7.jarvis.view.dialog.SingleChoiceDialog
import cn.vove7.jarvis.view.floatwindows.FloatyPanel
import cn.vove7.jarvis.view.statusbar.ExecuteAnimation
import cn.vove7.jarvis.view.statusbar.ListeningAnimation
import cn.vove7.jarvis.view.statusbar.ParseAnimation
import cn.vove7.vtp.builder.BundleBuilder
import cn.vove7.vtp.log.Vog
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
class MainService : ServiceBridge, OnSelectListener, OnMultiSelectListener {

    val context = GlobalApp.APP

    private lateinit var floatyPanel: FloatyPanel

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

    /**
     * 当前语音使用方式
     */
    private var voiceMode = MODE_VOICE

    //识别过程动画
    private val listeningAni: ListeningAnimation by lazy { ListeningAnimation() }
    private val parseAnimation: ParseAnimation by lazy { ParseAnimation() }
    private val executeAnimation: ExecuteAnimation by lazy { ExecuteAnimation() }

    init {
        instance = this
        GlobalApp.serviceBridge = this
        runOnNewHandlerThread("load_speech_engine", delay = 1000) {
            init()
        }
    }

    var speechEngineLoaded = false

    fun init() {
        AppBus.reg(this)
        floatyPanel = FloatyPanel()
        loadChatSystem()
        loadSpeechService()
        speechEngineLoaded = true
        GlobalApp.toastInfo("启动完成")
    }

    /**
     * 加载语音识别/合成服务
     */
    private fun loadSpeechService() {
        runWithClock("加载语音识别/合成服务") {
            speechRecogService = BaiduSpeechRecogService(RecogEventListener())
            speechSynService = SpeechSynService(SynthesisEventListener())
        }
    }

    /**
     * 加载对话系统
     */
    fun loadChatSystem(open: Boolean = AppConfig.openChatSystem) {
//        val type = GlobalApp.APP.resources.getStringArray(R.array.list_chat_system)

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

    override fun onExecuteStart(tag: String) {//
        Vog.d("开始执行 -> $tag")
        executeAnimation.begin()
        executeAnimation.show(tag)
    }

    /**
     * 执行结果回调
     * from executor 线程
     */
    override fun onExecuteFinished(result: Boolean) {//
        Vog.d("执行器执行结束 --> $result")
        if (!speaking) {//执行完毕后，在未speak时启动长语音
            Vog.d("执行器执行结束 移除悬浮窗")
            floatyPanel.hideImmediately()
            speechRecogService?.startIfLastingVoice()
        } else {
            Vog.d("执行器执行结束 speaking")
        }
        if (AppConfig.execSuccessFeedback) {
            if (result) executeAnimation.success()
            else executeAnimation.failedAndHideDelay()
        } else executeAnimation.hideDelay()
    }

    //from executor 线程
    override fun onExecuteFailed(errMsg: String?) {//错误信息
        GlobalLog.err("执行出错：$errMsg")
        executeAnimation.failedAndHideDelay(errMsg)
        if (AppConfig.execFailedVoiceFeedback)
            speakWithCallback("执行失败") {

            }
        else GlobalApp.toastError("执行失败")
        floatyPanel.hideImmediately()
    }

    override fun onExecuteInterrupt(errMsg: String) {
        Vog.e(errMsg)
        executeAnimation.failedAndHideDelay()
    }

    /**
     * 测试文本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runActionQue(que: PriorityQueue<Action>) {
        runActionQue(CExecutorI.DEBUG_SCRIPT, que)
    }

    fun runActionQue(cmd: String, que: PriorityQueue<Action>) {
        cExecutor.execQueue(cmd, que)
    }

    /**
     * 测试脚本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runAction(ac: Action) {
        val q = PriorityQueue<Action>()
        q.add(ac)
        cExecutor.execQueue(CExecutorI.DEBUG_SCRIPT, q)
    }

    /**
     * 立即执行的指令
     * 可使用AppBus(延迟)
     * @param order String
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onCommand(order: String) {//外部命令
        Vog.d("onCommand ---> $order")
        if (order == EVENT_FORCE_OFFLINE) {
            AppConfig.logout()
            return
        }
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
                EVENT_START_DEBUG_SERVER -> {
                    RemoteDebugServer.start()
                }
                EVENT_STOP_DEBUG_SERVER -> {
                    RemoteDebugServer.stop()
                }
                ACTION_BEGIN_SCREEN_PICKER -> {
                    context.startActivityOnNewTask(Intent(context, ScreenPickerActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    })
                }
                ACTION_BEGIN_SCREEN_PICKER_TRANSLATE -> {
                    context.startActivityOnNewTask(Intent(context, ScreenPickerActivity::class.java).also {
                        it.putExtra("t", "t")
                        it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    })
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
                    speechSynService?.reLoad()
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
        startActivity(intent)
    }

    fun destroy() {
        if (speechEngineLoaded) {
            speechRecogService?.release()
            speechSynService?.release()
        }
        AppBus.unreg(this)
        GlobalApp.serviceBridge = null
    }

    companion object {
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

        var instance: MainService? = null
            get() {
                return when {
                    field == null -> {
                        runOnPool {
                            GlobalApp.toastInfo("正在启动服务")
                            App.startServices()
                            Vog.i("instance ---> null")
                        }
                        null
                    }
                    field?.speechEngineLoaded == true -> field
                    else -> null
                }
            }

        val wpTimerEnd
            get() = (instance?.speechRecogService?.wakeupTimerEnd == true).also {
                Vog.d("语音唤醒定时器状态：${instance?.speechRecogService?.wakeupTimerEnd}")
            }
        val wakeupOpen
            get() =
                (instance?.speechRecogService?.wakeupI?.opened == true).also {
                    Vog.d("语音唤醒状态：$it")
                }


        val recogIsListening: Boolean
            get() {
                return if (instance?.speechEngineLoaded != true) {//未加载
                    GlobalApp.toastWarning("引擎未就绪")
                    false
                } else instance?.speechRecogService?.isListening == true
            }
        val exEngineRunning: Boolean
            get() {
                return if (instance?.speechEngineLoaded != true) {//未加载
                    GlobalApp.toastWarning("引擎未就绪")
                    false
                } else instance?.cExecutor?.running == true
            }
        /**
         * 语音合成speaking
         */
        val speaking: Boolean
            get() {
                return if (instance?.speechEngineLoaded != true) {//未加载
                    GlobalApp.toastWarning("引擎未就绪")
                    false
                } else instance?.speechSynService?.speaking == true
            }

        /**
         * 切换识别
         */
        fun switchRecog() {
            if (instance?.speechEngineLoaded != true) {//未加载
                GlobalApp.toastWarning("引擎未就绪")
                return
            }
            if (instance?.isSpeakingResWord == true && speaking) {
                Vog.d("正在响应词")
                return
            } else {
                instance?.isSpeakingResWord = false
            }
            if (recogIsListening) {
                instance?.onCommand(AppBus.ACTION_CANCEL_RECOG)
            } else
                instance?.onCommand(AppBus.ACTION_START_RECOG)
        }

        /**
         * 供插件调用
         * @param cmd String
         */
        fun parseCommand(cmd: String, chat: Boolean) {
            if (instance?.speechEngineLoaded != true) {//未加载
                GlobalApp.toastWarning("引擎未就绪")
                return
            }
            instance?.onParseCommand(cmd, false, chat)
        }
    }

    /**
     * 开启语音识别输入
     */
    fun startVoiceInput() {
        if (recogIsListening) return
        voiceMode = MODE_INPUT
        onCommand(ACTION_START_RECOG)
    }

    private fun hideAll(immediately: Boolean = false) {
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
            "播放" -> SystemBridge.mediaResume()
            "停止" -> SystemBridge.mediaStop()
            "暂停" -> SystemBridge.mediaPause()
            "上一首" -> SystemBridge.mediaPre()
            "下一首" -> SystemBridge.mediaNext()
            //打开电灯、关闭电灯、增大亮度、减小亮度
            //打开手电筒、关闭手电筒
            "打开手电筒", "打开电灯" -> SystemBridge.openFlashlight()
            "关闭手电筒", "关闭电灯" -> SystemBridge.closeFlashlight()
            else -> {//"截屏分享", "文字提取" 等命令
                runOnPool {
                    onParseCommand(w)
                }
            }
        }
        return null
    }


    //是否在播放响应词
    var isSpeakingResWord = false

    /**
     * 语音识别事件监听
     */
    inner class RecogEventListener : SpeechEvent {
        override fun onWakeup(word: String?) {
            Vog.d("onWakeup ---> 唤醒 -> $word")
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
            if (AppConfig.vibrateWhenStartReco || voiceMode != MODE_VOICE) {//询问参数时，震动
                SystemBridge.vibrate(80L)
            }
            floatyPanel.show("开始聆听")
            Vog.d("onPreStartRecog ---> 开始识别")
        }

        override fun onResult(voiceResult: String) {//解析完成再 resumeMusicIf()?
            Vog.d("结果 --------> $voiceResult")

            when (voiceMode) {
                MODE_VOICE -> {//剪去结束词
                    AppConfig.finishWord.also {
                        when {
                            it?.isEmpty() != false -> onParseCommand(voiceResult)
                            voiceResult.endsWith(it) -> onParseCommand(voiceResult.substring(0, voiceResult.length - it.length))
                            else -> onParseCommand(voiceResult)
                        }
                    }
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
                            floatyPanel.show("重新识别")
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
            floatyPanel.show(temp)
            listeningAni.show(temp)
            AppConfig.finishWord.also {
                if (it != null && it != "") {
                    if (temp.endsWith(it)) {
                        onCommand(ACTION_STOP_RECOG)
                    }
                }
            }
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
            floatyPanel.showAndHideDelay(SpeechEvent.codeString(errCode))
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
                    if (errCode != SpeechEvent.CODE_NET_ERROR)
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

    /**
     * 解析指令
     * @param result String
     * @param from Boolean
     */
    fun onParseCommand(
            result: String, needCloud: Boolean = true,
            chat: Boolean = AppConfig.openChatSystem, from: Int = 0): Boolean {
        Vog.d("解析命令：$result")
        isContinousDialogue = false
        floatyPanel.show(result)
        parseAnimation.begin()
        floatyPanel.showParseAni()

        resumeMusicIf()

        runOnCachePool {
            val parseResult = ParseEngine
                    .parseAction(result, AccessibilityApi.accessibilityService?.currentScope, smartOpen, onClick)
            if (parseResult.isSuccess) {
                if (parseResult.actionQueue?.isNotEmpty() == true) {
                    floatyPanel.hideDelay()//执行时 消失
                    cExecutor.execQueue(result, parseResult.actionQueue)

                    val his = CommandHistory(UserInfo.getUserId(), result,
                            parseResult.msg)
                    WrapperNetHelper.uploadUserCommandHistory(his)
                } else {
                    hideAll()
                }
            } else {// statistics
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
        }
        return true
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
            if (cmdWord != "") {//使用smartOpen
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
    private fun doChat(result: String) {
        parseAnimation.begin()
        floatyPanel.showParseAni()
        resumeMusicLock = true
        val data = chatSystem.chatWithText(result)
        if (data == null) {
            floatyPanel.showAndHideDelay("获取失败,详情查看日志")
            parseAnimation.failedAndHideDelay()
            resumeMusicIf()
            //检查长语音
            speechRecogService?.startIfLastingVoice()
        } else {
            data.resultUrls.also {
                when {
                    it.isEmpty() -> return@also
                    it.size == 1 -> SystemBridge.openUrl(it[0].url)
                    else -> startActivity(Intent(context, ResultPickerActivity::class.java)
                            .also { intent ->
                                intent.putExtra("title", data.word)
                                intent.putExtra("data", BundleBuilder().put("items", it).data)
                            })
                }
            }
            data.word.let { word ->
                AppBus.post(CommandHistory(UserInfo.getUserId(), result, word))
                speak(word)
                floatyPanel.show(if (word.contains("="))
                    data.word.replace("=", "\n=") else word)
                executeAnimation.begin()
                executeAnimation.show(word)

            }
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
    inner class SynthesisEventListener : SyncEvent {

        override fun onError(text: String?) {
            notifySpeakFinish(text, true)
            resumeMusicIf()
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
            floatyPanel.show(text)
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

}
