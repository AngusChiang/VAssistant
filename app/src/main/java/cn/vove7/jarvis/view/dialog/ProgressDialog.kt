package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.os.Handler
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # ProgressDialog
 *
 * @author Administrator
 * 2018/9/20
 */
class ProgressDialog(val context: Context, whenAutoClose: (() -> Unit)? = null) {
    val dialog = MaterialDialog(context)
    val handler: Handler
    private val autoDismiss = Runnable {
        //15s强制关闭
        if (dialog.isShowing) {
            dismiss()
            whenAutoClose?.invoke()
        }
    }

    init {
        dialog.customView(R.layout.dialog_progress)
                .cancelable(false)
                .show()
        handler = Handler()
        handler.postDelayed(autoDismiss
                , 15000)
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
            handler.removeCallbacks(autoDismiss)
        }
    }
}