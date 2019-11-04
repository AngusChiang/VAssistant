package cn.vove7.common.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.support.multidex.MultiDex
import android.util.Log
import android.widget.Toast
import cn.vove7.common.BuildConfig
import cn.vove7.common.bridges.ServiceBridge
import cn.vove7.common.helper.ToastyHelper
import cn.vove7.common.helper.ToastyHelper.TYPE_ERROR
import cn.vove7.common.helper.ToastyHelper.TYPE_INFO
import cn.vove7.common.helper.ToastyHelper.TYPE_SUCCESS
import cn.vove7.common.helper.ToastyHelper.TYPE_WARNING
import cn.vove7.common.utils.runInCatch
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.net.NetHelper
import es.dmoral.toasty.Toasty

/**
 * # GlobalApp
 * 基础Application
 * @author 17719
 * 2018/8/8
 */

open class GlobalApp : Application() {

    override fun onCreate() {
        launchTime = System.currentTimeMillis()
        super.onCreate()
        AppInfo.attachApplication(this)
        if (!BuildConfig.DEBUG) {
            runInCatch {
                Vog.init(this, Log.ERROR)
            }
        }
        NetHelper.timeout = 5
        Toasty.Config.getInstance()
                .tintIcon(true) // optional (apply textColor also to the icon)
                .allowQueue(false) // optional (prevents several Toastys from queuing)
                .setTextSize(14)
                .apply() // required
    }


    companion object {
        var launchTime: Long = 0

        //        var toastHandler: ColorfulToast.ToastHandler? = null
        lateinit var APP: Application
        val GApp: GlobalApp
            get() = APP as GlobalApp

        fun getString(id: Int): String = APP.getString(id)

        @JvmStatic
        @JvmOverloads
        fun toastInfo(rId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastInfo(getString(rId), duration)
        }

        @JvmStatic
        @JvmOverloads
        fun toastShort(msg: String?, duration: Int = Toast.LENGTH_SHORT) = toastInfo(msg, duration)

        @JvmStatic
        @JvmOverloads
        fun toastInfo(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
            ToastyHelper.toast(TYPE_INFO, "$msg", duration)
        }

        @JvmOverloads
        @JvmStatic
        fun toastSuccess(sId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastSuccess(getString(sId), duration)
        }

        @JvmStatic
        @JvmOverloads
        fun toastSuccess(msg: String, duration: Int = Toast.LENGTH_SHORT) {
            ToastyHelper.toast(TYPE_SUCCESS, msg, duration)
        }

        @JvmStatic
        @JvmOverloads
        fun toastError(sId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastError(getString(sId), duration)
        }

        @JvmStatic
        @JvmOverloads
        fun toastError(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
            ToastyHelper.toast(TYPE_ERROR, "$msg", duration)
        }

        @JvmStatic
        @JvmOverloads
        fun toastWarning(sId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastWarning(getString(sId), duration)
        }

        @JvmStatic
        @JvmOverloads
        fun toastWarning(msg: String, duration: Int = Toast.LENGTH_SHORT) {
            ToastyHelper.toast(TYPE_WARNING, msg, duration)
        }

        var serviceBridge: ServiceBridge? = null

    }

    /**
     * 是否为主进程
     * 未配置主进程名 默认为包名
     */
    val isMainProcess
        get() = this.packageName == currentProcessName

    val currentProcessName
        get(): String? {
            val pid = android.os.Process.myPid()
            val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.runningAppProcesses.forEach { process ->
                if (process.pid == pid)
                    return process.processName.also {
                        Vog.d("进程：${process.processName}")
                    }
            }
            return null
        }

    override fun attachBaseContext(base: Context?) {
        APP = this
        super.attachBaseContext(base)
        try {
            MultiDex.install(base)
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
    }

}