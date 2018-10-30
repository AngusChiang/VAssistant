package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog

/**
 * # ListeningToast
 * 无限显示
 * @author 17719247306
 * 2018/9/9
 */
class ListeningToast : AbFloatWindow<ListeningToast.VHolder>(GlobalApp.APP) {
    override var posY: Int = 80

    override fun layoutResId(): Int = R.layout.toast_listening_text
    override fun onCreateViewHolder(view: View): VHolder {
        return VHolder(view).also { v ->
            v.aniImg.post {
                (v.aniImg.drawable as? AnimationDrawable)?.start()
            }
        }
    }

    override val onNoPermission: () -> Unit = {
        AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_CANCEL_RECO)
        AppBus.post(RequestPermission("悬浮窗权限"))

    }

    init {
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
            if (!isShowing)
                show()
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
        Vog.d(this, "hideDelay ---> hide delay $delay")

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