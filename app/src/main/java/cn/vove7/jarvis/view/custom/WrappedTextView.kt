package cn.vove7.jarvis.view.custom

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.widget.TextView
import cn.vove7.common.utils.span
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.R

/**
 * # WrappedTextView
 *
 * @author Administrator
 * 2018/12/19
 */
class WrappedTextView : android.support.v7.widget.AppCompatTextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    fun selectable(b: Boolean) {
        setTextIsSelectable(b)
    }

    fun appendlnBold(text: String, fontSize: Int = 15) {
        appendln(text.span(fontSize = fontSize, typeface = Typeface.BOLD))
    }

    val handler by lazy { UiHandler(this, Looper.getMainLooper()) }

    @Synchronized
    fun appendln(s: Any? = null) {
        if (s != null) mappend(s)
        mappend("\n")
    }

    @Synchronized
    fun appendlnRed(s: String) {
        appendlnColor(s, R.color.red_900)
    }

    @Synchronized
    fun appendlnGreen(s: String) {
        appendlnColor(s, R.color.green_700)
    }

    @Synchronized
    private fun appendlnColor(s: String, @ColorRes color: Int) {
        val ss = s.spanColor(ContextCompat.getColor(context, color))
        appendln(ss)
    }

    @Synchronized
    fun set(s: Any) {
        handler.sendMessage(handler.obtainMessage(SET, s))
    }

    @Synchronized
    fun clear() {
        handler.sendMessage(handler.obtainMessage(CLEAR))
    }

    @Synchronized
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
                CLEAR -> textView.text = ""
            }

        }
    }

    companion object {
        private const val APPEND = 0
        private const val SET = 1
        private const val CLEAR = -1
    }

}