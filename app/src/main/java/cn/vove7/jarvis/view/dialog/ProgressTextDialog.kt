package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.View
import androidx.annotation.ColorRes
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.color
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.noAutoScroll
import cn.vove7.jarvis.view.custom.WrappedTextView
import cn.vove7.jarvis.view.dialog.base.CustomizableDialog

/**
 * # ProgressTextDialog
 *
 * @author Administrator
 * 2018/10/18
 */
open class ProgressTextDialog(context: Context, title: String? = null,
                              cancelable: Boolean = true, noAutoDismiss: Boolean = false,
                              autoScroll: Boolean = false, var autoLink: Boolean = false)
    : CustomizableDialog(context, title, cancelable, noAutoDismiss) {
    private val textView by lazy {
        WrappedTextView(context).also {
            it.isEditable = false
            it.background = null
            if (SystemBridge.isDarkMode) {
                it.setTextColor(Color.WHITE)
            }
        }
    }

    init {
        show()
        if (!autoScroll) {
            dialog.noAutoScroll()
        }
    }

    fun show(func: ProgressTextDialog.() -> Unit): ProgressTextDialog {
        this.func()
        show()
        return this
    }

    override fun initView(): View {
        textView.setPadding(60, 0, 60, 0)
        if (autoLink) {
            textView.autoLinkMask = Linkify.WEB_URLS
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
        selectable(true)
        return textView
    }

    fun selectable(b: Boolean) {
        textView.selectable(b)
    }

    fun appendlnBold(text: String, fontSize: Int = 15) {
        textView.appendlnBold(text, fontSize)
    }

    @Synchronized
    fun appendln(s: Any? = null): ProgressTextDialog {
        if (s != null) textView.appendln(s)
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

    fun append(s: Any): ProgressTextDialog {
        textView.mappend(s)
        return this
    }

    fun appendlnAmber(s: String): ProgressTextDialog {
        appendlnColor(s, R.color.amber_A700)
        return this
    }

    private fun appendlnColor(s: String, @ColorRes color: Int): ProgressTextDialog {
        val ss = s.spanColor(context.color(color))
        appendln(ss)
        return this
    }

    fun clear() {
        textView.set("")
    }

    fun scrollToTop() {
        Handler().postDelayed({
            dialog.noAutoScroll()
        }, 1000)
    }

    companion object {
        private const val APPEND = 0
        private const val SET = 1
    }


}