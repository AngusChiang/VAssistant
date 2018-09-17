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
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.netacc.tool.SignHelper
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.utils.TextHelper
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.gson.reflect.TypeToken

/**
 * # LoginDialog
 *
 * @author 17719247306
 * 2018/9/12
 */
typealias OnLoginSuccess = () -> Unit

class LoginDialog(val context: Context, initEmail: String? = null,
                  initPas: String? = null, val r: OnLoginSuccess) : View.OnClickListener {
    val dialog: MaterialDialog = MaterialDialog(context)

    private var userAccountView: TextInputLayout
    private var userPassView: TextInputLayout
    private var loginBtn: Button
    private var loadBar: ProgressBar
    private val toast = ColorfulToast(context)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_login, null)
        userAccountView = view.findViewById(R.id.user_account_view)
        userPassView = view.findViewById(R.id.user_pass_view)
        if (initEmail != null)
            userAccountView.editText?.setText(initEmail)
        if (initPas != null)
            userPassView.editText?.setText(initPas)

        loginBtn = view.findViewById(R.id.dialog_login_btn)
        loadBar = view.findViewById(R.id.loading_bar)

        view.findViewById<View>(R.id.sign_up_view).setOnClickListener(this)
        view.findViewById<View>(R.id.retrieve_pass_view).setOnClickListener(this)

        loginBtn.setOnClickListener {
            userAccountView.error = ""
            userPassView.error = ""
//            val params = TreeMap<String, String>()
            val userInfo = UserInfo()
            val loginId = userAccountView.editText?.text.toString()
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
                userInfo.email = loginId
            } else userInfo.userName = loginId

            userInfo.userPass = SignHelper.MD5(userPass)

            loadBar.visibility = View.VISIBLE
            //post
            NetHelper.postJson<UserInfo>(ApiUrls.LOGIN, BaseRequestModel(userInfo), type = object
                : TypeToken<ResponseMessage<UserInfo>>() {}.type, callback = { _, bean ->
                //泛型
                Vog.d(this, "onResponse ---> $bean")
                loadBar.visibility = View.INVISIBLE
                if (bean != null) {
                    if (bean.isOk()) {
                        try {
                            bean.data!!.success()
                        } catch (e: Exception) {
                            toast.showShort("code: ld82")
                            return@postJson
                        }
                        toast.showShort("登录成功")
                        r.invoke()
                        dialog.dismiss()
                    } else {
                        toast.showShort(bean.message)
                    }
                } else {
                    toast.showShort("出错")
                }
            })
        }
        dialog.customView(view = view).title(R.string.text_login).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_up_view -> {
                SignupDialog(context, r)
                dialog.dismiss()
            }
            R.id.retrieve_pass_view -> {

            }
            else -> {

            }
        }
    }
}