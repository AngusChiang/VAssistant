package cn.vove7.jarvis.tools.pay

import android.app.Activity
import android.didikee.donate.AlipayDonate
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import cn.vove7.vtp.log.Vog
//import com.alipay.sdk.app.PayTask


/**
 * # PurchaseHelper
 *
 * @author Administrator
 * 9/20/2018
 */
object PurchaseHelper {

    private val payCode = "FKX07237LYKEFIVIY8MSE9"

    fun openAliPay(activity: Activity, onResult: (Boolean, String) -> Unit) {
        val hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(activity)
        if (hasInstalledAlipayClient) {
            if (AlipayDonate.startAlipayClient(activity, payCode)) {
                onResult.invoke(true, "正在打开支付宝")
            } else {
                onResult.invoke(false, "打开支付宝失败")
            }
        } else {
            onResult.invoke(false, "未安装支付宝")
        }
    }

    const val FLAG_ALIPAY = 0
    const val FLAG_WECHAT = 1

    class PayResultHandler(val onResult: (Boolean, String) -> Unit) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                FLAG_ALIPAY -> {
                    val payResult = PayResult(msg.obj as Map<String, String>)
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.result// 同步返回需要验证的信息
                    Vog.d(this, "resultInfo ---> $resultInfo")
                    val resultStatus = payResult.resultStatus
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        onResult.invoke(true, "支付成功")
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        onResult.invoke(false, "支付失败")
                    }
                }
                FLAG_WECHAT -> {
                }
            }
        }
    }

//    fun withAlipay(activity: Activity, price: Double, onResult: (Boolean, String) -> Unit) {
//        //getOrder from server
//        NetHelper.postJson<String>(ApiUrls.GET_ALI_ORDER, BaseRequestModel(price),
//                type = NetHelper.StringType) { _, bean ->
//            if (bean?.isOk() == true) {
//                val order = bean.data
//                if (order != null) {//get order complete
//                    openAliPay(activity, order, PayResultHandler(onResult))
//                } else {
//                    onResult.invoke(false, "获取支付信息失败")
//                }
//            } else {
//                onResult.invoke(false, "网络错误")
//            }
//
//        }
//    }

//    private fun openAliPay(activity: Activity, order: String, mHandler: Handler) {
//        val payRunnable = Runnable {
//            val alipay = PayTask(activity)
//            val result = alipay.payV2(order, true)
//            Log.i("msp", result.toString())
//
//            val msg = Message()
//            msg.what = FLAG_ALIPAY
//            msg.obj = result
//            mHandler.sendMessage(msg)
//        }
//
//        val payThread = Thread(payRunnable)
//        payThread.start()
//    }
}