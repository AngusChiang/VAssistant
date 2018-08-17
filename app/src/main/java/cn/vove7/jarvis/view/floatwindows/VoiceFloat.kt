package cn.vove7.jarvis.view.floatwindows

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.SpeechRecoAction
import cn.vove7.appbus.VoiceData
import cn.vove7.jarvis.R
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.SpeechRecoService
import cn.vove7.jarvis.utils.ServiceChecker
import cn.vove7.vtp.floatwindow.AbFloatWindow
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.ScreenInfo
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.logging.Handler

/**
 * 语音悬浮窗
 *
 * Created by Vove on 2018/7/1
 */
class VoiceFloat : AbFloatWindow<VoiceFloat.Holder> {

    override var posX: Int = 500
    override var posY: Int = 500

    constructor(context: Context) : super(context) {
        this.screenInfo = DeviceInfo.getInfo(context).screenInfo
        posX = screenInfo.width - contentView.width
        posY = (screenInfo.height * 0.8).toInt()
        Vog.d(this," $posX $posY")
    }

    val screenInfo: ScreenInfo

    override fun layoutResId(): Int = R.layout.float_voice

    override fun onCreateViewHolder(view: View): Holder {

        val h = Holder(view)
//        h.floatView.setOnTouchListener(View.OnTouchListener { v, event ->
//            Vog.d(this, "OnTouchListener $event")
//            when (event.action) {
//                MotionEvent.ACTION_UP -> {
//                    if (isClick) {
//                        onClick()
//                    } else {
//                    }
//                    isClick = false
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    updatePoint(event.rawX.toInt(), event.rawY.toInt())
//                    isClick = false
//                }
//                MotionEvent.ACTION_DOWN -> {
//                    isClick = true
//                }
//            }
//            return@OnTouchListener true
//        })
        h.floatView.setOnClickListener { onClick() }
        return h
    }

    fun onClick() {
        if (SpeechRecoService.instance.isListening()) {
            AppBus.postSpeechRecoAction(SpeechRecoAction.ActionCode.ACTION_STOP_RECO)
        } else {
            ServiceChecker(context).checkService()
            AppBus.postSpeechRecoAction(SpeechRecoAction.ActionCode.ACTION_START_RECO)
        }
    }

    fun autoAttachEdge(x: Int, y: Int) {

    }

    override fun show() {
        AppBus.reg(this)
        super.show()
    }

    override fun hide() {
        AppBus.unreg(this)
        super.hide()
    }

    class Holder(view: View) : AbFloatWindow.ViewHolder(view) {
        val voiceImage = view.findViewById<ImageView>(R.id.voice)!!
        val result = view.findViewById<TextView>(R.id.result)!!
    }

    // TODO 事件响应效果
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showResult(data: VoiceData) {
        when (data.what) {
            MainService.WHAT_VOICE_TEMP -> {
                holder.result.text = data.data
            }
            MainService.WHAT_VOICE_VOL -> {

            }
            MainService.WHAT_VOICE_ERR -> {
                holder.result.text = "识别失败"
            }
            MainService.WHAT_VOICE_RESULT -> {
                holder.result.text = "result: ${data.data}"
            }
        }
    }
}

