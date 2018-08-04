package cn.vove7.jarvis.services

import android.app.AlertDialog
import android.app.Dialog
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.SpeechAction
import cn.vove7.appbus.VoiceData
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.ActionScriptExecutor
import cn.vove7.executorengine.AbsExecutorImpl
import cn.vove7.executorengine.GetAccessibilityBridge
import cn.vove7.executorengine.OnExecutorResult
import cn.vove7.executorengine.bridges.*
import cn.vove7.executorengine.model.RequestPermission
import cn.vove7.jarvis.PermissionManagerActivity
import cn.vove7.jarvis.utils.Utils.checkCancel
import cn.vove7.jarvis.utils.Utils.checkConfirm
import cn.vove7.jarvis.view.dialog.MultiChoiceDialog
import cn.vove7.jarvis.view.dialog.OnMultiSelectListener
import cn.vove7.jarvis.view.dialog.OnSelectListener
import cn.vove7.jarvis.view.dialog.SingleChoiceDialog
import cn.vove7.jarvis.view.floatwindows.VoiceFloat
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
    private lateinit var floatVoice: VoiceFloat

    /**
     * 信使
     */
    var messengerAction: Action? = null
    /**
     * 执行器
     */
    private lateinit var actionScriptExecutor: AbsExecutorImpl

    /**
     * 当前语音使用方式
     */
    private var voiceMode = MODE_VOICE

    override fun onCreate() {
        super.onCreate()
        AppBus.reg(this)
        actionScriptExecutor = ActionScriptExecutor(
                this, this,
                cn.vove7.executorengine.bridges.SystemBridge(this),
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
            voiceMode = MODE_ALERT
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
        voiceMode = MODE_VOICE
        actionScriptExecutor.notifySync()
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
        actionScriptExecutor.notifySync()
    }


    /**
     * 多选回调
     */
    override fun onMultiSelect(data: List<ChoiceData>?, msg: String) {
        messengerAction?.responseResult = data != null
        Vog.d(this, "多选回调 $data")
//        messengerAction?.responseBundle?.putSerializable("data", data)
        hideDialog()
        actionScriptExecutor.notifySync()
    }

    /**
     * 中途获取未知参数
     * @param action 执行动作
     */
    override fun getVoiceParam(action: Action) {
        toast.showShort(action.param?.askText ?: "临时参数")
        messengerAction = action
        voiceMode = MODE_GET_PARAM
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
                when (voiceMode) {
                    MODE_VOICE -> {
                        AppBus.postVoiceData(VoiceData(msg.what, res))
                    }
                    MODE_GET_PARAM -> {
                        toast.showShort("获取参数失败")
                        actionScriptExecutor.onGetVoiceParam(null)
                        voiceMode = MODE_VOICE
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
                when (voiceMode) {
                    MODE_VOICE -> {
                        AppBus.postVoiceData(VoiceData(WHAT_VOICE_TEMP, voiceData))
                        toast.showShort("开始解析")
                        val parseResult = ParseEngine
                                .parseGlobalAction(voiceData, getBridge()?.currentScope?.packageName
                                        ?: "")
                        if (parseResult.isSuccess) {
                            toast.showShort("解析成功")
                            actionScriptExecutor.exec(parseResult.actionQueue)
                        } else {
                            toast.showShort("解析失败")
                        }
                    }
                    MODE_GET_PARAM -> {//中途参数
                        if (voiceData == "") {//失败
                            //询问重新
//                            return
                            messengerAction?.responseResult = false
                            actionScriptExecutor.onGetVoiceParam(null)
                        } else {//通知
                            actionScriptExecutor.onGetVoiceParam(voiceData)
                        }
                        voiceMode = MODE_VOICE
                    }
                    MODE_ALERT -> {
                        when {
                            checkConfirm(voiceData) -> {
                                alertDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.performClick()
                                voiceMode = MODE_VOICE
                            }
                            checkCancel(voiceData) -> {
                                alertDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.performClick()
                                voiceMode = MODE_VOICE
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
        toast.showShort(result)
    }

    override fun onExecutorFailed(errMsg: String) {
        Vog.d(this, errMsg)
        toast.showShort(errMsg)
    }

    override fun onDestroy() {
        AppBus.unreg(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {

        }
    }

    override fun getBridge(): AccessibilityApi? {
        return MyAccessibilityService.accessibilityService
    }

    /**
     * 测试文本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runAction(que: PriorityQueue<Action>) {
        actionScriptExecutor.exec(que)
    }

    /**
     * 测试脚本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runScript(ac: Action) {
        val q = PriorityQueue<Action>()
        q.add(ac)
        actionScriptExecutor.exec(q)
    }

    override fun toast(msg: String, showMillis: Int) {
        toast.show(msg,showMillis)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun stopExecutor(order: String) {
        when (order) {
            "stop exec" -> {
                AppBus.postSpeechAction(SpeechAction(SpeechAction.ACTION_STOP))
                actionScriptExecutor.stop()
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


