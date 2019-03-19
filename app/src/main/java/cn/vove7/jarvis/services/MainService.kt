package cn.vove7.jarvis.services

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.EVENT_FORCE_OFFLINE
import cn.vove7.common.appbus.AppBus.EVENT_START_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.EVENT_STOP_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.ORDER_BEGIN_SCREEN_PICKER
import cn.vove7.common.appbus.AppBus.ORDER_BEGIN_SCREEN_PICKER_TRANSLATE
import cn.vove7.common.appbus.AppBus.ORDER_CANCEL_RECOG
import cn.vove7.common.appbus.AppBus.ORDER_START_RECOG
import cn.vove7.common.appbus.AppBus.ORDER_START_RECOG_SILENT
import cn.vove7.common.appbus.AppBus.ORDER_START_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.AppBus.ORDER_STOP_EXEC
import cn.vove7.common.appbus.AppBus.ORDER_STOP_RECOG
import cn.vove7.common.appbus.AppBus.ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.BusService
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.appbus.VoiceData
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
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.utils.RegUtils.checkCancel
import cn.vove7.common.utils.RegUtils.checkConfirm
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.startActivityOnNewTask
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.executorengine.exector.MultiExecutorEngine
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
import cn.vove7.jarvis.speech.SpeechRecoService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.AudioController
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
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


/**
 * 主服务
 */
class MainService : BusService(),
        ServiceBridge, OnSelectListener, OnMultiSelectListener {

    private val floatyPanel: FloatyPanel by lazy {
        FloatyPanel()
    }

    /**
     * 设置语音面板依靠
     */
    var toastAlign: Int = 0
        set(v) {
//            floatyPanel.align = v
        }

    override val serviceId: Int
        get() = 126

    /**
     * 悬浮窗
     */
//    private lateinit var floatVoice: VoiceFloat

    /**
     * 信使Action
     */
//    var messengerAction: Action? = null
    /**
     * 执行器
     */
    private val cExecutor: CExecutorI by lazy { MultiExecutorEngine() }

    private lateinit var chatSystem: ChatSystem

    //启动过慢  lateinit 导致未初始化异常
    var speechRecoService: SpeechRecoService? = null
    var speechSynService: SpeechSynService? = null

    /**
     * 当前语音使用方式
     */
    private var voiceMode = MODE_VOICE

    //识别过程动画
    private val listeningAni: ListeningAnimation by lazy { ListeningAnimation() }
    private val parseAnimation: ParseAnimation by lazy { ParseAnimation() }
    private val executeAnimation: ExecuteAnimation by lazy { ExecuteAnimation() }

    override fun onCreate() {
        super.onCreate()
        instance = this
        GlobalApp.serviceBridge = this
        runOnNewHandlerThread("load_speech_engine", delay = 2000) {
            init()
        }
    }

    var speechEngineLoaded = false

    fun init() {
        loadChatSystem()
        loadSpeechService()
        speechEngineLoaded = true
        GlobalApp.toastInfo("启动完成")
    }

    /**
     * 加载语音识别/合成服务
     */
    private fun loadSpeechService() {
        speechRecoService = BaiduSpeechRecoService(RecgEventListener())
        speechSynService = SpeechSynService(SynthesisEventListener())
    }

    /**
     * 加载对话系统
     */
    fun loadChatSystem() {
//        val type = GlobalApp.APP.resources.getStringArray(R.array.list_chat_system)

        if (!AppConfig.openChatSystem)
            return
//        chatSystem = when (AppConfig.chatSystem) {
//            type[0] -> QykChatSystem()
//            type[1] -> TulingChatSystem()
//            else -> QykChatSystem()
//        }
        chatSystem = TulingChatSystem()
    }

    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {}
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
            alertDialog = AlertDialog.Builder(this)
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
                    speechRecoService?.startRecog()
                }
            } catch (e: Exception) {
                GlobalLog.err(e)
                onRequestPermission(RequestPermission("悬浮窗权限"))
                notifyAlertResult(false)
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
            if (!afterSpeakResumeListen)// check 长语音
                speechRecoService?.cancelRecog()
        }
        cExecutor.notifyAlertResult(r)
        resumeListenCommandIfLasting()
    }

    /**
     * 长语音时 speak或AlertDialog 会 [暂时]关闭识别
     * 聊天对话说完(speak)后 继续标志（可能有其他调用speak，则不继续）
     *
     * 标志更改 ：开始聊天对话|showAlert set 识别状态recogIsListening  取消识别时 set false
     *
     */
    var afterSpeakResumeListen: Boolean = false

    /**
     * 长语音下继续语音识别
     * fixme 其他调用speak 也会触发
     */
    fun resumeListenCommandIfLasting() {//开启长语音时
        Vog.d("resumeListenCommandIfLasting ---> 检查长语音 afterSpeakResumeListen:$afterSpeakResumeListen IsListening:$recogIsListening")
        if (afterSpeakResumeListen && AppConfig.lastingVoiceCommand && !recogIsListening)//防止长语音识别 继续
            AppBus.postDelay("lastingVoiceCommand",
                    AppBus.ORDER_START_RECOG_SILENT, 1200)
    }

    /**
     * speak时暂时关闭 语音识别（长语音）
     */
    fun stopRecogTemp() {
        if (AppConfig.lastingVoiceCommand) {//防止长语音识别 speak聊天对话
            Vog.d("stopRecogTemp ---> speak临时 $recogIsListening")
            afterSpeakResumeListen = recogIsListening
            speechRecoService?.doCancelRecog()
            speechRecoService?.doStopRecog()
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
//    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun showChoiceDialog(event: ShowDialogEvent) {
        runOnUi {
            choiceDialog = when (event.whichDialog) {
                ShowDialogEvent.WHICH_SINGLE -> {
                    SingleChoiceDialog(this, event.askTitle, event.choiceDataSet, this)
                }
                ShowDialogEvent.WHICH_MULTI -> {
                    MultiChoiceDialog(this, event.askTitle, event.choiceDataSet, this)
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
     * @param action 执行动作
     */
    override fun getVoiceParam() {
        voiceMode = MODE_GET_PARAM
        speechRecoService?.startRecog()
    }

    private var speakSync = false
    override fun speak(text: String?) {
        stopRecogTemp()
        //关闭语音播报 toast
        if (AppConfig.audioSpeak && AppConfig.currentStreamVolume != 0) {
            speakSync = false
            speechSynService?.speak(text)
        } else {
            GlobalApp.toastInfo(text ?: "null")
            notifySpeakFinish()
        }
    }

    private val speakCallbacks = mutableListOf<SpeakCallback>()

    /**
     * 同步 响应词
     * @param text String?
     * @param resume Boolean speak结束是否继续(设置afterSpeakResumeListen) true
     * @param call SpeakCallback
     */
    private fun speakWithCallback(text: String?, resume: Boolean = true, call: SpeakCallback) {
        if (resume) stopRecogTemp()
        else afterSpeakResumeListen = false
        speakCallbacks.add(call)
        speakSync = true
        speechSynService?.speak(text) ?: notifySpeakFinish()
    }

    override fun speakSync(text: String?): Boolean {
        stopRecogTemp()
        speakSync = true
        return if (AppConfig.audioSpeak && AppConfig.currentStreamVolume != 0) {//当前音量非静音
            speechSynService?.speak(text) ?: notifySpeakFinish()
            true
        } else {
            GlobalApp.toastInfo(text ?: "null")
            false
        }
    }

    override fun onExecuteStart(tag: String) {//
        Vog.d("开始执行 -> $tag")
//        floatyPanel.showAndHideDelay("开始执行")
        executeAnimation.begin()
        executeAnimation.show(tag)
    }

    /**
     * 执行结果回调
     * from executor 线程
     */
    override fun onExecuteFinished(result: Boolean) {//
        Vog.d("onExecuteFinished  --> $result")
        floatyPanel.hideImmediately()
        if (AppConfig.execSuccessFeedback) {
            if (result) executeAnimation.success()
            else executeAnimation.failedAndHideDelay()
        } else executeAnimation.hideDelay()
    }

    //from executor 线程
    override fun onExecuteFailed(errMsg: String?) {//错误信息
        GlobalLog.log("执行出错：$errMsg")
        executeAnimation.failedAndHideDelay(errMsg)
        if (AppConfig.execFailedVoiceFeedback)
            speakSync("执行失败")
        else GlobalApp.toastError("执行失败")
        floatyPanel.hideImmediately()
    }

    override fun onExecuteInterrupt(errMsg: String) {
        Vog.e("onExecuteInterrupt: $errMsg")
        executeAnimation.failedAndHideDelay()
//        GlobalApp.toastInfo("")
        executeAnimation.failedAndHideDelay()
    }


    /**
     * onSpeechAction
     * 无需立即执行，可延缓使用AppBus
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSpeechAction(sAction: SpeechAction) {
        if (!speechEngineLoaded) {
            GlobalApp.toastWarning("引擎未就绪")
            return
        }
        when (sAction.action) {
            SpeechAction.ActionCode.ACTION_START_RECOG -> {
                speechSynService?.stopIfSpeaking()
                speechRecoService?.startRecog()
            }
            SpeechAction.ActionCode.ACTION_STOP_RECOG -> speechRecoService?.stopRecog()
            SpeechAction.ActionCode.ACTION_CANCEL_RECOG -> {
                hideAll()
                speechRecoService?.cancelRecog()
            }
            SpeechAction.ActionCode.ACTION_START_WAKEUP -> {
                AppConfig.voiceWakeup = true
                speechRecoService?.startWakeUp()
            }
            SpeechAction.ActionCode.ACTION_START_WAKEUP_WITHOUT_SWITCH -> {
                speechRecoService?.startWakeUp()
            }
            SpeechAction.ActionCode.ACTION_RELOAD_SYN_CONF -> speechSynService?.reLoad()
            SpeechAction.ActionCode.ACTION_STOP_WAKEUP -> {
                AppConfig.voiceWakeup = false
                speechRecoService?.stopWakeUp()
            }
            SpeechAction.ActionCode.ACTION_STOP_WAKEUP_WITHOUT_SWITCH -> {
                speechRecoService?.stopWakeUp()
            }
            SpeechAction.ActionCode.ACTION_STOP_WAKEUP_TIMER -> {
                speechRecoService?.stopAutoSleepWakeup()
            }
            SpeechAction.ActionCode.ACTION_START_WAKEUP_TIMER -> {
                speechRecoService?.startAutoSleepWakeup()
            }
            else -> {
                Vog.e(sAction)
            }
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
        thread(priority = Thread.MAX_PRIORITY) {
            when (order) {
                ORDER_STOP_EXEC -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastWarning("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.cancelRecog()
                    speechSynService?.stop()
                    cExecutor.interrupt()
                    hideAll()
                }
                ORDER_STOP_RECOG -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastWarning("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.stopRecog()
                }
                ORDER_CANCEL_RECOG -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastWarning("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.cancelRecog()
                }
                ORDER_START_RECOG -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastWarning("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.startRecog()
                }
                ORDER_START_RECOG_SILENT -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastWarning("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.startRecogSilent()
                }
                EVENT_FORCE_OFFLINE -> {
                    AppConfig.logout()
                }
                ORDER_START_VOICE_WAKEUP_WITHOUT_NOTIFY -> {//不重新计时
                    if (!speechEngineLoaded) {
                        GlobalApp.toastWarning("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.startWakeUpSilently(false)
                }
                ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastWarning("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.stopWakeUpSilently()
                }
                EVENT_START_DEBUG_SERVER -> {
                    RemoteDebugServer.start()
                }
                EVENT_STOP_DEBUG_SERVER -> {
                    RemoteDebugServer.stop()
                }
                ORDER_BEGIN_SCREEN_PICKER -> {
                    startActivityOnNewTask(Intent(this, ScreenPickerActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    })
                }
                ORDER_BEGIN_SCREEN_PICKER_TRANSLATE -> {
                    startActivityOnNewTask(Intent(this, ScreenPickerActivity::class.java).also {
                        it.putExtra("t", "t")
                        it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    })
                }
                else -> {
                }
            }
        }
    }

    /**
     * 请求权限
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onRequestPermission(r: RequestPermission) {
        val intent = Intent(this, PermissionManagerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("pName", r.permissionName)
        startActivity(intent)
    }

    override fun onDestroy() {
        if (speechEngineLoaded) {
            speechRecoService?.release()
            speechSynService?.release()
        }
        GlobalApp.serviceBridge = null
        super.onDestroy()
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

        val recogIsListening: Boolean
            get() {
                return if (instance?.speechEngineLoaded != true) {//未加载
                    GlobalApp.toastWarning("引擎未就绪")
                    false
                } else instance?.speechRecoService?.isListening == true
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
            if (recogIsListening) {//配置
                instance?.onCommand(AppBus.ORDER_CANCEL_RECOG)
            } else
                instance?.onCommand(AppBus.ORDER_START_RECOG)
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
        onCommand(ORDER_START_RECOG)
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

    fun playSoundEffect(rawId: Int) {//音效异步
        if (AppConfig.voiceRecogFeedback && AppConfig.currentStreamVolume != 0) {
            AudioController.playOnce(rawId)
        }
    }

    /**
     *
     * @param rawId Int
     * @param lock CountDownLatch?
     */
    fun playSoundEffectSync(rawId: Int, lock: CountDownLatch? = null) {//音效同步
        Vog.d("playSoundEffectSync ---> 音效开始")
        if (AppConfig.voiceRecogFeedback && AppConfig.currentStreamVolume != 0) {
            SystemBridge.requestMusicFocus()
            val l = lock ?: CountDownLatch(1)
            AudioController.playOnce(rawId) {
                Vog.d("playSoundEffectSync ---> 音效结束")
                l.countDown()
            }
            if (lock == null) l.await(2L, TimeUnit.SECONDS)
        } else lock?.countDown()
    }

    /**
     * 语音识别事件监听
     */
    inner class RecgEventListener : SpeechEvent {
        override fun onWakeup(word: String?) {
            Vog.d("onWakeup ---> 唤醒 -> $word")
            //解析成功  不再唤醒
            parseWakeUpCommand(word ?: "") ?: return
            Vog.d("onWakeup ---> 开始聆听 -> $word")
            //唤醒词 你好小V，小V同学 ↓
            speechRecoService?.cancelRecog(false)
            speechRecoService?.startRecog(true)
            return
        }

        private fun speakResponseWord(lock: CountDownLatch? = null) {
            resumeMusicLock = false //不继续播放后台，
            Vog.d("speakResponseWord 响应词 ---> ${AppConfig.responseWord}")
            val l = lock ?: CountDownLatch(1)
            speakWithCallback(AppConfig.responseWord, false, object : SpeakCallback {
                override fun speakCallback(result: String?) {
                    Vog.d("speakWithCallback ---> $result")
                    sleep(200)
                    l.countDown()
                }
            })
            if (lock == null)
                if (!l.await(5L, TimeUnit.SECONDS)) {
                    speechSynService?.stopIfSpeaking()
                    Vog.d("speakResponseWord ---> 等待超时")
                }
            Vog.d("speak finish")
        }

        //响应词 与 提示音
        //语音唤醒时已播放  就不再播放
        /**
         * 识别反馈
         * @param byVoice Boolean
         */
        private fun recogEffect(byVoice: Boolean) {
            if (voiceMode != MODE_VOICE) {
                return
            }
            if (byVoice) {//语音唤醒
                val lock = CountDownLatch(2)

                if (AppConfig.openResponseWord && AppConfig.speakResponseWordOnVoiceWakeup) {
                    speakResponseWord(lock)
                } else lock.countDown()
                playSoundEffectSync(R.raw.recog_start, lock)
                lock.await()
            } else {//按键 快捷 等其他方式
                val lock = CountDownLatch(2)
                if (AppConfig.openResponseWord && !AppConfig.speakResponseWordOnVoiceWakeup) {
                    speakResponseWord(lock)
                } else lock.countDown()
                playSoundEffectSync(R.raw.recog_start, lock)
                lock.await()
            }
            Vog.d("recogEffect ---> 结束")
        }

        override fun onStartRecog(byVoice: Boolean) {
            speechSynService?.stopIfSpeaking()

            AppBus.post(AppBus.EVENT_BEGIN_RECO)
            Vog.d("onStartRecog ---> 开始识别")
            checkMusic()//检查后台播放
            listeningAni.begin()//
            recogEffect(byVoice)
            floatyPanel.show("开始聆听")
            //震动
            if (AppConfig.vibrateWhenStartReco || voiceMode != MODE_VOICE) {//询问参数时，震动
                SystemBridge.vibrate(80L)
            }

        }

        override fun onResult(voiceResult: String) {//解析完成再 resumeMusicIf()?
            Vog.d("结果 --------> $voiceResult")

            if (AppConfig.lastingVoiceCommand) {//识别结束，开启长语音定时
                speechRecoService?.restartLastingUpTimer()
            }
            when (voiceMode) {
                MODE_VOICE -> {//剪去结束词
                    AppConfig.finishWord.also {
                        //fixme 播放结束不回调
                        playSoundEffectSync(R.raw.recog_finish)
                        if (it == null || it == "") {

                            onParseCommand(voiceResult)
                        } else if (voiceResult.endsWith(it)) {
                            onParseCommand(voiceResult.substring(0, voiceResult.length - it.length))
                        } else onParseCommand(voiceResult)
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
                            onCommand(ORDER_START_RECOG)  //继续????
                        }
                    }
                }
                MODE_INPUT -> {
                    hideAll()
                    AppBus.post(VoiceRecogResult(0, voiceResult))
                    voiceMode = MODE_VOICE
                    if (AppConfig.lastingVoiceCommand)
                        onCommand(ORDER_CANCEL_RECOG)
                }

            }
        }

        var temResult: String? = null

        override fun onTempResult(temp: String) {
            temResult = temp
            if (AppConfig.lastingVoiceCommand) {//临时结果 暂时关闭长语音定时器
                speechRecoService?.stopLastingUpTimer()
            }
            Vog.d("onTempResult ---> 临时结果 $temp")
            floatyPanel.show(temp)
            listeningAni.show(temp)
            AppConfig.finishWord.also {
                if (it != null && it != "") {
                    if (temp.endsWith(it)) {
                        onCommand(ORDER_STOP_RECOG)
                    }
                }
            }
        }

        override fun onStopRecog() {
            Vog.d("onStopRecog ---> ")
            resumeMusicIf()
            //fix 百度长语音 在无结果stop，无回调
            if (AppConfig.lastingVoiceCommand &&
                    speechRecoService is BaiduSpeechRecoService && temResult == null) {
                Vog.d("onStopRecog ---> 长语音无结果")
                speechRecoService?.cancelRecog()
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
                MODE_VOICE -> playSoundEffect(R.raw.recog_cancel)//取消-音效

                MODE_INPUT -> {
                    AppBus.post(VoiceRecogResult(-1))
                    voiceMode = MODE_VOICE
                }
            }
        }


        override fun onRecogFailed(err: String) {
            AppBus.post(AppBus.EVENT_ERROR_RECO)
            floatyPanel.showAndHideDelay(err)
            when (voiceMode) {
                MODE_VOICE -> {
                    listeningAni.hideDelay()
                    playSoundEffect(R.raw.recog_failed)
                    resumeMusicIf()
                }
                MODE_GET_PARAM -> {//获取参数失败
                    cExecutor.onGetVoiceParam(null)
                    voiceMode = MODE_VOICE
                    executeAnimation.begin()//continue
                }
                MODE_ALERT -> {//fixme 网络错误，无限...
                    onCommand(ORDER_START_RECOG)  //继续????
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
        NetHelper.uploadUserCommandHistory(his)
    }

    var isContinousDialogue = false

    /**
     * 解析指令
     * @param result String
     * @param from Boolean
     */
    fun onParseCommand(
            result: String, needCloud: Boolean = true,
            chat: Boolean = AppConfig.openChatSystem, from: Int = 0): Boolean {
        isContinousDialogue = false
        floatyPanel.show(result)
        parseAnimation.begin()

        resumeMusicIf()

        runOnCachePool {
            sleep(500)
            val parseResult = ParseEngine
                    .parseAction(result, AccessibilityApi.accessibilityService?.currentScope, smartOpen, onClick)
            if (parseResult.isSuccess) {
                if (parseResult.actionQueue?.isNotEmpty() == true) {
                    floatyPanel.hideImmediately()//执行时 消失
                    cExecutor.execQueue(result, parseResult.actionQueue)
                    val his = CommandHistory(UserInfo.getUserId(), result,
                            parseResult.msg)
                    NetHelper.uploadUserCommandHistory(his)
                    runOnCachePool {
                        if (AppConfig.lastingVoiceCommand) {//开启长语音,重启定时
                            speechRecoService?.restartLastingUpTimer()
                        }
                    }
                }
            } else {// statistics
                //云解析
                if (needCloud && AppConfig.cloudServiceParseIfLocalFailed) {
                    Vog.d("onParseCommand ---> 失败云解析")
                    NetHelper.cloudParse(result) {
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
                val engine = MultiExecutorEngine()
                val result = engine.use {
                    it.command = cmdWord
                    it.smartOpen(cmdWord)
                }
                if (result) {//成功打开
                    Vog.d("parseAction ---> MultiExecutorEngine().smartOpen(cmdWord) 成功打开")
                    hideAll(true)
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
        } else {
            data.word.let { word ->
                AppBus.post(CommandHistory(UserInfo.getUserId(), result, word))
                floatyPanel.show(if (word.contains("="))
                    data.word.replace("=", "\n=") else word)
                executeAnimation.begin()
                executeAnimation.show(word)

                afterSpeakResumeListen = recogIsListening
                speakWithCallback(word, true, object : SpeakCallback {
                    override fun speakCallback(result: String?) {
                        hideAll()
                    }
                })
            }
            data.resultUrls.also {
                when {
                    it.isEmpty() -> return@also
                    it.size == 1 -> SystemBridge.openUrl(it[0].url)
                    else -> startActivity(Intent(this, ResultPickerActivity::class.java)
                            .also { intent ->
                                intent.putExtra("title", data.word)
                                intent.putExtra("data", BundleBuilder().put("items", it).data)
                            })
                }
            }
        }
    }

    /**
     * 命令解析失败
     * @param cmd String
     */
    private fun onCommandParseFailed(cmd: String) {
        NetHelper.uploadUserCommandHistory(CommandHistory(UserInfo.getUserId(), cmd, null))
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

        override fun onError(err: String, responseText: String?) {
            GlobalApp.toastInfo(responseText ?: "")
            GlobalLog.err(err)
            notifySpeakFinish()
            resumeMusicIf()
        }

        override fun onFinish() {
            Vog.v("onSynData 结束")
            notifySpeakFinish()
            if (resumeMusicLock) {//
                resumeMusicIf()
            }
        }

        override fun onUserInterrupt() {
        }

        override fun onStart() {
            Vog.d("onSynData 开始")
            checkMusic()
        }
    }

    fun notifySpeakFinish() {
        if (speakSync) {
            cExecutor.speakCallback()
            speakCallbacks.forEach {
                it.speakCallback()
            }
            speakCallbacks.clear()
        }
        resumeListenCommandIfLasting()
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
        if (!isMusicFocus) {
            SystemBridge.requestMusicFocus()
            isMusicFocus = true
        }
    }

    private var isMusicFocus = false

    var resumeMusicLock = true//在获取后音频焦点后 是否 释放（speak后）

    fun resumeMusicIf() {
        if (isMusicFocus) {
            SystemBridge.removeMusicFocus()
            isMusicFocus = false
        }
    }

}
