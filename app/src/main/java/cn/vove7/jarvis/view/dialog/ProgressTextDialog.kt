package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.widget.TextView
import cn.vove7.common.view.editor.MultiSpan
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # ProgressTextDialog
 *
 * @author Administrator
 * 2018/10/18
 */
class ProgressTextDialog(val context: Context, val title: String? = null,
                         val cancelable: Boolean = true) {
    val dialog = MaterialDialog(context)
    val textView = TextView(context)

    val handler = UiHandler(textView, Looper.getMainLooper())

    init {
        textView.setPadding(60, 0, 60, 0)
        textView.gravity = Gravity.BOTTOM
        textView.setTextColor(context.resources.getColor(R.color.primary_text))
        dialog.title(text = title)
                .customView(view = textView, scrollable = true)
                .cancelable(cancelable)
                .show()
    }

    fun seletable(b: Boolean) {
        textView.setTextIsSelectable(b)
    }

    @Synchronized
    fun appendln(s: Any? = null) {
        if (s != null) append(s)
        append("\n")
    }

    fun appendlnGreen(s: String) {
        appendlnColor(s, R.color.green_700)
    }

    fun appendlnRed(s: String) {
        appendlnColor(s, R.color.red_900)
    }

    fun appendlnAmber(s: String) {
        appendlnColor(s, R.color.amber_A700)
    }

    private fun appendlnColor(s: String, color: Int) {
        val ss = MultiSpan(context, s, color).spanStr
        appendln(ss)
    }

    fun set(s: Any) {
        handler.sendMessage(handler.obtainMessage(SET, s))
    }

    fun clear() {
        set("")
    }

    fun append(s: Any) {
        handler.sendMessage(handler.obtainMessage(APPEND, s))
    }

    fun finish() {
        Handler(Looper.getMainLooper()).post {
            dialog.positiveButton { it.dismiss() }
        }
    }

    class UiHandler(val textView: TextView, loop: Looper) : Handler(loop) {
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

    companion object {
        private const val APPEND = 0
        private const val SET = 1
    }


}