package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.jarvis.utils.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.netacc.tool.SecureHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.custom.CountDownButton
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.gson.reflect.TypeToken

/**
 * # SignupDialog
 *
 * @author Administrator
 * 2018/9/14
 */

class SignupDialog(context: Context, val r: OnLoginSuccess) : View.OnClickListener {
    val dialog: MaterialDialog = MaterialDialog(context)
    val lastBit = 6

    private var userEmailView: TextInputLayout
    private var userPassView: TextInputLayout
    private var confirmPassView: TextInputLayout
    private var verCodeView: TextInputLayout
    private var signUpBtn: Button
    private var loadBar: ProgressBar
    private val toast = ColorfulToast(context)
    val countDownSecs = 60

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_sign_up, null)
        userEmailView = view.findViewById(R.id.user_email_view)
        userPassView = view.findViewById(R.id.user_pass_view)
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

                NetHelper.postJson<String>(ApiUrls.SEND_EMAIL_VER_CODE, BaseRequestModel(userEmail),
                        type = object : TypeToken<ResponseMessage<String>>() {}.type, callback = { _, bean ->
                    loadBar.visibility = View.INVISIBLE
                    if (bean != null) {
                        if (bean.isOk()) {
                            toast.showShort(bean.data ?: "null")
                            startDown(5)
                        } else {
                            this.isEnabled = true
                            toast.showShort(bean.message ?: "null")
                        }
                    } else {
                        this.isEnabled = true
                        toast.showShort("出错")
                    }
                })
            }
        }

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
                userInfo.setUserName(userEmail)

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
            //post
            NetHelper.postJson<String>(ApiUrls.REGISTER_BY_EMAIL, BaseRequestModel(userInfo, verCode), type = object
                : TypeToken<ResponseMessage<String>>() {}.type, callback = { _, bean ->
                //泛型
                Vog.d(this, "onResponse ---> $bean")
                loadBar.visibility = View.INVISIBLE
                if (bean != null) {
                    if (bean.isOk()) {
                        toast.showShort(bean.data ?: "null")
                        LoginDialog(context, userEmail, userPass, r)
                        dialog.dismiss()
                    } else {
                        toast.showShort(bean.message ?: "null")
                    }
                } else {
                    toast.showShort("出错")
                }
            })
        }
        dialog.customView(view = view, scrollable = true).title(R.string.text_sign_up).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}