package cn.vove7.accessibilityservicedemo.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Message
import cn.vove7.accessibilityservicedemo.utils.Bus
import cn.vove7.accessibilityservicedemo.utils.SpeechAction
import cn.vove7.accessibilityservicedemo.utils.VoiceData
import cn.vove7.executorengine.ActionExecutor
import cn.vove7.executorengine.GetAccessibilityBridge
import cn.vove7.executorengine.OnExecutorResult
import cn.vove7.executorengine.bridge.AccessibilityBridge
import cn.vove7.executorengine.bridge.SpeechBridge
import cn.vove7.executorengine.bridge.SystemBridge
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.parseengine.model.Action
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.toast.Voast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 主服务
 */
class MainService : Service(), OnExecutorResult, GetAccessibilityBridge, SpeechBridge {
    lateinit var toast: Voast

    /**
     * 执行中间参数
     */
    var paramAction: Action? = null
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
                this,
                SystemBridge(this),
                this,
                this
        )

        toast = Voast.with(this).top()
    }

    override fun getUnsetParam(action: Action) {
        toast.showShort("获取临时参数")
        paramAction = action
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
                        Vog.d(this, "获取参数失败")
                        paramAction?.voiceOk = false
                        actionExecutor.resume()
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
                            paramAction?.voiceOk = false
                            actionExecutor.resume()
                        } else {//通知
                            paramAction?.param?.value = res
                            actionExecutor.resume()
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


