package cn.vove7.jarvis.services

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import cn.vove7.androlua.luautils.LuaContext
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.ORDER_BEGIN_SCREEN_PICKER
import cn.vove7.common.appbus.AppBus.EVENT_FORCE_OFFLINE
import cn.vove7.common.appbus.AppBus.EVENT_START_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.EVENT_STOP_DEBUG_SERVER
import cn.vove7.common.appbus.AppBus.ORDER_BEGIN_SCREEN_PICKER_TRANSLATE
import cn.vove7.common.appbus.AppBus.ORDER_CANCEL_RECO
import cn.vove7.common.appbus.AppBus.ORDER_START_RECO
import cn.vove7.common.appbus.AppBus.ORDER_START_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.AppBus.ORDER_STOP_EXEC
import cn.vove7.common.appbus.AppBus.ORDER_STOP_RECO
import cn.vove7.common.appbus.AppBus.ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.appbus.VoiceData
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.bridges.ShowDialogEvent
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.interfaces.SpeakCallback
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.utils.RegUtils.checkCancel
import cn.vove7.common.utils.RegUtils.checkConfirm
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.startActivityOnNewTask
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.exector.MultiExecutorEngine
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.jarvis.App
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PermissionManagerActivity
import cn.vove7.jarvis.activities.ScreenPickerActivity
import cn.vove7.jarvis.chat.ChatSystem
import cn.vove7.jarvis.chat.QykChatSystem
import cn.vove7.jarvis.chat.TulingChatSystem
import cn.vove7.jarvis.speech.SpeechEvent
import cn.vove7.jarvis.speech.SpeechRecoService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.AudioController
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.dialog.MultiChoiceDialog
import cn.vove7.jarvis.view.dialog.OnMultiSelectListener
import cn.vove7.jarvis.view.dialog.OnSelectListener
import cn.vove7.jarvis.view.dialog.SingleChoiceDialog
import cn.vove7.jarvis.view.floatwindows.ListeningToast
import cn.vove7.jarvis.view.statusbar.ExecuteAnimation
import cn.vove7.jarvis.view.statusbar.ListeningAnimation
import cn.vove7.jarvis.view.statusbar.ParseAnimation
import cn.vove7.vtp.dialog.DialogUtil
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.SubscriberExceptionEvent
import org.greenrobot.eventbus.ThreadMode
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.concurrent.thread

/**
 * 主服务
 */
class MainService : BusService(),
        ServiceBridge, OnSelectListener, OnMultiSelectListener, LuaContext {

    private val listeningToast: ListeningToast by lazy {
        ListeningToast()
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
    var messengerAction: Action? = null
    /**
     * 执行器
     */
    private lateinit var cExecutor: CExecutorI

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
        runOnNewHandlerThread("load_speech_engine") {
            init()
        }
    }

    var speechEngineLoaded = false
    fun init() {
        loadChatSystem()
        loadSpeechService()
        cExecutor = MultiExecutorEngine()
        speechEngineLoaded = true
        GlobalApp.toastShort("启动完成")
    }

    private fun loadSpeechService() {
        speechRecoService = BaiduSpeechRecoService(RecgEventListener())
        speechSynService = SpeechSynService(SyncEventListener())
    }

    fun loadChatSystem(byUserSet: Boolean = false) {
        val type = GlobalApp.APP.resources.getStringArray(R.array.list_chat_system)

        if (!AppConfig.openChatSystem)
            return
        chatSystem = when (AppConfig.chatSystem) {
            type[0] -> QykChatSystem()
            type[1] -> TulingChatSystem()
            else -> QykChatSystem()
        }
        if (byUserSet) {
            GlobalApp.toastShort("对话系统切换完成")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {}
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSubException(e: SubscriberExceptionEvent) {
        Vog.d(this, "onSubException ---> $e")
//        e.throwable?.printStackTrace()
        GlobalLog.err(e.throwable)
    }

    /**
     * 继续执行确认框
     */
    private var alertDialog: AlertDialog? = null

    override fun showAlert(title: String?, msg: String?) {
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
                DialogUtil.setFloat(alertDialog!!)
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
        if (AppConfig.voiceControlDialog) {
            speechRecoService?.cancelRecog()
        }
        voiceMode = MODE_VOICE
        cExecutor.notifyAlertResult(r)
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
        Vog.d(this, "单选回调 $data")
        hideDialog()
        cExecutor.onSingleChoiceResult(pos, data)
    }


    /**
     * 多选回调
     */
    override fun onMultiSelect(data: List<ChoiceData>?, msg: String) {
        messengerAction?.responseResult = data != null
        Vog.d(this, "多选回调 $data")
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
        //关闭语音播报 toast
        if (AppConfig.audioSpeak && AppConfig.currentStreamVolume != 0) {
            speakSync = false
            speechSynService?.speak(text)
        } else {
            GlobalApp.toastShort(text ?: "null")
        }
    }

    private val speakCallbacks = mutableListOf<SpeakCallback>()

    /**
     * 同步
     * @param text String?
     * @param call SpeakCallback
     */
    private fun speakWithCallback(text: String?, call: SpeakCallback) {
        speakCallbacks.add(call)
        speakSync = true
        speechSynService?.speak(text) ?: notifySpeakFinish()
    }

    override fun speakSync(text: String?): Boolean {
        speakSync = true
        return if (AppConfig.audioSpeak && AppConfig.currentStreamVolume != 0) {//当前音量非静音
            speechSynService?.speak(text) ?: notifySpeakFinish()
            true
        } else {
            GlobalApp.toastShort(text ?: "null")
            false
        }
    }

    override fun onExecuteStart(tag: String) {//
        Vog.d(this, "开始执行 -> $tag")
//        listeningToast.showAndHideDelay("开始执行")
        executeAnimation.begin()
        executeAnimation.show(tag)
    }

    /**
     * 执行结果回调
     * from executor 线程
     */
    override fun onExecuteFinished(result: Boolean) {//
        Vog.d(this, "onExecuteFinished  --> $result")
        listeningToast.hideImmediately()
        if (AppConfig.execSuccessFeedback) {
            if (result) executeAnimation.success()
            else executeAnimation.failedAndHideDelay()
        } else executeAnimation.hideDelay()
    }

    //from executor 线程
    override fun onExecuteFailed(errMsg: String?) {//错误信息
        Vog.e(this, "onExecuteFailed: $errMsg")
        executeAnimation.failedAndHideDelay()
        if (AppConfig.execFailedVoiceFeedback)
            speakSync("执行失败")
        else GlobalApp.toastShort("执行失败")
        listeningToast.hideImmediately()
    }

    override fun onExecuteInterrupt(errMsg: String) {
        Vog.e(this, "onExecuteInterrupt: $errMsg")
        executeAnimation.failedAndHideDelay()
//        GlobalApp.toastShort("")
        executeAnimation.failedAndHideDelay()
    }


    /**
     * onSpeechAction
     * 无需立即执行，可延缓使用AppBus
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSpeechAction(sAction: SpeechAction) {
        if (!speechEngineLoaded) {
            GlobalApp.toastShort("引擎未就绪")
            return
        }
        when (sAction.action) {
            SpeechAction.ActionCode.ACTION_START_RECO -> {
                speechSynService?.stopIfSpeaking()
                speechRecoService?.startRecog()
            }
            SpeechAction.ActionCode.ACTION_STOP_RECO -> speechRecoService?.stopRecog()
            SpeechAction.ActionCode.ACTION_CANCEL_RECO -> {
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
                Vog.e(this, sAction)
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
        Vog.d(this, "onCommand ---> $order")
        thread(priority = Thread.MAX_PRIORITY) {
            when (order) {
                ORDER_STOP_EXEC -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastShort("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.cancelRecog()
                    speechSynService?.stop()
                    cExecutor.interrupt()
                    hideAll()
                }
                ORDER_STOP_RECO -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastShort("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.stopRecog()
                }
                ORDER_CANCEL_RECO -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastShort("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.cancelRecog()
                }
                ORDER_START_RECO -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastShort("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.startRecog()
                }
                EVENT_FORCE_OFFLINE -> {
                    AppConfig.logout()
                }
                ORDER_START_VOICE_WAKEUP_WITHOUT_NOTIFY -> {//不重新计时
                    if (!speechEngineLoaded) {
                        GlobalApp.toastShort("引擎未就绪")
                        return@thread
                    }
                    speechRecoService?.startWakeUpSilently(false)
                }
                ORDER_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY -> {
                    if (!speechEngineLoaded) {
                        GlobalApp.toastShort("引擎未就绪")
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
         * 语音事件数据类型
         */

        private val data = HashMap<String, Any>()
        var instance: MainService? = null
            get() {
                return when {
                    field == null -> {
                        runOnPool {
                            GlobalApp.toastShort("正在启动服务")
                            App.startServices()
                            Vog.i(this, "instance ---> null")
                        }
                        null
                    }
                    field?.speechEngineLoaded == true -> field
                    else -> null
                }
            }

        val recoIsListening: Boolean
            get() {
                return if (instance?.speechEngineLoaded != true) {//未加载
                    GlobalApp.toastShort("引擎未就绪")
                    false
                } else instance?.speechRecoService?.isListening == true
            }
        val exEngineRunning: Boolean
            get() {
                return if (instance?.speechEngineLoaded != true) {//未加载
                    GlobalApp.toastShort("引擎未就绪")
                    false
                } else instance?.cExecutor?.running == true
            }
        /**
         * 语音合成speaking
         */
        val speaking: Boolean
            get() {
                return if (instance?.speechEngineLoaded != true) {//未加载
                    GlobalApp.toastShort("引擎未就绪")
                    false
                } else instance?.speechSynService?.speaking == true
            }

        /**
         * 切换识别
         */
        fun switchReco() {
            if (instance?.speechEngineLoaded != true) {//未加载
                GlobalApp.toastShort("引擎未就绪")
                return
            }
            if (recoIsListening) {//配置
                instance?.onCommand(AppBus.ORDER_CANCEL_RECO)
            } else
                instance?.onCommand(AppBus.ORDER_START_RECO)
        }

        /**
         * 供插件调用
         * @param cmd String
         */
        fun parseCommand(cmd: String, chat: Boolean) {
            if (instance?.speechEngineLoaded != true) {//未加载
                GlobalApp.toastShort("引擎未就绪")
                return
            }
            instance?.onParseCommand(cmd, false, chat)
        }
    }

    private fun hideAll(immediately: Boolean = false) {
        if (immediately) {
            listeningToast.hideImmediately()
            listeningAni.hideDelay(0)
        } else {
            listeningToast.hideDelay()
            listeningAni.hideDelay()
        }
    }

    override fun getGlobalData(): Map<*, *> {
        return data
    }

    override fun get(name: String): Any? {
        return data[name]
    }

    override fun getContext(): Context = this

    override fun call(name: String, args: Array<Any>) {}

    override fun set(name: String, `object`: Any) {
        data[name] = `object`
    }

    override fun getWidth(): Int {
        return resources.displayMetrics.widthPixels
    }

    override fun getHeight(): Int {
        return resources.displayMetrics.heightPixels
    }

    /**
     * 解析唤醒词
     * @param w String
     * @return Boolean 是否继续识别
     */
    fun parseWakeUpCommand(w: String): Boolean {
        if (recoIsListening) {
            Vog.d(this, "parseWakeUpCommand ---> 正在识别")
            return true
        }
        when (w) {
            "你好小V", "你好小v", "小V同学", "小v同学" -> { //唤醒词
                return false
            }
            in AppConfig.userWakeupWord.split('#') -> { //用户唤醒词
                Vog.d(this, "parseWakeUpCommand ---> 用户唤醒词")
                return false
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
        return true
    }

    fun playSoundEffect(rawId: Int) {//音效异步
        if (AppConfig.voiceRecogFeedback && AppConfig.currentStreamVolume != 0) {
            SystemBridge.getMusicFocus()
            AudioController.playOnce(rawId)
        }
    }

    /**
     *
     * @param rawId Int
     * @param lock CountDownLatch?
     */
    fun playSoundEffectSync(rawId: Int, lock: CountDownLatch? = null) {//音效同步
        Vog.d(this, "playSoundEffectSync ---> 音效开始")
        if (AppConfig.voiceRecogFeedback && AppConfig.currentStreamVolume != 0) {
            SystemBridge.getMusicFocus()
            val l = lock ?: CountDownLatch(1)
            runOnPool {
                AudioController.playOnce(rawId) {
                    l.countDown()
                }
            }
            if (lock == null) l.await(2L, TimeUnit.SECONDS)
        } else lock?.countDown()
        Vog.d(this, "playSoundEffectSync ---> 音效结束")
    }

    /**
     * 语音识别事件监听
     */
    inner class RecgEventListener : SpeechEvent {
        override fun onWakeup(word: String?) {
            //解析成功  不再唤醒
            parseWakeUpCommand(word ?: "").also {
                if (it) return
            }
            //唤醒词 你好小V，小V同学 ↓
            speechRecoService?.cancelRecog(false)
            speechRecoService?.startRecog(true)
            return
        }

        private fun speakResponseWord(lock: CountDownLatch? = null) {
            continuePlay = false//不继续播放后台，
            Vog.d(this, "speakResponseWord 响应词 ---> ${AppConfig.responseWord}")
            val l = lock ?: CountDownLatch(1)
            speakWithCallback(AppConfig.responseWord, object : SpeakCallback {
                override fun speakCallback(result: String?) {
                    Vog.d(this, "speakWithCallback ---> $result")
                    sleep(200)
                    l.countDown()
                }
            })
            if (lock == null) l.await()
            Vog.d(this, "speakResponseWord ---> speak finish")
        }

        //响应词 与 提示音
        //语音唤醒时已播放  就不再播放

        /**
         *
         * @param voiceWakeup Boolean true唤醒 false聆听
         * @param sayWord Boolean 播放响应词
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
        }

        override fun onStartRecog(byVoice: Boolean) {
            speechSynService?.stopIfSpeaking()
            AppBus.post(AppBus.EVENT_BEGIN_RECO)
            Vog.d(this, "onStartRecog ---> 开始识别")
            if (continuePlay)//唤醒时检查过
                checkMusic()//检查后台播放
            listeningAni.begin()//
            recogEffect(byVoice)
            listeningToast.show("开始聆听")
            //震动
            if (AppConfig.vibrateWhenStartReco || voiceMode != MODE_VOICE) {//询问参数时，震动
                SystemBridge.vibrate(80L)
            }

        }

        override fun onResult(voiceResult: String) {//解析完成再 resumeMusicIf()?
            Vog.d(this, "结果 --------> $voiceResult")
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
                        else -> onCommand(ORDER_START_RECO)  //继续????
                    }
                }
            }
        }

        override fun onTempResult(temp: String) {
            listeningToast.show(temp)
            listeningAni.show(temp)
            AppConfig.finishWord.also {
                if (it != null && it != "") {
                    if (temp.endsWith(it)) {
                        onCommand(ORDER_STOP_RECO)
                    }
                }
            }
        }

        override fun onStopRecog() {
            Vog.d(this, "onStopRecog ---> ")
            resumeMusicIf()
//            listeningToast.hideImmediately()
            parseAnimation.begin()
        }

        override fun onCancelRecog() {
            Vog.d(this, "onCancelRecog ---> ")
            continuePlay = true
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
            }
        }

        override fun onRecogFailed(err: String) {
            resumeMusicIf()
            AppBus.post(AppBus.EVENT_ERROR_RECO)
            listeningToast.showAndHideDelay(err)
            when (voiceMode) {
                MODE_VOICE -> {
                    hideAll()
                    playSoundEffect(R.raw.recog_failed)
                }
                MODE_GET_PARAM -> {//获取参数失败
                    cExecutor.onGetVoiceParam(null)
                    voiceMode = MODE_VOICE
                    executeAnimation.begin()//continue
                }
                MODE_ALERT -> {//fixme 网络错误，无限...
                    onCommand(ORDER_START_RECO)  //继续????
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
     */
    fun onParseCommand(result: String, needCloud: Boolean = true, chat: Boolean = AppConfig.openChatSystem): Boolean {
        isContinousDialogue = false
        listeningToast.show(result)
        parseAnimation.begin()
        resumeMusicIf()
//        if (UserInfo.isVip() && AppConfig.onlyCloudServiceParse) {//高级用户且仅云解析
//            Vog.d(this, "onParseCommand ---> only云解析")
//            NetHelper.cloudParse(result) {
//                runFromCloud(result, it)
//            }
//            return
//        }
        val parseResult = ParseEngine
                .parseAction(result, AccessibilityApi.accessibilityService?.currentScope)
        if (parseResult.isSuccess) {
            listeningToast.hideImmediately()//执行时 消失
            val his = CommandHistory(UserInfo.getUserId(), result,
                    parseResult.msg)
            NetHelper.uploadUserCommandHistory(his)
            cExecutor.execQueue(result, parseResult.actionQueue)
            return true
        } else {// statistics
            //云解析
            return if (needCloud && AppConfig.cloudServiceParseIfLocalFailed) {
                Vog.d(this, "onParseCommand ---> 失败云解析")
                NetHelper.cloudParse(result) {
                    runFromCloud(result, it)
                }
                true
            } else if (chat) {//聊天
                parseAnimation.begin()
                listeningToast.showParseAni()
                runOnCachePool {
                    val data = chatSystem.chatWithText(result)
                    if (data == null) {
                        listeningToast.showAndHideDelay("获取失败")
                        parseAnimation.failedAndHideDelay()
                    } else {
                        listeningToast.show(if (data.contains("="))
                            data.replace("=", "\n=") else data)
                        executeAnimation.begin()
                        speakWithCallback(data, object : SpeakCallback {
                            override fun speakCallback(result: String?) {
                                hideAll()
                                if (!userInterrupted && AppConfig.continuousDialogue) {//连续对话
                                    isContinousDialogue = true
                                    AppBus.postDelay(ORDER_START_RECO, ORDER_START_RECO, 750)
                                }
                            }
                        })
                    }
                }
                true
            } else {
                NetHelper.uploadUserCommandHistory(CommandHistory(UserInfo.getUserId(), result, null))
                listeningToast.showAndHideDelay("解析失败")
                parseAnimation.failedAndHideDelay()
                false
            }
        }
    }

    private fun runFromCloud(command: String, actions: List<Action>?): Boolean {
        if (actions == null || actions.isEmpty()) {
            listeningToast.showAndHideDelay("解析失败")
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

    var userInterrupted = false
        get() {
            val b = field
            field = false
            return b
        }

    /**
     * 语音合成事件监听
     */
    inner class SyncEventListener : SyncEvent {

        override fun onError(err: String, requestText: String?) {
            GlobalApp.toastShort(requestText ?: "")
            GlobalLog.err(err)
            notifySpeakFinish()
            resumeMusicIf()
        }

        override fun onFinish() {
            Vog.d(this, "onSynData 结束")
            notifySpeakFinish()
            if (continuePlay) {//
                resumeMusicIf()
            }
        }

        override fun onUserInterrupt() {
            userInterrupted = true
        }

        override fun onStart() {
            Vog.d(this, "onSynData 开始")
            if (continuePlay)//不再检查 播放响应词
                checkMusic()
        }
    }

    fun notifySpeakFinish() {
        Vog.d(this, "notifySpeakFinish ---> $speakSync")
        if (speakSync) {
            cExecutor.speakCallback()
            speakCallbacks.forEach {
                it.speakCallback()
            }
            speakCallbacks.clear()
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
        SystemBridge.getMusicFocus()

//        if (!isContinousDialogue && continuePlay && SystemBridge.isMediaPlaying()
//                && !speechSynService.speaking) {
//            SystemBridge.getMusicFocus()
//            GlobalLog.log("checkMusic ---> 有音乐播放")
////            Vog.d(this, "checkMusic ---> 有音乐播放")
//            haveMusicPlay = true
//        } else {
//            haveMusicPlay = false
//            GlobalLog.log("checkMusic ---> 无音乐播放")
////            Vog.d(this, "checkMusic ---> 无音乐播放")
//        }
    }

    //识别前是否有音乐播放
    private var haveMusicPlay = false
    var continuePlay = true//是否继续播放| 在说完响应词后，不改变haveMusicPlay
//

    fun resumeMusicIf() {
        SystemBridge.removeMusicFocus()
//        if() return
//        Vog.d(this, "音乐继续 ---> HAVE: $haveMusicPlay CONTINUE: $continuePlay")
//        synchronized(haveMusicPlay) {
//            if (!isContinousDialogue && haveMusicPlay) {
//                if (continuePlay) {//   speak响应词
//                    SystemBridge.mediaResume()
//                    haveMusicPlay = false
//                } else continuePlay = true
//            } else {
//                continuePlay = true
//            }
//        }
    }

}
