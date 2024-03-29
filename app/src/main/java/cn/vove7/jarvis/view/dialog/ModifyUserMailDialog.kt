package cn.vove7.jarvis.view.dialog

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.app.AppApi
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.view.checkEmpty
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * # ModifyUserMailDialog
 *
 * @author 11324
 * 2019/4/20
 */
class ModifyUserMailDialog(val context: Context, val onUpdate: () -> Unit) {

    val dialog = MaterialDialog(context)
            .title(text = "修改邮箱")
            .customView(R.layout.dialog_modify_mail, scrollable = true)
            .noAutoDismiss()
            .positiveButton(text = "修改") {
                modify()
            }
            .negativeButton { it.dismiss() }

    private val userPass: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.user_pass) }
    private val newMail: TextInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.new_mail) }

    init {
        dialog.show()
    }

    private fun modify() {
        var pass = userPass.checkEmpty() ?: return
        val newMailAddress = newMail.checkEmpty() ?: return

        if (!TextHelper.isEmail(newMailAddress)) {
            newMail.error = "邮箱格式不正确"
            return
        }
        if (UserInfo.getEmail() == newMailAddress) {
            newMail.error = "不能和原邮箱一致"
            return
        }

        pass = SecureHelper.MD5(pass)
        //包装信息
        val info = UserInfo().apply {
            setUserId(UserInfo.getUserId())
            userPass = pass
            setEmail(newMailAddress)
        }
        val p = ProgressDialog(context)
        GlobalScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                AppApi.modifyMail(info)
            }.onSuccess { b ->
                withContext(Dispatchers.Main) {
                    if (b.isOk()) {
                        GlobalApp.toastSuccess(R.string.text_modify_succ)
                        val newInfo = UserInfo.INSTANCE.apply {
                            setEmail(newMailAddress)
                        }
                        AppLogic.onLogin(newInfo)
                        onUpdate.invoke()
                        dialog.dismiss()
                    } else {
                        GlobalApp.toastError(b.message)
                    }
                }
            }.onFailure { e ->
                e.log()
                GlobalApp.toastError(e.message
                    ?: context.getString(R.string.text_modify_failed))
            }
            withContext(Dispatchers.Main) {
                p.dismiss()
            }
        }

    }

}