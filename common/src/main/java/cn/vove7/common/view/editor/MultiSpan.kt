package cn.vove7.common.view.editor

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.view.View


/**
 * # MultiSpan
 * 指定颜色可点击文本
 *
 *
 * Create By Vove
 */

/**
 * @param fontSize 单位dp
 */
class MultiSpan(
        val text: String, selectionText: String = text,
        color: Int? = null, fontSize: Int? = null,
        underLine: Boolean = false,
        /**
         * @[android.graphics.Typeface]
         */
        typeface: Int? = null,
        onClick: ((String) -> Unit)? = null
) {
    var spanStr = SpannableStringBuilder(text)
    val start = text.indexOf(selectionText)
    val end = start + selectionText.length

    init {

        onClick?.also {
            val cs = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    it.invoke(text)
                }
            }
            spanStr.setSpan(cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }


        fontSize?.also {
            val sizeSpan = AbsoluteSizeSpan(it, true)
            spanStr.setSpan(sizeSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        color?.also {
            val cs = ForegroundColorSpan(it)
            spanStr.setSpan(cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (underLine) {
            spanStr.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        typeface?.also {
            spanStr.setSpan(StyleSpan(it), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun build(): SpannableStringBuilder {
        return spanStr
    }
}