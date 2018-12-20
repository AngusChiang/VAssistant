package cn.vove7.jarvis.view.dialog

import android.content.Context
import cn.vove7.jarvis.fragments.base.SheetStatusListener
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithMarkdown

/**
 * # UpdateLogDialog
 *
 * @author Administrator
 * 2018/10/28
 */
class UpdateLogDialog(context: Context, onDismiss: (() -> Unit)? = null) {
    init {
        val d = BottomDialogWithMarkdown(context, "更新日志")

        d.show()
        d.loadFromAsset("files/update_log.md")

        d.listener = object : SheetStatusListener {
            override fun onDismiss() {
                onDismiss?.invoke()
            }
        }
    }

}