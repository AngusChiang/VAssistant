package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.support.design.widget.TextInputLayout
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.tool.SecureHelper
import cn.vove7.common.utils.NetHelper
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # ModifyUserPassDialog
 *
 * @author Administrator
 * 9/29/2018
 */
class ModifyUserPassDialog(val context: Context) {
    val toast = ColorfulToast(context).yellow()

    val dialog = MaterialDialog(context)
            .customView(R.layout.dialog_modify_pass, scrollable = true)
            .noAutoDismiss()
            .positiveButton {
                var old = checkEmptyErr(oldPassIn) ?: return@positiveButton
                var newP1 = checkEmptyErr(newPassIn1) ?: return@positiveButton
                val newP2 = checkEmptyErr(newPassIn2) ?: return@positiveButton
                if (newP1 != newP2) {
                    newPassIn2.error = "两次密码不一致"
                    return@positiveButton
                }
                old = SecureHelper.MD5(old)
                newP1 = SecureHelper.MD5(newP1)
                val p = ProgressDialog(context)
                NetHelper.postJson<Any>(ApiUrls.MODIFY_PASS, BaseRequestModel(old, newP1)) { _, b ->
                    p.dismiss()
                    if (b?.isOk() == true) {
                        toast.showShort(R.string.text_modify_succ)
                        it.dismiss()
                    } else {
                        toast.showShort(b?.message ?: context.getString(R.string.text_modify_failed))
                    }
                }

            }
    init {
        dialog.show()
    }


    private val oldPassIn: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.old_pass) }
    private val newPassIn1: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.new_pass1) }
    private val newPassIn2: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.new_pass2) }


    private fun checkEmptyErr(it: TextInputLayout): String? {
        val s = it.editText?.text.toString()
        if (s.trim() == "") {
            it.error = context.getString(R.string.text_not_empty)
            return null
        }
        it.error = ""
        return s
    }
}