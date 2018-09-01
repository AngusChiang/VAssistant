package cn.vove7.common.app

import android.app.Application
import android.os.Handler
import android.os.Message
import android.support.annotation.StringRes
import cn.vove7.vtp.builder.BundleBuilder
import cn.vove7.vtp.toast.Voast

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
    }


    companion object {
        val toastHandler = ToastHandler()
        lateinit var APP: Application
        fun getString(@StringRes id: Int): String = APP.getString(id)

        fun toastShort(msg: String) {
            val m = Message()
            m.data=BundleBuilder().put("data",msg).data
            toastHandler.sendMessage(m)
        }
    }

    class ToastHandler : Handler() {
        override fun handleMessage(msg: Message?) {
            val str = msg?.data?.getString("data") ?: ""
            Voast.with(GlobalApp.APP).top().showShort(str)
        }
    }

}