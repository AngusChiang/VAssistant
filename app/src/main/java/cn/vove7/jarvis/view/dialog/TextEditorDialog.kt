package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.widget.EditText
import cn.vove7.jarvis.R
import cn.vove7.jarvis.databinding.EditViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

/**
 * # TextEditorDialog
 *
 * @author 11324
 * 2019/3/12
 */
class TextEditorDialog(val context: Context, val text: String?, builder: MaterialDialog.() -> Unit) {

    init {
        MaterialDialog(context).show {
            val vb = EditViewBinding.inflate(layoutInflater)
            customView(view = vb.root, scrollable = true)
            vb.editText.setText(text)
            apply(builder)
        }
    }

}

val MaterialDialog.editorView: EditText get() = getCustomView().findViewById(R.id.editText)