package cn.vove7.jarvis.view.dialog

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VipPrice
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.utils.onFailureMain
import cn.vove7.common.utils.onSuccessMain
import cn.vove7.jarvis.R
import cn.vove7.jarvis.app.AppApi
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.tools.openQQChat
import cn.vove7.jarvis.tools.pay.PurchaseHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            .neutralButton(R.string.text_recharge) {
                it.dismiss()
                recharge(context)
            }
            .negativeButton(R.string.text_logout) {
                exit = true
                AppLogic.onLogout()
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
                        GlobalScope.launch(Dispatchers.IO) {
                            kotlin.runCatching {
                                AppApi.modifyName(s.toString())
                            }.apply {
                                onSuccessMain { b ->
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
                                onFailureMain { e ->
                                    p.dismiss()
                                    GlobalApp.toastError(e.message
                                        ?: GlobalApp.getString(R.string.text_modify_failed))
                                }
                            }
                        }
                    }.show()
            dialog.dismiss()
        }
        dialog.show()
    }

    companion object {

        fun recharge(context: Activity) {
            val pd = ProgressDialog(context)
            Handler().postDelayed({
                GlobalScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        AppApi.getVipPrices()
                    }.apply {
                        onSuccessMain { bean ->
                            pd.dismiss()
                            if (bean.isOk()) {
                                showReChargeDialog(context, bean.data!!)
                            } else GlobalApp.toastError(R.string.text_failed_to_get_data)
                        }
                        onFailureMain { e ->
                            pd.dismiss()
                            GlobalApp.toastError(R.string.text_failed_to_get_data)
                        }
                    }
                }
            }, 500)
        }

        /**
         * 购买充值码dialog
         * @param ps List<VipPrice>
         */
        private fun showReChargeDialog(context: Activity, ps: List<VipPrice>) {
            val bu = StringBuilder()

            ps.withIndex().forEach {
                bu.append("${it.value.durationText} \t (${it.value.price}元)")
                if (it.index != ps.size - 1) bu.appendln()
            }
            val cView = View.inflate(context, R.layout.dialog_recharge, null)
            cView.findViewById<TextView>(R.id.prices_text).text = bu.toString()

            MaterialDialog(context).title(R.string.text_purchase_recharge_code)
                    .customView(view = cView, scrollable = true)
                    .positiveButton(text = "支付宝") {
                        PurchaseHelper.openAliPay(context) { b, m ->
                            if (!b) GlobalApp.toastInfo(m)
                        }
                    }.negativeButton(text = "联系QQ") {
                        openQQChat("529545532")
                    }.neutralButton(text = "使用充值码") {
                        showActCodeDialog(context)
                    }.show()
        }

        private fun showActCodeDialog(context: Context) {
            val clipText = SystemBridge.getClipText()

            MaterialDialog(context).title(text = "使用充值码")
                    .input(prefill = if (clipText?.matches("[a-z0-9A-Z]+".toRegex()) == true) clipText else "",
                            waitForPositiveButton = true) { _, charSequence ->
                        useCode(context, charSequence.toString())
                    }.positiveButton { d ->
                        d.dismiss()
                    }.negativeButton {
                        it.dismiss()
                    }.noAutoDismiss()
                    .show()
        }

        private fun useCode(context: Context, s: String) {
            val pd = ProgressDialog(context)
            GlobalScope.launch(Dispatchers.IO) {
                kotlin.runCatching { AppApi.activateVip(s) }.apply {
                    onSuccessMain { bean ->
                        pd.dismiss()
                        if (bean.isOk()) {
                            AppBus.post(AppBus.EVENT_REFRESH_USER_INFO)
                            GlobalApp.toastSuccess("${bean.data}", Toast.LENGTH_LONG)
                        } else {
                            GlobalApp.toastInfo(bean.message)
                        }
                    }
                    onFailureMain { e ->
                        pd.dismiss()
                        e.log()
                        GlobalApp.toastError(R.string.text_net_err)
                    }
                }
            }
        }
    }

    var exit = false

    /**
     * 获取数据
     */
    private fun loadInfo() = GlobalScope.launch(Dispatchers.IO) {
        kotlin.runCatching {
            AppApi.getUserInfo()
        }.apply {
            onSuccessMain { bean ->
                if (exit) return@onSuccessMain
                if (bean.isOk(true)) {
                    try {
                        val userInfo = bean.data!!
                        AppLogic.onLogin(userInfo)
                        onUpdate.invoke()
                        setData()
                    } catch (e: Exception) {
                        GlobalApp.toastError(R.string.text_error_occurred)
                        GlobalLog.err(e)
                    }
                }
            }
            onFailure { e ->
                e.log()
                if (exit) return@onFailure
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


}