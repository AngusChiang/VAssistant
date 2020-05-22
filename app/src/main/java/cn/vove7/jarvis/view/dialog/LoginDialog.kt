package cn.vove7.jarvis.view.dialog

import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.inVisibility
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppLogic
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.dialog_login.view.*

/**
 * # LoginDialog
 *
 * @author 17719247306
 * 2018/9/12
 */
typealias OnLoginSuccess = () -> Unit

class LoginDialog(val context: Context, initEmail: String? = null,
                  initPas: String? = null, val r: OnLoginSuccess) {
    val dialog: MaterialDialog = MaterialDialog(context).positiveButton(R.string.text_sign_up) {
        SignupDialog(context, r)
    }.neutralButton(R.string.text_retrieve_password) {
        RetrievePasswordDialog(context)
    }

    private val userAccountView: TextInputLayout
            by lazy { view.user_account_view }
    private val userPassView: TextInputLayout
            by lazy { view.user_pass_view }
    private val loginBtn: Button
            by lazy { view.dialog_login_btn }
    private val loadBar: ProgressBar
            by lazy { view.loading_bar }

    private val view: View by lazy { LayoutInflater.from(context).inflate(R.layout.dialog_login, null) }

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
            //post
            WrapperNetHelper.postJson<UserInfo>(ApiUrls.LOGIN, loginInfo) {
                success { _, bean ->
                    if (bean.isOk()) {
                        try {
                            val userInfo = bean.data!!
                            AppLogic.onLogin(userInfo)
                        } catch (e: Exception) {
                            GlobalApp.toastInfo(R.string.text_error_occurred)
                            GlobalLog.err(e.message)
                            return@success
                        }
                        GlobalApp.toastSuccess("登录成功")
                        r.invoke()
                        dialog.dismiss()
                    } else {
                        GlobalApp.toastInfo(bean.message)
                    }
                }
                fail { _, e ->
                    e.log()
                    GlobalApp.toastError("出错${e.message}")
                }
                end {
                    loadBar.inVisibility()
                }
            }
        }
        dialog.customView(view = view, scrollable = true)
                .title(R.string.text_login).show()
    }

}