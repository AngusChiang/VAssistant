package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.support.design.widget.TextInputLayout
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.jarvis.net.ApiUrls
import cn.vove7.jarvis.net.WrapperNetHelper
import cn.vove7.jarvis.net.tool.SecureHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.checkEmpty
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # ModifyUserPassDialog
 *
 * @author Administrator
 * 9/29/2018
 */
class ModifyUserPassDialog(val context: Context) {

    val dialog = MaterialDialog(context)
            .title(R.string.text_modify_pass)
            .customView(R.layout.dialog_modify_pass, scrollable = true)
            .noAutoDismiss()
            .positiveButton {
                var old = oldPassIn.checkEmpty() ?: return@positiveButton
                var newP1 = newPassIn1.checkEmpty() ?: return@positiveButton
                val newP2 = newPassIn2.checkEmpty() ?: return@positiveButton
                if (newP1 != newP2) {
                    newPassIn2.error = "两次密码不一致"
                    return@positiveButton
                }
                old = SecureHelper.MD5(old)
                newP1 = SecureHelper.MD5(newP1)
                val p = ProgressDialog(context)
                WrapperNetHelper.postJson<Any>(ApiUrls.MODIFY_PASS, model = old, arg1 = newP1) {
                    success { _, b ->
                        if (b.isOk()) {
                            GlobalApp.toastSuccess(R.string.text_modify_succ)
                            it.dismiss()
                        } else {
                            GlobalApp.toastInfo(b.message)
                        }
                    }
                    fail { _, e ->
                        e.log()
                        GlobalApp.toastError(e.message
                            ?: context.getString(R.string.text_modify_failed))
                    }
                    end {
                        p.dismiss()
                    }
                }

            }

    init {
        dialog.show()
    }


    private val oldPassIn: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.old_pass) }
    private val newPassIn1: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.new_pass1) }
    private val newPassIn2: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.new_pass2) }

}