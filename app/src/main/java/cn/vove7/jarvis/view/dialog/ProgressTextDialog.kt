package cn.vove7.jarvis.view.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.annotation.StringRes
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.widget.TextView
import cn.vove7.common.view.editor.MultiSpan
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView

/**
 * # ProgressTextDialog
 *
 * @author Administrator
 * 2018/10/18
 */
class ProgressTextDialog(val context: Context, val title: String? = null,
                         val cancelable: Boolean = true, noAutoDismiss: Boolean = false, autoScroll: Boolean = false) {
    val dialog = MaterialDialog(context)
    val textView = TextView(context)

    val handler = UiHandler(textView, Looper.getMainLooper())

    init {
        textView.setPadding(60, 0, 60, 0)
        //todo
//        if (autoScroll) textView.gravity = Gravity.TOP
//        else
// textView.gravity = Gravity.BOTTOM

        textView.setTextColor(context.resources.getColor(R.color.primary_text))
        dialog.title(text = title)
                .customView(view = textView, scrollable = true)
                .cancelable(cancelable)
                .show {
                    if (noAutoDismiss)
                        noAutoDismiss()
                }
        seletable(true)
    }

    fun seletable(b: Boolean) {
        textView.setTextIsSelectable(b)
    }

    @SuppressLint("CheckResult")
    fun positiveButton(
            @StringRes res: Int? = null,
            text: CharSequence? = null,
            click: DialogCallback? = null
    ): ProgressTextDialog {
        dialog.positiveButton(res, text, click)
        return this
    }

    @SuppressLint("CheckResult")
    fun negativeButton(
            @StringRes res: Int? = null,
            text: CharSequence? = null,
            click: DialogCallback? = null
    ): ProgressTextDialog {
        dialog.negativeButton(res, text, click)
        return this
    }

    @SuppressLint("CheckResult")
    fun neutralButton(
            @StringRes res: Int? = null,
            text: CharSequence? = null,
            click: DialogCallback? = null
    ): ProgressTextDialog {
        dialog.neutralButton(res, text, click)
        return this
    }

    fun onDismiss(callback: DialogCallback): ProgressTextDialog {
        dialog.onDismiss {
            callback.invoke(it)
        }
        return this
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

    fun dismiss() {
        dialog.dismiss()
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

    fun finish() {
        Handler(Looper.getMainLooper()).post {
            dialog.positiveButton { it.dismiss() }
        }
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

    companion object {
        private const val APPEND = 0
        private const val SET = 1
    }


}