package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.widget.EditText
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.edit_view.view.*

/**
 * # TextEditorDialog
 *
 * @author 11324
 * 2019/3/12
 */
class TextEditorDialog(val context: Context, val text: String?, builder: MaterialDialog.() -> Unit) {

    init {
        MaterialDialog(context).show {
            val v = layoutInflater.inflate(R.layout.edit_view, null)
            customView(view = v, scrollable = true)
            v.editText.setText(text)
            apply(builder)
        }
    }

}

val MaterialDialog.editorView: EditText get() = getCustomView()!!.editText