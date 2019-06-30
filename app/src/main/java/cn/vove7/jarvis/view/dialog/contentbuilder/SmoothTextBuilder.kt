package cn.vove7.jarvis.view.dialog.contentbuilder

import android.view.View
import cn.vove7.bottomdialog.interfaces.ContentBuilder
import cn.vove7.bottomdialog.util.listenToUpdate
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.custom.scmoothtextview.TextWebView
import kotlinx.android.synthetic.main.dialog_content_text_web_view.view.*

/**
 * # SmoothTextBuilder
 *
 * @author Vove
 * 2019/6/30
 */
class SmoothTextBuilder : ContentBuilder() {

    var html: String? by listenToUpdate(null, this, 1)
    var text: String? by listenToUpdate(null, this, 2)

    override val layoutRes: Int
        get() = R.layout.dialog_content_text_web_view

    lateinit var textWebView: TextWebView

    override fun init(view: View) {
        textWebView = view.text_web_view
    }

    override fun updateContent(type: Int, data: Any?) {
        if (type == -1 || type == 1) {
            html?.also {
                textWebView.setHtml(it)
            }
        }
        if (type == -1 || type == 2) {
            text?.also { textWebView.setSource(it) }
        }
    }
}