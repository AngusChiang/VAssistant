package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.onFailureMain
import cn.vove7.common.utils.onSuccessMain
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.app.AppApi
import cn.vove7.jarvis.view.custom.CountDownButton
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * # RetrievePasswordDialog
 *
 * @author Administrator
 * 2018/10/5
 */
class RetrievePasswordDialog(context: Context) {
    val dialog: MaterialDialog = MaterialDialog(context)
    val lastBit = 6

    private var userEmailView: TextInputLayout
    private var userPassView: TextInputLayout
    private var confirmPassView: TextInputLayout
    private var verCodeView: TextInputLayout
    private var signUpBtn: Button
    private var loadBar: ProgressBar
    private val countDownSecs = 30

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_sign_up, null)
        userEmailView = view.findViewById(R.id.user_email_view)
        userEmailView.hint = "找回邮箱"
        userEmailView.helperText = ""
        userPassView = view.findViewById(R.id.user_pass_view)
        userPassView.hint = "新密码"
        confirmPassView = view.findViewById(R.id.confirm_pass_view)
        verCodeView = view.findViewById(R.id.ver_code_view)

        signUpBtn = view.findViewById(R.id.dialog_signup_btn)
        loadBar = view.findViewById(R.id.loading_bar)

        view.findViewById<CountDownButton>(R.id.get_ver_btn).apply {
            setCallBack(RecOnFinish)
            setOnClickListener {
                val userEmail = userEmailView.editText?.text.toString()
                if (BuildConfig.DEBUG && userEmail == "") {
                    startDown(5)
                    return@setOnClickListener
                }
                if (!TextHelper.isEmail(userEmail)) {//邮箱
                    userEmailView.error = GlobalApp.getString(R.string.text_email_format_err)
                    return@setOnClickListener
                }
                this.isEnabled = false
                loadBar.visibility = View.VISIBLE
//                val p = NetParamsBuilder.of(Pair("emailAdd", userEmail)).sign()

                GlobalScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        AppApi.sendResetPassCode(userEmail, "check")
                    }.onSuccess { bean ->
                        withContext(Dispatchers.Main) {
                            loadBar.visibility = View.INVISIBLE
                            if (bean.isOk()) {
                                GlobalApp.toastSuccess(bean.data ?: "null")
                                startDown(countDownSecs)
                            } else {
                                this@apply.isEnabled = true
                                GlobalApp.toastError(bean.message)
                            }
                        }
                    }.onFailure { e ->
                        e.log()
                        withContext(Dispatchers.Main) {
                            this@apply.isEnabled = true
                            GlobalApp.toastError("出错 ${e.message}")
                        }
                    }
                }
            }
        }
        signUpBtn.text = "找回"
        signUpBtn.setOnClickListener {
            userEmailView.error = ""
            userPassView.error = ""
            confirmPassView.error = ""
            verCodeView.error = ""
//            val params = TreeMap<String, String>()
            val userInfo = UserInfo()
            val userEmail = userEmailView.editText?.text.toString()
            val userPass = userPassView.editText?.text.toString()
            val confirmPass = confirmPassView.editText?.text.toString()
            val verCode = verCodeView.editText?.text.toString()

            if (TextUtils.isEmpty(userEmail)) {
                userEmailView.error = GlobalApp.getString(R.string.text_not_empty)
                return@setOnClickListener
            }
            if (TextHelper.isEmail(userEmail)) {//邮箱
                userInfo.setEmail(userEmail)
            } else {
                userEmailView.error = GlobalApp.getString(R.string.text_email_format_err)
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(userPass)) {//密码
                userPassView.error = GlobalApp.getString(R.string.text_not_empty)
                return@setOnClickListener
            }
            if (userPass.length < lastBit) {//密码
                userPassView.error = "密码长度至少${lastBit}位"
                return@setOnClickListener
            }
            if (confirmPass != userPass) {//密码
                confirmPassView.error = GlobalApp.getString(R.string.text_pass_not_same)
                return@setOnClickListener
            }
            userInfo.userPass = SecureHelper.MD5(userPass)
            if (TextUtils.isEmpty(verCode)) {//验证码
                verCodeView.error = GlobalApp.getString(R.string.text_not_empty)
                return@setOnClickListener
            }

            loadBar.visibility = View.VISIBLE

            GlobalScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    AppApi.resetPassByEmailCode(userInfo, verCode)
                }.apply {
                    onSuccessMain { bean ->
                        //泛型
                        Vog.d("onResponse ---> $bean")
                        loadBar.visibility = View.INVISIBLE
                        if (bean.isOk()) {
                            GlobalApp.toastInfo(bean.data ?: "网络错误")
                            dialog.dismiss()
                        } else {
                            GlobalApp.toastError(bean.message)
                        }
                    }
                    onFailureMain { e ->
                        loadBar.visibility = View.INVISIBLE
                        e.log()
                        GlobalApp.toastError("出错 ${e.message}")
                    }
                }
            }
        }
        dialog.customView(view = view, scrollable = true)
                .title(R.string.text_retrieve_password).show()
    }
}