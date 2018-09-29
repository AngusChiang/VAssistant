package cn.vove7.jarvis.view.dialog

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VipPrice
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.utils.NetHelper
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.jarvis.R
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.pay.PurchaseHelper
import cn.vove7.vtp.system.SystemHelper
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

    val toast = ColorfulToast(context).yellow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        setData()
        loadInfo()
        //修改昵称
        dialog.findViewById<View>(R.id.name_lay).setOnClickListener {
            MaterialDialog(context).title(R.string.text_modify_user_name)
                    .noAutoDismiss()
                    .input(prefill = UserInfo.getUserName()) { materialDialog, s ->
                        val p = ProgressDialog(context)
                        NetHelper.postJson<String>(ApiUrls.MODIFY_NAME, BaseRequestModel(s.toString())) { _, b ->
                            p.dismiss()
                            if (b?.isOk() == true) {
                                UserInfo.INSTANCE.setUserName(s.toString())
                                toast.showShort(R.string.text_modify_succ)
                                Handler().postDelayed({
                                    onUpdate.invoke()
                                },1000)
                                materialDialog.dismiss()
                            } else {
                                GlobalLog.err(b?.message)
                                toast.showShort(R.string.text_modify_failed)
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
            NetHelper.get<List<VipPrice>>(ApiUrls.GET_PRICES,
                    type = NetHelper.VipPriceListType) { _, bean ->
                pd.dismiss()
                if (bean != null) {
                    if (bean.isOk()) {
                        showReChargeDialog(bean.data!!)
                        dialog.dismiss()
                    } else toast.showShort(R.string.text_failed_to_get_data)
                } else toast.showShort(R.string.text_failed_to_get_data)
            }
        }, 500)
    }

    var exit = false
    /**
     * 获取数据
     */
    private fun loadInfo() {
        NetHelper.postJson<UserInfo>(ApiUrls.GET_USER_INFO, BaseRequestModel<String>(),
                type = NetHelper.UserInfoType) { _, bean ->
            if (exit) return@postJson
            if (bean != null) {
                if (bean.isOk()) {
                    try {
                        val userInfo = bean.data!!
                        AppConfig.login(userInfo)
                        onUpdate.invoke()
                        setData()
                    } catch (e: Exception) {
                        toast.showShort(R.string.text_error_occurred)
                        GlobalLog.err(e.message + "--code: ui52")
                        return@postJson
                    }
                } /*else toast.showShort(bean.message)*/
            } else toast.showShort(R.string.text_failed_to_get_user_info)
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

        ps.forEach {
            bu.append("${it.durationText} \t (${it.price}元)\n")
        }
        val cView = View.inflate(context, R.layout.dialog_recharge, null)
        cView.findViewById<TextView>(R.id.prices_text).text = bu.toString()
        cView.findViewById<Button>(R.id.btn_guide).setOnClickListener {
            SystemHelper.openLink(context, ApiUrls.USER_GUIDE)
        }
        MaterialDialog(context).title(R.string.text_purchase_recharge_code)
                .customView(view = cView, scrollable = true)
                .positiveButton(text = "支付宝") {
                    val cs = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cs.primaryClip = ClipData.newPlainText("", UserInfo.getEmail())
                    toast.showLong(R.string.text_email_copy_to_clip)

                    PurchaseHelper.openAliPay(context) { b, m ->
                        if (!b) toast.showShort(m)
                    }
                }.negativeButton(text = "联系QQ") {
                    val qqNum = "529545532"
                    val qqIntent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=$qqNum&version=1"))
                    qqIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(qqIntent)
                    } catch (e: Exception) {
                        toast.showShort("唤起QQ失败")
                    }
                }.neutralButton(text = "使用充值码") {
                    showActCodeDialog()
                }.show()
    }

//    @Deprecated("unuse")
//    private fun showPurchaseDialog(ps: List<VipPrice>) {
//        val items = mutableListOf<String>()
//        ps.forEach {
//            items.add("${it.durationText} (${it.price})")
//        }
//        var pi = 0
//        MaterialDialog(context).title(text = "充值会员")
//                .listItemsSingleChoice(items = items, initialSelection = 0) { _, i, s ->
//                    pi = i
//                }.positiveButton(R.string.text_alipay_pay) {
//                    PurchaseHelper.withAlipay(context, ps[pi].price) { succ, result ->
//                        if (succ) {
//                            loadInfo()
//                        }
//                        toast.showShort(result)
//                    }
//                }.negativeButton(R.string.text_wechat_pay) {
//                    //todo
//                }.neutralButton(R.string.text_recharge_code) {
//                    showActCodeDialog()
//                }
//                .show()
//    }

    var pd: ProgressDialog? = null
    private fun showActCodeDialog() {
        var s = ""
        MaterialDialog(context).title(text = "使用充值码")
                .input { materialDialog, charSequence ->
                    s = charSequence.toString()
                }.positiveButton { d ->
                    if (s == "") return@positiveButton
                    pd = ProgressDialog(context)
                    d.dismiss()
                    NetHelper.postJson<String>(ApiUrls.ACTIVATE_VIP, BaseRequestModel(null, s)) { _, bean ->
                        pd?.dismiss()
                        if (bean != null) {
                            if (bean.isOk()) {
                                toast.showShort("${bean.data}")
                            } else {
                                toast.showShort(bean.message)
                            }
                        } else {
                            toast.showShort(R.string.text_net_err)
                        }
                    }
                }.negativeButton {
                    it.dismiss()
                }.noAutoDismiss().show()
    }

}