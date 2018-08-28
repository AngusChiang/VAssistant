package cn.vove7.jarvis.view.floatwindows

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Paint
import android.graphics.Point
import android.view.MotionEvent
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechRecoAction
import cn.vove7.common.appbus.VoiceData
import cn.vove7.jarvis.R
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.SpeechRecoService
import cn.vove7.jarvis.speech.recognition.model.IStatus
import cn.vove7.jarvis.utils.ServiceChecker
import cn.vove7.vtp.floatwindow.AbFloatWindow
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.ScreenInfo
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 语音悬浮窗
 *
 * Created by Vove on 2018/7/1
 */
class VoiceFloat : AbFloatWindow<VoiceFloat.Holder> {

    override var posX: Int = 500
    override var posY: Int = 500
    /**
     * 移动动画
     */
    private var smallImgMoveToEdgeAnim: ValueAnimator? = null

    constructor(context: Context) : super(context) {
        this.screenInfo = DeviceInfo.getInfo(context).screenInfo
        posX = screenInfo.width
        posY = (screenInfo.height * 0.6).toInt()
        contentView.post {
            posX = screenInfo.width - contentView.width
        }
        Vog.d(this, " $posX $posY")
    }

    val screenInfo: ScreenInfo

    override fun layoutResId(): Int = R.layout.float_voice

    var onMoving = false
    var align = Paint.Align.LEFT

    override fun onCreateViewHolder(view: View): Holder {

        var isClick = false
        var downPoint = Point(0, 0)
        var dx = 0
        var dy = 0

        val h = Holder(view)
        h.floatView.setOnTouchListener(View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        onClick()
                    } else {
                        posX = event.rawX.toInt() - dx
                        posY = event.rawY.toInt() - dy
                        val desX = getdesX()
                        attachToEdge(desX)
                    }
                    isClick = false
                    onMoving = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val nowPoint = Point(event.rawX.toInt(), event.rawY.toInt())
                    if (!onMoving && !isBigMove(downPoint, nowPoint))
                        return@OnTouchListener false
                    posX = event.rawX.toInt() - dx
                    posY = event.rawY.toInt() - dy
                    Vog.d(this, "onCreateViewHolder move")
                    updatePoint(posX, posY)
                    onMoving = true
                    isClick = false
                }
                MotionEvent.ACTION_DOWN -> {
                    isClick = true
                    onMoving = false
                    dx = event.rawX.toInt() - posX
                    dy = event.rawY.toInt() - posY
//                    Vog.d(this, "ACTION_DOWN $posX $posY")
//                    Vog.d(this, "ACTION_DOWN $dx $dy")
                    downPoint = Point(event.rawX.toInt(), event.rawY.toInt())
                }
            }
            return@OnTouchListener false
        })
        return h
    }


    private fun getdesX(): Int {

        val or = windowManager.defaultDisplay.rotation
        val s = if (or == ROTATION_0 || or == ROTATION_180) {
            align = Paint.Align.LEFT
            screenInfo.width
        } else {
            align = Paint.Align.RIGHT
            screenInfo.height
        }
        return if (posX > s / 2) s - contentView.width else 0

    }

    private fun isBigMove(raw: Point, rel: Point): Boolean {
        val dx = raw.x - rel.x
        val dy = raw.y - rel.y
//        Vog.d(this, "isBigMove $dx $dy")
        return (dx * dx + dy * dy) > 400
    }

    private fun attachToEdge(desX: Int) {
        if (smallImgMoveToEdgeAnim != null && smallImgMoveToEdgeAnim!!.isRunning) {
            smallImgMoveToEdgeAnim!!.cancel()
        }
        smallImgMoveToEdgeAnim = ValueAnimator.ofInt(posX, desX)
        smallImgMoveToEdgeAnim?.duration = 300
        smallImgMoveToEdgeAnim?.interpolator = AccelerateDecelerateInterpolator()
        smallImgMoveToEdgeAnim?.addUpdateListener { animation ->
            posX = animation.animatedValue as Int
            try {
                updatePoint(posX, posY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        smallImgMoveToEdgeAnim?.start()
    }


    // TODO 效果
    fun onClick() {
        holder.result.text = "begin"
        if (SpeechRecoService.instance?.isListening() == true) {
            AppBus.postSpeechRecoAction(SpeechRecoAction.ActionCode.ACTION_CANCEL_RECO)
        } else {
            ServiceChecker(context).checkService()
            AppBus.postSpeechRecoAction(SpeechRecoAction.ActionCode.ACTION_START_RECO)
        }
    }


    override fun show() {
        AppBus.reg(this)
        super.show()
    }

    override fun hide() {
        AppBus.unreg(this)
        val desX = getdesX()
        attachToEdge(desX)
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
            IStatus.STATUS_WAKEUP_SUCCESS -> {
                holder.result.text = "唤醒成功"
            }
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

