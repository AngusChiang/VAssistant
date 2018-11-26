package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.TextView
import cn.vove7.common.view.editor.MultiSpan
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.scrollToTop
import cn.vove7.jarvis.view.dialog.base.CustomizableDialog
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.callbacks.onPreShow

/**
 * # ProgressTextDialog
 *
 * @author Administrator
 * 2018/10/18
 */
open class ProgressTextDialog(context: Context, title: String? = null,
                              cancelable: Boolean = true, noAutoDismiss: Boolean = false,
                              val autoScroll: Boolean = false)
    : CustomizableDialog(context, title, cancelable, noAutoDismiss) {
    val textView by lazy { TextView(context) }

    val handler = UiHandler(textView, Looper.getMainLooper())

    init {
        show()
    }

    fun show(func: ProgressTextDialog.() -> Unit): ProgressTextDialog {
        this.func()
        show()
        return this
    }

    override fun initView(): View {
        textView.setPadding(60, 0, 60, 0)
        Vog.d(this, "initView ---> 111111111")
        selectable(true)
        dialog.onPreShow {
            if (!autoScroll) {//fixme don't work
                textView.also {
                    it.isFocusable = true
                    it.requestFocus()
                    it.gravity = Gravity.BOTTOM
                }
            }
        }

        textView.setTextColor(context.resources.getColor(R.color.primary_text))
        return textView
    }

    fun selectable(b: Boolean) {
        textView.setTextIsSelectable(b)
    }


    @Synchronized
    fun appendln(s: Any? = null): ProgressTextDialog {
        if (s != null) append(s)
        append("\n")
        return this
    }

    fun appendlnGreen(s: String): ProgressTextDialog {
        appendlnColor(s, R.color.green_700)
        return this
    }

    fun appendlnRed(s: String): ProgressTextDialog {
        appendlnColor(s, R.color.red_900)
        return this
    }

    fun appendlnAmber(s: String): ProgressTextDialog {
        appendlnColor(s, R.color.amber_A700)
        return this
    }


    private fun appendlnColor(s: String, color: Int): ProgressTextDialog {
        val ss = MultiSpan(context, s, color).spanStr
        appendln(ss)
        return this
    }

    fun set(s: Any) {
        handler.sendMessage(handler.obtainMessage(SET, s))
    }

    fun clear() {
        set("")
    }

    fun append(s: Any): ProgressTextDialog {
        handler.sendMessage(handler.obtainMessage(APPEND, s))
        return this
    }

    class UiHandler(private val textView: TextView, loop: Looper) : Handler(loop) {
        override fun handleMessage(msg: Message?) {
            val data = msg?.obj
            when (msg?.what) {
                APPEND -> {
                    when (data) {
                        is CharSequence -> textView.append(data)
                        is SpannableStringBuilder -> textView.append(data)
                    }
                }
                SET -> {
                    when (data) {
                        is CharSequence -> textView.text = data
                        is SpannableStringBuilder -> textView.text = data
                    }
                }
            }

        }
    }

    fun scrollToTop() {
        Handler().postDelayed({
            dialog.scrollToTop()
        }, 1000)
    }

    companion object {
        private const val APPEND = 0
        private const val SET = 1
    }


}