package cn.vove7.jarvis.view.dialog.contentbuilder

import android.view.View
import cn.vove7.bottomdialog.interfaces.ContentBuilder
import cn.vove7.bottomdialog.util.listenToUpdate
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.custom.WrappedTextView
import kotlinx.android.synthetic.main.wrapped_text_view.view.*

/**
 * # TextContentBuilder
 *
 * @author Vove
 * 2019/6/30
 */
class WrappedTextContentBuilder(init: CharSequence?) : ContentBuilder() {

    var text by listenToUpdate(init, this)

    override val layoutRes: Int get() = R.layout.wrapped_text_view

    lateinit var textView: WrappedTextView

    override fun init(view: View) {
        textView = view.text_view
    }

    override fun updateContent(type: Int, data: Any?) {
        textView.text = text
    }
}