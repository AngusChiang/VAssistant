package cn.vove7.jarvis.view.floatwindows

import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.BaseAction
import cn.vove7.appbus.VoiceData
import cn.vove7.common.model.ScreenMetrics
import cn.vove7.jarvis.R
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.SpeechRecoService
import cn.vove7.jarvis.utils.ServiceChecker
import cn.vove7.vtp.floatwindow.AbFloatWindow
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 语音悬浮窗
 *
 * Created by Vove on 2018/7/1
 */
class VoiceFloat(
        context: Context,
        posX: Int = 1920

        ,
        posY: Int = 500,
        mParams: WindowManager.LayoutParams? = null
) : AbFloatWindow<VoiceFloat.Holder>(context, mParams, posX, posY) {
    val screenSize: Point = Point(ScreenMetrics.deviceScreenWidth, ScreenMetrics.deviceScreenHeight)

    override fun layoutResId(): Int = R.layout.float_voice

    override fun onCreateViewHolder(view: View): Holder {

        var downX = 0.0
        var downY = 0.0
        var lastX = 0.0
        var lastY = 0.0
        var isClick = false

        val h = Holder(view)
        h.floatView.setOnTouchListener(View.OnTouchListener { v, event ->
            Vog.d(this, "OnTouchListener $event")
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        onClick()
                    } else {
                    }
                    isClick = false
                }
                MotionEvent.ACTION_MOVE -> {
                    updatePoint(event.rawX.toInt(), event.rawY.toInt())
                    isClick = false
                }
                MotionEvent.ACTION_DOWN -> {
                    isClick = true
                }
            }
            return@OnTouchListener true
        })

        return h
    }

    fun onClick() {
        if (SpeechRecoService.instance.isListening()) {
            AppBus.postSpeechRecoAction(BaseAction.ACTION_STOP)
        } else {
            ServiceChecker(context).checkService()
            AppBus.postSpeechRecoAction(BaseAction.ACTION_START)
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
        val voiceImage = view.findViewById<ImageButton>(R.id.voice)!!
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
            MainService.WHAT_VOICE_ERR -> {//TODO
                holder.result.text = "识别失败"
            }
            MainService.WHAT_VOICE_RESULT -> {
                holder.result.text = "result: ${data.data}"
            }
        }
    }
}

