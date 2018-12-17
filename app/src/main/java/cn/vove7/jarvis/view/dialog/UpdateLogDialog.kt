package cn.vove7.jarvis.view.dialog

import android.content.Context

/**
 * # UpdateLogDialog
 *
 * @author Administrator
 * 2018/10/28
 */
class UpdateLogDialog(context: Context, onDismiss: (() -> Unit)? = null) {
    init {
        val d = MarkDownDialog(context, "更新日志")
        d.loadFromAsset("files/update_log.md")
        d.show()

        d.onDismiss {
            onDismiss?.invoke()
        }
        d.finish()
    }

}