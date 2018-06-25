package cn.vove7.accessibilityservicedemo.services

import android.app.Dialog
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Message
import cn.vove7.accessibilityservicedemo.PermissionManagerActivity
import cn.vove7.accessibilityservicedemo.dialog.MultiChoiceDialog
import cn.vove7.accessibilityservicedemo.dialog.OnMultiSelectListener
import cn.vove7.accessibilityservicedemo.dialog.OnSelectListener
import cn.vove7.accessibilityservicedemo.dialog.SingleChoiceDialog
import cn.vove7.appbus.Bus
import cn.vove7.appbus.SpeechAction
import cn.vove7.appbus.VoiceData
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.ActionExecutor
import cn.vove7.executorengine.GetAccessibilityBridge
import cn.vove7.executorengine.OnExecutorResult
import cn.vove7.executorengine.bridge.AccessibilityBridge
import cn.vove7.executorengine.bridge.ChoiceData
import cn.vove7.executorengine.bridge.ServiceBridge
import cn.vove7.executorengine.bridge.ShowDialogEvent
import cn.vove7.executorengine.model.RequestPermission
import cn.vove7.parseengine.engine.ParseEngine
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
        Bus.reg(this)
        actionExecutor = ActionExecutor(
                this, this,
                cn.vove7.executorengine.bridge.SystemBridge(this),
                this,
                this
        )
        toast = Voast.with(this, true).top()
    }

    var currentDialog: Dialog? = null
    fun hideDalog() {
        if (currentDialog?.isShowing == true) {
            currentDialog?.dismiss()
            currentDialog = null
        }
    }

    /**
     * 选择对话框
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun showChoiceDialog(event: ShowDialogEvent) {
        messengerAction = event.action
        currentDialog = when (event.whichDialog) {
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
        hideDalog()
        actionExecutor.notifySync()
    }


    /**
     * 多选回调
     */

    override fun onMultiSelect(data: List<ChoiceData>?, msg: String) {
        messengerAction?.responseResult = data != null
        Vog.d(this, "多选回调 $data")
//        messengerAction?.responseBundle?.putSerializable("data", data)
        hideDalog()
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
        Bus.postSpeechAction(SpeechAction(SpeechAction.ACTION_START))
    }

    /**
     * 语音事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVoiceData(msg: Message?) {
        when (msg?.what) {
            WHAT_VOICE_TEMP -> {
                val res = msg.data.getString("msg")
                Bus.postVoiceData(VoiceData(msg.what, res))
            }
            WHAT_VOICE_ERR -> {
                val res = msg.data.getString("msg")
                when (mode) {
                    MODE_VOICE -> {
                        Bus.postVoiceData(VoiceData(msg.what, res))
                    }
                    MODE_GET_PARAM -> {
                        toast.showShort("获取参数失败")
                        actionExecutor.onGetVoiceParam(null)
                        mode = MODE_VOICE
                    }
                }
            }
            WHAT_VOICE_VOL -> {
                Bus.postVoiceData(msg.data.getSerializable("data") as VoiceData)
            }
            WHAT_VOICE_RESULT -> {
                val res = msg.data.getString("msg")
                Vog.d(this, " --------> $res")
                when (mode) {
                    MODE_VOICE -> {
                        Bus.postVoiceData(VoiceData(WHAT_VOICE_TEMP, res))
                        toast.showShort("开始解析")
                        val parseResult = ParseEngine.parseAction(res)
                        if (parseResult.isSuccess) {
                            toast.showShort("解析成功")
                            actionExecutor.exec(parseResult.actionQueue)
                        } else {
                            toast.showShort("解析失败")
                        }
                    }
                    MODE_GET_PARAM -> {//中途参数
                        if (res == "") {//失败
                            //询问重新
//                            return
                            messengerAction?.responseResult = false
                            actionExecutor.onGetVoiceParam(null)
                        } else {//通知
                            actionExecutor.onGetVoiceParam(res)
                        }
                        mode = MODE_VOICE
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
        Bus.unreg(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return object : Binder() {

        }
    }

    override fun getBridge(): AccessibilityBridge? {
        return MyAccessibilityService.accessibilityService
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun runAction(que: PriorityQueue<Action>) {
        actionExecutor.exec(que)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun stopExecutor(order: String) {
        when (order) {
            "stop exec" -> {
                Bus.postSpeechAction(SpeechAction(SpeechAction.ACTION_STOP))
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
         * 语音事件数据类型
         */
        const val WHAT_VOICE_TEMP = 1 //临时结果
        const val WHAT_VOICE_VOL = 2 //音量数据
        const val WHAT_VOICE_ERR = 4 //出错
        const val WHAT_VOICE_RESULT = 3 //识别结果
    }
}


