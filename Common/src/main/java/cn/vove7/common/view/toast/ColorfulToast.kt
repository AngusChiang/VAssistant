package cn.vove7.common.view.toast

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.annotation.StringRes
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.vove7.common.R

/**
 * # ColorfulToast
 *
 * @author 17719247306
 * 2018/9/9
 */

class ColorfulToast(val context: Context, textColor: Int = R.color.fff) {
    var toast: Toast

    var toastView: View
    var textView: TextView
    private val lHandler: ToastHandler

    init {
        var looper = Looper.myLooper()
        if (looper == null) {
            Looper.prepare()
            looper = Looper.myLooper()!!


        }
        toast = Toast(context)
        lHandler = ToastHandler(looper)
        toastView = LayoutInflater.from(context.applicationContext).inflate(R.layout.toast_colorful, null)
        textView = toastView.findViewById(R.id.text)
        textView.setTextColor(context.resources.getColor(textColor))
        toast.view = toastView
        toast.setGravity(Gravity.TOP, 0, 30)

    }

    fun bottom(): ColorfulToast {
        toast.setGravity(Gravity.BOTTOM, 0, 30)
        return this
    }

    fun red(): ColorfulToast {
        lHandler.post {
            toastView.setBackgroundResource(R.drawable.round_bg_red)
        }
        return this
    }

    fun green(): ColorfulToast {
        lHandler.post {
            toastView.setBackgroundResource(R.drawable.round_bg_green)
        }
        return this
    }

    fun yellow(): ColorfulToast {
        lHandler.post {
            toastView.setBackgroundResource(R.drawable.round_bg_yellow)
        }
        return this
    }

    fun blue(): ColorfulToast {
        lHandler.post {
            toastView.setBackgroundResource(R.drawable.round_bg_blue)
        }
        return this
    }

    fun showShort(@StringRes textId: Int) {
        showShort(context.getString(textId))
    }

    fun showShortDelay(text: String, delay: Long = 0) {
        lHandler.sendMessageDelayed(lHandler.obtainMessage(SHOW_SHORT, text), delay)
    }

    fun showShort(text: String) {
        lHandler.sendMessage(lHandler.obtainMessage(SHOW_SHORT, text))
    }

    fun showLong(@StringRes textId: Int) {
        showLong(context.getString(textId))
    }

    fun showLong(text: String) {
        lHandler.sendMessage(lHandler.obtainMessage(SHOW_LONG, text))
    }

    fun showAndHideDelay(text: String) {
        lHandler.sendMessage(lHandler.obtainMessage(SHOW_SHORT, text))
        hideDelay()
    }

    fun cancel() {
        toast.cancel()
    }

    fun hideDelay(delay: Long = 800) {
        lHandler.sendEmptyMessageDelayed(HIDE, delay)
    }

    inner class ToastHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                SHOW_SHORT -> {
                    val text = msg.obj as String
                    s(text, Toast.LENGTH_SHORT)
                }
                SHOW_LONG -> {
                    val text = msg.obj as String
                    s(text, Toast.LENGTH_LONG)
                }
                HIDE -> {
                    cancel()
                }
                else -> {
                }
            }
        }
    }

    private fun s(s: String, t: Int) {
        textView.text = s
        toast.duration = t
        toast.show()
    }

    companion object {
        const val SHOW_SHORT = 5
        const val SHOW_LONG = 6
        const val HIDE = 1
    }
}