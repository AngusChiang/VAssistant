package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.inVisibility
import cn.vove7.jarvis.R
import cn.vove7.jarvis.app.AppApi
import cn.vove7.jarvis.databinding.DialogLoginBinding
import cn.vove7.jarvis.tools.AppLogic
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * # LoginDialog
 *
 * @author Vove
 * 2018/9/12
 */
typealias OnLoginSuccess = () -> Unit

class LoginDialog(val context: Context, initEmail: String? = null,
                  initPas: String? = null, val r: OnLoginSuccess) {
    @Suppress("DEPRECATION")
    val dialog: MaterialDialog = MaterialDialog(context).positiveButton(R.string.text_sign_up) {
        SignUpDialog(context, r)
    }.neutralButton(R.string.text_retrieve_password) {
        RetrievePasswordDialog(context)
    }

    private inline val userAccountView get() = viewBinding.userAccountView
    private inline val userPassView get() = viewBinding.userPassView
    private inline val loginBtn get() = viewBinding.dialogLoginBtn
    private inline val loadBar get() = viewBinding.loadingBar

    private val viewBinding by lazy {
        DialogLoginBinding.inflate(LayoutInflater.from(context))
    }

    init {

        if (initEmail != null)
            userAccountView.editText?.setText(initEmail)
        if (initPas != null)
            userPassView.editText?.setText(initPas)

        loginBtn.setOnClickListener {
            userAccountView.error = ""
            userPassView.error = ""
            val loginInfo = UserInfo()
            val loginId = userAccountView.editText?.text?.toString()?.trim()
            val userPass = userPassView.editText?.text.toString()
            if (TextUtils.isEmpty(loginId)) {
                userAccountView.error = GlobalApp.getString(R.string.text_not_empty)
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(userPass)) {
                userPassView.error = GlobalApp.getString(R.string.text_not_empty)
                return@setOnClickListener
            }

            if (TextHelper.isEmail(loginId)) {
                loginInfo.setEmail(loginId)
            } else loginInfo.setUserName(loginId)

            loginInfo.userPass = SecureHelper.MD5(userPass)

            loadBar.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    AppApi.login(loginInfo)
                }.onSuccess { bean ->
                    withContext(Dispatchers.Main) {
                        if (bean.isOk()) {
                            try {
                                val userInfo = bean.data!!
                                AppLogic.onLogin(userInfo)
                            } catch (e: Exception) {
                                GlobalApp.toastInfo(R.string.text_error_occurred)
                                GlobalLog.err(e.message)
                                return@withContext
                            }
                            GlobalApp.toastSuccess("登录成功")
                            r.invoke()
                            dialog.dismiss()
                        } else {
                            GlobalApp.toastInfo(bean.message)
                        }
                    }
                }.onFailure { e ->
                    e.log()
                    GlobalApp.toastError("出错${e.message}")
                }
                withContext(Dispatchers.Main) {
                    loadBar.inVisibility()
                }
            }
        }
        dialog.customView(view = viewBinding.root, scrollable = true)
                .title(R.string.text_login).show()
    }

}