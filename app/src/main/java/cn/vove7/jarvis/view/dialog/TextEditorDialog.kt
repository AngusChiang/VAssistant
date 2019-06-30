package cn.vove7.jarvis.view.dialog

import android.app.Activity
import android.content.Context
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.content
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.edit_view.view.*

/**
 * # TextEditorDialog
 *
 * @author 11324
 * 2019/3/12
 */
class TextEditorDialog(context: Activity, val text: String) {
    init {
        MaterialDialog(context).show {
            title(text = "编辑")
            val v = layoutInflater.inflate(R.layout.edit_view, null)
            customView(view = v, scrollable = true)
            v.editText.setText(text)
            positiveButton(text = "复制") {
                SystemBridge.setClipText(v.editText.content())
            }
            negativeButton(text = "分享") {
                SystemBridge.shareText(v.editText.content())
            }
            neutralButton(text = "完成") {
                TextOperationDialog(context,
                        TextOperationDialog.TextModel(v.editText.content()))
                dismiss()
            }
        }
    }

}