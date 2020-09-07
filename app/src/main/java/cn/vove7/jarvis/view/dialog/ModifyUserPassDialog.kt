package cn.vove7.jarvis.view.dialog

import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.app.AppApi
import cn.vove7.jarvis.view.checkEmpty
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

                GlobalScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        AppApi.modifyPass(old, newP1)
                    }.onSuccess { b ->
                        if (b.isOk()) {
                            GlobalApp.toastSuccess(R.string.text_modify_succ)
                            withContext(Dispatchers.Main) {
                                it.dismiss()
                            }
                        } else {
                            GlobalApp.toastInfo(b.message)
                        }
                    }.onFailure {e ->
                        e.log()
                        GlobalApp.toastError(e.message
                            ?: context.getString(R.string.text_modify_failed))
                    }
                    withContext(Dispatchers.Main){
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