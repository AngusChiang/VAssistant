package cn.vove7.jarvis.view.dialog.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import cn.vove7.jarvis.fragments.base.BaseBottomDialogWithToolbar
import cn.vove7.jarvis.view.custom.scmoothtextview.TextWebView

/**
 * # BottomDialogWithSmoothText
 * @author 11324
 * 2019/4/13
 */
class BottomDialogWithSmoothText(
        context: Context, title: String)
    : BaseBottomDialogWithToolbar(context, title) {

    private val textView by lazy { TextWebView(context) }

    override fun onCreateContentView(parent: View): View {
        textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        textView.minimumHeight = 500
        return textView
    }

    fun setHtml(html: String) {
        textView.setHtml(html)
    }

    fun setText(text: String, wrap: Boolean = false) {
        textView.setSource(text, wrap)
    }

}