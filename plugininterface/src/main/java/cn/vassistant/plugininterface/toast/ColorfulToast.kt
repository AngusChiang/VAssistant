package cn.vassistant.plugininterface.toast

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.vassistant.plugininterface.R
import cn.vassistant.plugininterface.app.GlobalLog
import java.lang.reflect.Field


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

        hook(toast)
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

    fun showShort(textId: Int) {
        showShort(context.getString(textId))
    }

    fun showShortDelay(text: String, delay: Long = 0) {
        lHandler.sendMessageDelayed(lHandler.obtainMessage(SHOW_SHORT, text), delay)
    }

    fun showShort(text: String) {
        lHandler.sendMessage(lHandler.obtainMessage(SHOW_SHORT, text))
    }

    fun showLong(textId: Int) {
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
        try {
            textView.text = s
            toast.duration = t
            toast.show()
        } catch (e: Exception) {
            GlobalLog.err("toast 显示失败")
            GlobalLog.err(e)
        }
    }

    companion object {
        const val SHOW_SHORT = 5
        const val SHOW_LONG = 6
        const val HIDE = 1

        private var sField_TN: Field? = null
        private var sField_TN_Handler: Field? = null

        init {
            try {
                sField_TN = Toast::class.java.getDeclaredField("mTN")
                sField_TN?.isAccessible = true
                sField_TN_Handler = sField_TN?.type?.getDeclaredField("mHandler")
                sField_TN_Handler?.isAccessible = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * https://blog.csdn.net/icuihai/article/details/81179105?tdsourcetag=s_pcqq_aiomsg
         * @param toast Toast
         */
        private fun hook(toast: Toast) {
            try {
                val tn = sField_TN?.get(toast)
                val preHandler = sField_TN_Handler?.get(tn) as Handler?
                sField_TN_Handler?.set(tn, SafelyHandlerWarpper(preHandler))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class SafelyHandlerWarpper(val impl: Handler?) : Handler() {

        override fun dispatchMessage(msg: Message) {
            try {
                super.dispatchMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun handleMessage(msg: Message?) {
            impl?.handleMessage(msg)//需要委托给原Handler执行
        }
    }
}