package cn.vove7.jarvis.services

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import cn.vove7.jarvis.PermissionManagerActivity
import cn.vove7.jarvis.utils.Utils.checkCancel
import cn.vove7.jarvis.utils.Utils.checkConfirm
import cn.vove7.jarvis.view.dialog.MultiChoiceDialog
import cn.vove7.jarvis.view.dialog.OnMultiSelectListener
import cn.vove7.jarvis.view.dialog.OnSelectListener
import cn.vove7.jarvis.view.dialog.SingleChoiceDialog
import cn.vove7.jarvis.view.floatwindows.VoiceFloat
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.SpeechAction
import cn.vove7.appbus.VoiceData
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.ActionExecutor
import cn.vove7.executorengine.GetAccessibilityBridge
import cn.vove7.executorengine.OnExecutorResult
import cn.vove7.executorengine.bridge.*
import cn.vove7.executorengine.model.RequestPermission
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.dialog.DialogUtil
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.toast.Voast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 主服务
 */
class MainService : Service(), OnExecutorResult, GetAccessibilityBridge,
        ServiceBridge, OnSelectListener, OnMultiSelectListener {
    lateinit var toast: Voast

    /**
     * 悬浮窗
     */
    lateinit var floatVoice: VoiceFloat

    /**
     * 信使
     */
    var messengerAction: Action? = null
    /**
     * 执行器
     */
    lateinit var actionExecutor: ActionExecutor

    /**
     * 当前语音使用方式
     */
    private var mode = MODE_VOICE

    override fun onCreate() {
        super.onCreate()
        AppBus.reg(this)
        actionExecutor = ActionExecutor(
                this, this,
                cn.vove7.executorengine.bridge.SystemBridge(this),
                this,
                this
        )
        toast = Voast.with(this, true).top()
        floatVoice = VoiceFloat(this, 200, 200)
        floatVoice.show()
    }


    /**
     * 继续执行确认框
     */
    private var alertDialog: AlertDialog? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun showAlert(r: ShowAlertEvent) {
        alertDialog = AlertDialog.Builder(this)
                .setTitle("确认以继续")
                .setMessage(r.msg)
                .setCancelable(false)
                .setPositiveButton("继续") { _, _ ->
                    r.action.responseResult = true
                    notifyAlertResult()
                }.setNegativeButton("取消") { _, _ ->
                    r.action.responseResult = false
                    notifyAlertResult()
                }
                .create()
        try {
            DialogUtil.setFloat(alertDialog!!)
            alertDialog?.show()
            //语音
            mode = MODE_ALERT
            AppBus.post(SpeechAction(SpeechAction.ACTION_START))
        } catch (e: Exception) {
            onRequestPermission(RequestPermission("悬浮窗权限"))
        }
    }

    /**
     * Alert同步
     * 停止语音
     */
    private fun notifyAlertResult() {
        AppBus.post(SpeechAction(SpeechAction.ACTION_STOP))
        mode = MODE_VOICE
        actionExecutor.notifySync()
    }

    /**
     * 选择框
     */
    private var choiceDialog: Dialog? = null

    private fun hideDialog() {
        if (choiceDialog?.isShowing == true) {
            choiceDialog?.dismiss()
            choiceDialog = null
        }
    }

    /**
     * 选择对话框
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun showChoiceDialog(event: ShowDialogEvent) {
        messengerAction = event.action
        choiceDialog = when (event.whichDialog) {
            ShowDialogEvent.WHICH_SINGLE -> {
                SingleChoiceDialog(this, event.askTitle, event.choiceDataSet, this)
            }
            ShowDialogEvent.WHICH_MULTI -> {
                MultiChoiceDialog(this, event.askTitle, event.choiceDataSet, this)
            }
            else -> return
        }.show()
    }

    /**
     * 单选回调
     */
    override fun onSingleSelect(pos: Int, data: ChoiceData?, msg: String) {
        Vog.d(this, "单选回调 $data")
        messengerAction?.responseResult = data != null
        messengerAction?.responseBundle?.putSerializable("data", data)
        messengerAction?.responseBundle?.putString("msg", msg)
        hideDialog()
        actionExecutor.notifySync()
    }


    /**
     * 多选回调
     */
    override fun onMultiSelect(data: List<ChoiceData>?, msg: String) {
        messengerAction?.responseResult = data != null
        Vog.d(this, "多选回调 $data")
//        messengerAction?.responseBundle?.putSerializable("data", data)
        hideDialog()
        actionExecutor.notifySync()
    }

    /**
     * 中途获取未知参数
     * @param action 执行动作
     */
    override fun getVoiceParam(action: Action) {
        toast.showShort(action.param?.askText ?: "临时参数")
        messengerAction = action
        mode = MODE_GET_PARAM
        AppBus.postSpeechAction(SpeechAction(SpeechAction.ACTION_START))
    }

    /**
     * 语音事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVoiceData(msg: Message?) {
        when (msg?.what) {
            WHAT_VOICE_TEMP -> {
                val res = msg.data.getString("msg")
                AppBus.postVoiceData(VoiceData(msg.what, res))
            }
            WHAT_VOICE_ERR -> {
                val res = msg.data.getString("msg")
                when (mode) {
                    MODE_VOICE -> {
                        AppBus.postVoiceData(VoiceData(msg.what, res))
                    }
                    MODE_GET_PARAM -> {
                        toast.showShort("获取参数失败")
                        actionExecutor.onGetVoiceParam(null)
                        mode = MODE_VOICE
                    }
                    MODE_ALERT -> {
                        toast.showShort("没有听懂")
                        AppBus.post(SpeechAction(SpeechAction.ACTION_START))  //继续????
                    }
                }
            }
            WHAT_VOICE_VOL -> {
                AppBus.postVoiceData(msg.data.getSerializable("data") as VoiceData)
            }
            WHAT_VOICE_RESULT -> {
                val voiceData = msg.data.getString("msg")
                Vog.d(this, "结果 --------> $voiceData")
                when (mode) {
                    MODE_VOICE -> {
                        AppBus.postVoiceData(VoiceData(WHAT_VOICE_TEMP, voiceData))
                        toast.showShort("开始解析")
                        val parseResult = ParseEngine
                                .parseGlobalAction(voiceData, getBridge()?.currentScope?.packageName
                                        ?: "")
                        if (parseResult.isSuccess) {
                            toast.showShort("解析成功")
                            actionExecutor.exec(parseResult.actionQueue)
                        } else {
                            toast.showShort("解析失败")
                        }
                    }
                    MODE_GET_PARAM -> {//中途参数
                        if (voiceData == "") {//失败
                            //询问重新
//                            return
                            messengerAction?.responseResult = false
                            actionExecutor.onGetVoiceParam(null)
                        } else {//通知
                            actionExecutor.onGetVoiceParam(voiceData)
                        }
                        mode = MODE_VOICE
                    }
                    MODE_ALERT -> {
                        when {
                            checkConfirm(voiceData) -> {
                                alertDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.performClick()
                                mode = MODE_VOICE
                            }
                            checkCancel(voiceData) -> {
                                alertDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.performClick()
                                mode = MODE_VOICE
                            }
                            else -> AppBus.post(SpeechAction(SpeechAction.ACTION_START))  //继续????
                        }
                    }
                }
            }
        }
    }

    /**
     * 执行结果回调
     */
    override fun onExecutorSuccess(result: String) {
        Vog.d(this, result)
        uiHandler.sendMessage(uiHandler.buildToast(result))
    }

    override fun onExecutorFailed(errMsg: String) {
        Vog.d(this, errMsg)
        uiHandler.sendMessage(uiHandler.buildToast(errMsg))
    }

    override fun onDestroy() {
        AppBus.unreg(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {

        }
    }

    override fun getBridge(): AccessibilityBridge? {
        return MyAccessibilityService.accessibilityService
    }

    /**
     * 测试文本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runAction(que: PriorityQueue<Action>) {
        actionExecutor.exec(que)
    }

    /**
     * 测试脚本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runScript(ac: Action) {
        val q = PriorityQueue<Action>()
        q.add(ac)
        actionExecutor.exec(q)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun stopExecutor(order: String) {
        when (order) {
            "stop exec" -> {
                AppBus.postSpeechAction(SpeechAction(SpeechAction.ACTION_STOP))
                actionExecutor.interrupt()
            }
            else -> {
            }
        }
    }

    /**
     * 请求权限
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onRequestPermission(r: RequestPermission) {
        val intent = Intent(this, PermissionManagerActivity::class.java)
        intent.putExtra("pName", r.permissionName)
        startActivity(intent)
    }

    @SuppressLint("HandlerLeak")
    private val uiHandler = object : Handler() {
        val WHAT_TOAST = 1
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                WHAT_TOAST -> {
                    val m = msg.data["msg"] as String?
                    toast.showShort(m ?: "null")
                }
            }
        }

        fun buildToast(msg: String): Message {
            val data = Bundle()
            data.putString("msg", msg)
            val m = Message()
            m.what = WHAT_TOAST
            m.data = data
            return m
        }
    }

    companion object {
        /**
         * 正常语音模式
         */
        const val MODE_VOICE = 858
        /**
         * 执行期间获取"参数"
         */
        const val MODE_GET_PARAM = 72
        /**
         * 确认对话框模式
         */
        const val MODE_ALERT = 27

        /**
         * 语音事件数据类型
         */
        const val WHAT_VOICE_TEMP = 1 //临时结果
        const val WHAT_VOICE_VOL = 2 //音量数据
        const val WHAT_VOICE_ERR = 4 //出错
        const val WHAT_VOICE_RESULT = 3 //识别结果
    }
}


