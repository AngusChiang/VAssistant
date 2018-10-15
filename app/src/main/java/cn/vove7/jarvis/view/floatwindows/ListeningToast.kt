package cn.vove7.jarvis.view.floatwindows

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.model.RequestPermission
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog

/**
 * # ListeningToast
 * 无限显示
 * @author 17719247306
 * 2018/9/9
 */
class ListeningToast(context: Context) : AbFloatWindow<ListeningToast.VHolder>(context) {
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
    var lHandler: ListeningHandler? = null

    init {
    }

    fun show(text: String) {
        if (lHandler == null)
            lHandler = ListeningHandler(Looper.getMainLooper())
        lHandler?.post {
            if (!isShowing)
                show()
        }
        lHandler?.sendMessage(lHandler!!.obtainMessage(SHOW, text))
    }

    fun showAndHideDelay(text: String) {
        lHandler?.sendMessage(lHandler!!.obtainMessage(SHOW, text))
        hideDelay()
    }

    fun hideImmediately() {//立即
        lHandler?.sendEmptyMessage(HIDE)
    }

    inner class ListeningHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                SHOW -> {
                    val text = msg.obj as String
                    holder.text = text
                }
                HIDE -> {
                    Vog.v(this, "hideDelay ---> hide exec")
                    hide()
                }
                else -> {
                }
            }

        }
    }

    companion object {
        const val SHOW = 5
        const val HIDE = 1
    }

    fun hideDelay(delay: Long = 800) {
        Vog.d(this, "hideDelay ---> hide delay $delay")
        lHandler?.sendEmptyMessageDelayed(HIDE, delay)
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