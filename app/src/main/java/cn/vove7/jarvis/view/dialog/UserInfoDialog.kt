package cn.vove7.jarvis.view.dialog

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VipPrice
import cn.vove7.jarvis.net.ApiUrls
import cn.vove7.jarvis.net.WrapperNetHelper
import cn.vove7.jarvis.R
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.tools.openQQChat
import cn.vove7.jarvis.tools.pay.PurchaseHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import java.text.SimpleDateFormat
import java.util.*


/**
 * # UserInfoDialog
 *
 * @author Administrator
 * 2018/9/19
 */
class UserInfoDialog(val context: Activity, val onUpdate: () -> Unit) {
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_user_info, null)

    private val userEmailText: TextView = view.findViewById(R.id.user_email_text)
    private val userNameText: TextView = view.findViewById(R.id.user_name_text)
    private val redHeardText: TextView = view.findViewById(R.id.red_heard)
    private val vipEndDateText: TextView = view.findViewById(R.id.vip_end_date)
    private val regDateText: TextView = view.findViewById(R.id.reg_time)

    val dialog = MaterialDialog(context)
            .positiveButton(R.string.text_modify_pass) {
                ModifyUserPassDialog(context)
            }
            .neutralButton(R.string.text_recharge) { recharge() }
            .negativeButton(R.string.text_logout) {
                exit = true
                AppConfig.logout()
                onUpdate.invoke()
            }.customView(view = view)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        setData()
        loadInfo()
        userEmailText.setOnClickListener {
            //修改邮箱
            ModifyUserMailDialog(context, onUpdate)
            dialog.dismiss()
        }
        //修改昵称
        dialog.findViewById<View>(R.id.name_lay).setOnClickListener {
            MaterialDialog(context).title(R.string.text_modify_user_name)
                    .noAutoDismiss()
                    .input(prefill = UserInfo.getUserName()) { materialDialog, s ->
                        val p = ProgressDialog(context)
                        WrapperNetHelper.postJson<String>(ApiUrls.MODIFY_NAME, s.toString()) {
                            success { _, b ->
                                p.dismiss()
                                if (b.isOk()) {
                                    UserInfo.INSTANCE.setUserName(s.toString())
                                    GlobalApp.toastSuccess(R.string.text_modify_succ)
                                    Handler().postDelayed({
                                        onUpdate.invoke()
                                    }, 1000)
                                    materialDialog.dismiss()
                                } else {
                                    GlobalApp.toastError(b.message)
                                }
                            }
                            fail { _, e ->
                                p.dismiss()
                                GlobalApp.toastError(e.message
                                    ?: GlobalApp.getString(R.string.text_modify_failed))

                            }
                        }
                    }.show()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun recharge() {
        val pd = ProgressDialog(context)
        Handler().postDelayed({
            WrapperNetHelper.postJson<List<VipPrice>>(ApiUrls.GET_PRICES) {
                success { _, bean ->
                    pd.dismiss()
                    if (bean.isOk()) {
                        showReChargeDialog(bean.data!!)
                        dialog.dismiss()
                    } else GlobalApp.toastError(R.string.text_failed_to_get_data)
                }
                fail { _, e ->
                    pd.dismiss()
                    GlobalApp.toastError(R.string.text_failed_to_get_data)
                }
            }

        }, 500)
    }

    var exit = false
    /**
     * 获取数据
     */
    private fun loadInfo() {
        WrapperNetHelper.postJson<UserInfo>(ApiUrls.GET_USER_INFO) {
            success { _, bean ->
                if (exit) return@success
                if (bean.isOk()) {
                    try {
                        val userInfo = bean.data!!
                        AppConfig.login(userInfo)
                        onUpdate.invoke()
                        setData()
                    } catch (e: Exception) {
                        GlobalApp.toastError(R.string.text_error_occurred)
                        GlobalLog.err(e)
                        return@success
                    }
                }
            }
            fail { _, e ->
                e.log()
                if (exit) return@fail
                GlobalApp.toastError(R.string.text_failed_to_get_user_info)
            }
        }
    }

    private fun setData() {
        regDateText.text = dateFormat.format(UserInfo.getRegTime())
        userNameText.text = UserInfo.getUserName()
        userEmailText.text = UserInfo.getEmail()
        redHeardText.visibility = if (UserInfo.isVip()) View.VISIBLE else View.GONE

        vipEndDateText.text = UserInfo.getVipEndDate().let {
            if (it != null) dateFormat.format(it)
            else ""
        }
    }

    /**
     * 购买充值码dialog
     * @param ps List<VipPrice>
     */
    private fun showReChargeDialog(ps: List<VipPrice>) {
        val bu = StringBuilder()

        ps.withIndex().forEach {
            bu.append("${it.value.durationText} \t (${it.value.price}元)")
            if (it.index != ps.size - 1) bu.appendln()
        }
        val cView = View.inflate(context, R.layout.dialog_recharge, null)
        cView.findViewById<TextView>(R.id.prices_text).text = bu.toString()
//        cView.findViewById<Button>(R.id.btn_guide).setOnClickListener {
//            SystemHelper.openLink(context, ApiUrls.USER_GUIDE)
//        }
        MaterialDialog(context).title(R.string.text_purchase_recharge_code)
                .customView(view = cView, scrollable = true)
                .positiveButton(text = "支付宝") {
                    val cs = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cs.primaryClip = ClipData.newPlainText("", UserInfo.getEmail())
                    GlobalApp.toastInfo(R.string.text_email_copy_to_clip, Toast.LENGTH_LONG)

                    PurchaseHelper.openAliPay(context) { b, m ->
                        if (!b) GlobalApp.toastInfo(m)
                    }
                }.negativeButton(text = "联系QQ") {
                    openQQChat("529545532")
                }.neutralButton(text = "使用充值码") {
                    showActCodeDialog()
                }.show()
    }

    private var pd: ProgressDialog? = null
    private fun showActCodeDialog() {
        val clipText = SystemBridge.getClipText()

        MaterialDialog(context).title(text = "使用充值码")
                .input(prefill = if (clipText?.matches("[a-z0-9A-Z]+".toRegex()) == true) clipText else "",
                        waitForPositiveButton = true) { _, charSequence ->
                    useCode(charSequence.toString())
                }.positiveButton { d ->
                    d.dismiss()
                }.negativeButton {
                    it.dismiss()
                }.noAutoDismiss()
                .show()
    }

    private fun useCode(s: String?) {
        pd = ProgressDialog(context)
        WrapperNetHelper.postJson<String>(ApiUrls.ACTIVATE_VIP, arg1 = s) {
            success { _, bean ->
                pd?.dismiss()
                if (bean.isOk()) {
                    GlobalApp.toastSuccess("${bean.data}", Toast.LENGTH_LONG)
                } else {
                    GlobalApp.toastInfo(bean.message)
                }
            }
            fail { _, e ->
                pd?.dismiss()
                e.log()
                GlobalApp.toastError(R.string.text_net_err)
            }
        }
    }

}