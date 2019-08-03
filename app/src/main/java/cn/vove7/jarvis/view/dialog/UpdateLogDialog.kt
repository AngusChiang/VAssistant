package cn.vove7.jarvis.view.dialog

import android.app.Activity
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.title
import cn.vove7.bottomdialog.builder.withCloseIcon
import cn.vove7.bottomdoalog.extension.awesomeHeader
import cn.vove7.jarvis.view.dialog.contentbuilder.MarkdownContentBuilder

/**
 * # UpdateLogDialog
 *
 * @author Administrator
 * 2018/10/28
 */
class UpdateLogDialog(context: Activity, onDismiss: (() -> Unit)? = null) {

    init {
        val d = BottomDialog.builder(context) {
            awesomeHeader("更新日志")
            content(MarkdownContentBuilder()) {
                loadMarkdownFromAsset("files/update_log.md")
            }
        }
        d.setOnDismissListener {
            onDismiss?.invoke()
        }
    }

}