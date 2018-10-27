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
class ProgressDialog(val context: Context) {
    val dialog = MaterialDialog(context)

    init {
        dialog.customView(R.layout.dialog_progress)
                .cancelable(false)
                .show()
        Handler().postDelayed({
            //10s强制关闭
            if (dialog.isShowing)
                dismiss()
        }, 15000)
    }

    fun dismiss() {
        if(dialog.isShowing) {
            dialog.dismiss()
        }
    }
}