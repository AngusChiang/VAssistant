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
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.WrapperNetHelper
import cn.vove7.common.netacc.tool.SecureHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.gone
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.dialog_sign_up.view.*

/**
 * # SignupDialog
 *
 * @author Administrator
 * 2018/9/14
 */

class SignupDialog(context: Context, val r: OnLoginSuccess) {
    val dialog: MaterialDialog = MaterialDialog(context)
    private val lastBit = 6

    private val userEmailView
            by lazy { view.user_email_view }
    private val userPassView
            by lazy { view.user_pass_view }
    private val confirmPassView
            by lazy { view.confirm_pass_view }
    private val verCodeView
            by lazy { view.ver_code_view }
    private val signUpBtn: Button by lazy { view.dialog_signup_btn }
    private val loadBar: ProgressBar by lazy { view.loading_bar }
    //    val countDownSecs = 30
    val view: View by lazy { LayoutInflater.from(context).inflate(R.layout.dialog_sign_up, null) }

    init {
        signUpBtn.setOnClickListener {
            userEmailView.error = ""
            userPassView.error = ""
            confirmPassView.error = ""
            verCodeView.error = ""
            val userInfo = UserInfo()
            val userEmail = userEmailView.editText?.text.toString()
            val userPass = userPassView.editText?.text.toString()
            val confirmPass = confirmPassView.editText?.text.toString()

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

            loadBar.visibility = View.VISIBLE
            //post
            WrapperNetHelper.postJson<String>(ApiUrls.REGISTER_BY_EMAIL, model = userInfo) {
                success { _, bean ->
                    //泛型
                    Vog.d("onResponse ---> $bean")
                    loadBar.visibility = View.INVISIBLE
                    if (bean.isOk()) {
                        GlobalApp.toastInfo(bean.data ?: "null")
                        LoginDialog(context, userEmail, userPass, r)
                        dialog.dismiss()
                    } else {
                        GlobalApp.toastInfo(bean.message)
                    }
                }
                fail { _, e ->
                    loadBar.visibility = View.INVISIBLE
                    e.log()
                    GlobalApp.toastError("出错")
                }
            }
        }
        dialog.customView(view = view, scrollable = true).title(R.string.text_sign_up).show()
        dialog.findViewById<View>(R.id.get_ver_layout).gone()
    }

}