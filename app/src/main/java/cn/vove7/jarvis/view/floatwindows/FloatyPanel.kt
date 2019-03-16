package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.toast_listening_text.view.*

/**
 * # FloatyPanel
 * 悬浮语音面板
 * @author 17719247306
 * 2018/9/9
 */
class FloatyPanel : AbFloatWindow<FloatyPanel.VHolder>(GlobalApp.APP) {
    override var posY: Int = 80

    lateinit var rootView: LinearLayout
    override fun layoutResId(): Int = R.layout.toast_listening_text
    override fun onCreateViewHolder(view: View): VHolder {
        rootView = view.root
        align = AppConfig.listeningToastAlignDirection
        return VHolder(view).also { v ->
            v.aniImg.post {
                (v.aniImg.drawable as? AnimationDrawable)?.start()
            }
        }
    }

    var align: Int = 0
        set(v) {
            Vog.d("align ---> $v")
            rootView.gravity = when (v) {
                0 -> {//居中
                    Gravity.CENTER
                }
                1 -> {//靠左
                    Gravity.START
                }
                2 -> {//靠右
                    Gravity.END
                }
                else -> Gravity.CENTER
            }
            field = v
        }
    override val onNoPermission: () -> Unit = {
        AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_CANCEL_RECOG)
        AppBus.post(RequestPermission("悬浮窗权限"))
    }

    var ani = R.drawable.listening_animation
    private fun setAniRes(resId: Int) {
        if (ani == resId) return
        ani = resId
        runOnUi {
            holder.aniImg.apply {
                setImageResource(resId)
                post {
                    (drawable as? AnimationDrawable)?.start()
                }
            }
        }
    }

    fun showParseAni() {
        runOnUi {
            setAniRes(R.drawable.parsing_animation)
            if (!isShowing)
                show()
        }
    }

    fun show(text: String) {
//        initIfNeed()
        removeDelayHide()
        runOnUi {
            setAniRes(R.drawable.listening_animation)
            holder.text = text
            if (!isShowing) {
                show()
                contentView.gone()
                contentView.show()
                contentView.post {
                    val animation = AlphaAnimation(0f, 1f)
                    animation.duration = 1000
                    contentView.startAnimation(animation)
                }
            }
        }

    }

    private fun removeDelayHide() {
        delayHandler?.removeCallbacks(delayHide)
    }

    fun showAndHideDelay(text: String) {
        show(text)
        hideDelay(1000)
    }

    var delayHide = Runnable {
        hide()
    }
    var delayHandler: Handler? = null
    fun hideDelay(delay: Long = 800) {
        runOnUi {
            if (delayHandler == null) delayHandler = Handler()
            delayHandler?.postDelayed(delayHide, delay)
        }
        Vog.d("hideDelay ---> hide delay $delay")

    }

    fun hideImmediately() {//立即
        runOnUi {
            hide()
        }
    }


    class VHolder(val view: View) : AbFloatWindow.ViewHolder(view) {
        var text: String
            get() = textView.text.toString()
            set(v) {
                textView.text = v
            }
        val aniImg = view.findViewById<ImageView>(R.id.ani_img)
        var textView: TextView = view.findViewById(R.id.text)
    }


}