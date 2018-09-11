package cn.vove7.common.app

import android.app.Application
import android.support.annotation.StringRes
import cn.vove7.common.view.toast.ColorfulToast

/**
 * # GlobalApp
 *
 * @author 17719
 * 2018/8/8
 */

open class GlobalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        APP = this
        colorfulToast = ColorfulToast(this).blue()
//        toastHandler = ToastHandler(colorfulToast)
    }

    companion object {
        //        var toastHandler: ColorfulToast.ToastHandler? = null
        lateinit var APP: Application

        fun getString(@StringRes id: Int): String = APP.getString(id)

        fun toastShort(msg: String) {
            (APP as GlobalApp).toastShort(msg)
        }

        fun toastShort(rId: Int) {
            toastShort(getString(rId))
        }
    }

    private fun toastShort(msg: String) {
        colorfulToast.showShort(msg)
    }

    lateinit var colorfulToast: ColorfulToast

//    class ToastHandler(val colorfulToast: ColorfulToast) : Handler() {
//        override fun handleMessage(msg: Message?) {
//            val str = msg?.data?.getString("data") ?: ""
//            colorfulToast.showShort(str)
//        }
//    }

}