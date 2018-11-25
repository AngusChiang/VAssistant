package cn.vove7.common.view.editor

import android.content.Context
import android.graphics.Typeface
import android.support.annotation.ColorRes
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import cn.vove7.common.R


/**
 * # MultiSpan
 * 指定颜色可点击文本
 *
 *
 * Create By Vove
 */
typealias OnClick = (String) -> Unit

/**
 * @param fontSize 单位sp
 */
class MultiSpan(
        var context: Context, val text: String,
        @ColorRes private val colorId: Int = defaultColor, private var fontSize: Int = -1,
        val underLine: Boolean = false, typeface: Int? = null,
        private val onClick: OnClick? = null
) : ClickableSpan() {
    var spanStr: SpannableStringBuilder

    init {
        if (fontSize > 0)
            this.fontSize = DisplayUtils.sp2px(context, fontSize.toFloat())
//        spanStr = SpannableString(text)

        spanStr = SpannableStringBuilder(text)
        spanStr.setSpan(this, 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        if (underLine) {
            spanStr.setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        if (typeface != null) {
            spanStr.setSpan(StyleSpan(typeface),
                    0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun build(): SpannableStringBuilder {
        return spanStr
    }

    override fun onClick(widget: View) {
        onClick?.invoke(text)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = context.resources.getColor(colorId)
        if (fontSize > 0)
            ds.textSize = fontSize.toFloat()
    }

    companion object {

        private val defaultColor = R.color.primary_text
    }
}