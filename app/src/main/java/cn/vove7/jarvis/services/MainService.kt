package cn.vove7.jarvis.services

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.Toast
import cn.vove7.androlua.luautils.LuaContext
import cn.vove7.appbus.*
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.bridges.ShowAlertEvent
import cn.vove7.common.bridges.ShowDialogEvent
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.executor.OnExecutorResult
import cn.vove7.common.model.RequestPermission
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.luaexector.LuaExecutor
import cn.vove7.jarvis.activityies.PermissionManagerActivity
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
class MainService : BusService(), OnExecutorResult,
        ServiceBridge, OnSelectListener, OnMultiSelectListener, LuaContext {
    private lateinit var toast: Voast

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
    private lateinit var cExecutor: CExecutorI

    /**
     * 当前语音使用方式
     */
    private var voiceMode = MODE_VOICE

    override fun onCreate() {
        super.onCreate()
        cExecutor = LuaExecutor(
                this,
                this,
                this
        )
        toast = Voast.with(this, true).top()
        floatVoice = VoiceFloat(this)
        floatVoice.show()
    }

    /**
     * 继续执行确认框
     */
    private var alertDialog: AlertDialog? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun showAlert(r: ShowAlertEvent) {
        alertDialog = AlertDialog.Builder(this)
                .setTitle(r.title)
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
            AppBus.postSpeechRecoAction(BaseAction.ACTION_START)
        } catch (e: Exception) {
            onRequestPermission(RequestPermission("悬浮窗权限"))
        }
    }

    /**
     * Alert同步
     * 停止语音
     */
    private fun notifyAlertResult() {
        AppBus.postSpeechRecoAction(BaseAction.ACTION_STOP)
        voiceMode = MODE_VOICE
        cExecutor.notifySync()
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
        messengerAction?.responseBundle?.putString("errMsg", msg)
        hideDialog()
        cExecutor.notifySync()
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
     * 中途获取未知参数
     * @param action 执行动作
     */
    override fun getVoiceParam(action: Action) {
        toast.showShort(action.param?.askText ?: "???")
        messengerAction = action
        voiceMode = MODE_GET_PARAM
        AppBus.postSpeechRecoAction(BaseAction.ACTION_START)
    }

    /**
     * 语音事件
     */
    //TODO 错误处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVoiceData(msg: Message?) {
        when (msg?.what) {
            WHAT_VOICE_TEMP -> {
                val res = msg.data.getString("errMsg")
                AppBus.postVoiceData(VoiceData(msg.what, res))
            }
            WHAT_VOICE_ERR -> {
                val res = msg.data.getString("errMsg")
                when (voiceMode) {
                    MODE_VOICE -> {
                        AppBus.postVoiceData(VoiceData(msg.what, res))
                    }
                    MODE_GET_PARAM -> {
                        toast.showShort("获取参数失败")
                        cExecutor.onGetVoiceParam(null)
                        voiceMode = MODE_VOICE
                    }
                    MODE_ALERT -> {
                        toast.showShort("没有听懂")
                        AppBus.postSpeechRecoAction(BaseAction.ACTION_START)  //继续????
                    }
                }
            }
            WHAT_VOICE_VOL -> {
                AppBus.postVoiceData(msg.data.getSerializable("data") as VoiceData)
            }
            WHAT_VOICE_RESULT -> {
                val voiceData = msg.data.getString("errMsg")
                Vog.d(this, "结果 --------> $voiceData")
                AppBus.postVoiceData(VoiceData(WHAT_VOICE_RESULT, voiceData))
                when (voiceMode) {
                    MODE_VOICE -> {
                        toast.showShort("开始解析")
                        val parseResult = ParseEngine
                                .parseGlobalAction(voiceData, AccessibilityApi.accessibilityService?.currentScope?.packageName
                                    ?: "")
                        if (parseResult.isSuccess) {
                            toast.showShort("解析成功")
                            cExecutor.execQueue(voiceData, parseResult.actionQueue)
                        } else {
                            toast.showShort("解析失败")
                        }
                    }
                    MODE_GET_PARAM -> {//中途参数
                        if (voiceData == "") {//失败
                            //询问重新
//                            return
                            messengerAction?.responseResult = false
                            cExecutor.onGetVoiceParam(null)
                        } else {//通知
                            cExecutor.onGetVoiceParam(voiceData)
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
                            else -> AppBus.postSpeechRecoAction(BaseAction.ACTION_START)  //继续????
                        }
                    }
                }
            }
        }
    }

    var speakSync = false
    override fun speak(text: String) {
        speakSync = false
        AppBus.post(SpeechSynAction(BaseAction.ACTION_START, text))
    }

    override fun speakSync(text: String) {
        speakSync = true
        AppBus.post(SpeechSynAction(BaseAction.ACTION_START, text))
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onSynData(data: SpeechSynData) {
        when (data.status) {
            SpeechSynData.SYN_STATUS_PREPARE -> {
                Vog.d(this, "onSynData 准备")
            }
            SpeechSynData.SYN_STATUS_START -> {
                Vog.d(this, "onSynData 开始")
            }
            SpeechSynData.SYN_STATUS_PROCESS -> {
                Vog.d(this, "onSynData 进度")
            }
            SpeechSynData.SYN_STATUS_FINISH -> {
                Vog.d(this, "onSynData 结束")
                if (speakSync) cExecutor.speakCallback()
            }
            SpeechSynData.SYN_STATUS_ERROR -> {
                Vog.d(this, "onSynData 出错 ${data.errMsg}")
                if (speakSync) cExecutor.speakCallback(data.errMsg)
            }
        }
    }


    override fun onExecuteStart(words: String) {
        Vog.d(this, "开始执行 -> $words")
    }

    /**
     * 执行结果回调
     */
    override fun onExecuteFinished(result: String) {
        Vog.d(this, result)
//        toast.showShort(result)
    }

    override fun onExecuteFailed(errMsg: String) {
        Vog.e(this, "onExecuteFailed" + errMsg)
        toast.showShort(errMsg)
    }

    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {

        }
    }

    /**
     * 测试文本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runAction(que: PriorityQueue<Action>) {
        cExecutor.execQueue("测试runAction", que)
    }

    /**
     * 测试脚本
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runScript(ac: Action) {
        val q = PriorityQueue<Action>()
        q.add(ac)
        cExecutor.execQueue("测试runScript", q)
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun stopExecutor(order: String) {
        when (order) {
            "stop execQueue" -> {
                AppBus.postSpeechRecoAction(BaseAction.ACTION_STOP)
                cExecutor.interrupt()
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
        const val WHAT_VOICE_TEMP = 1 //临时结果
        const val WHAT_VOICE_VOL = 2 //音量数据
        const val WHAT_VOICE_ERR = 4 //出错
        const val WHAT_VOICE_RESULT = 3 //识别结果
        private val data = HashMap<String, Any>()
    }

    override fun getGlobalData(): Map<*, *> {
        return data
    }

    override fun get(name: String): Any? {
        return data[name]
    }

    override fun getContext(): Context {
        return this
    }

    override fun sendMsg(msg: String) {
        Log.i("Vove :", "sendMsg  ----> $msg")
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

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

    override fun sendError(title: String, msg: Exception) {
        Vog.d(this, "sendError $title $msg")
    }

}


