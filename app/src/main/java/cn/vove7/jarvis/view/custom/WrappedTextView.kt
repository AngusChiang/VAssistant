package cn.vove7.jarvis.view.custom

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.widget.TextView
import cn.vove7.common.view.editor.MultiSpan

/**
 * # WrappedTextView
 *
 * @author Administrator
 * 2018/12/19
 */
class WrappedTextView : TextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    fun selectable(b: Boolean) {
        setTextIsSelectable(b)
    }

    fun appendlnBold(text: String, fontSize: Int = 15) {
        appendln(MultiSpan(context, text, fontSize = fontSize, typeface = Typeface.BOLD).spanStr)
    }

    val handler by lazy { UiHandler(this, Looper.getMainLooper()) }
    @Synchronized
    fun appendln(s: Any? = null) {
        if (s != null) mappend(s)
        mappend("\n")
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

    fun mappend(s: Any) {
        handler.sendMessage(handler.obtainMessage(APPEND, s))
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