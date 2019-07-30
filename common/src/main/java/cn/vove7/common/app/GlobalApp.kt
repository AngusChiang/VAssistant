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
import cn.vove7.smartkey.android.AndroidSettings

import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import com.qihoo360.replugin.*
import es.dmoral.toasty.Toasty

/**
 * # GlobalApp
 * 基础Application
 * @author 17719
 * 2018/8/8
 */

open class GlobalApp : RePluginApplication() {
    override fun onCreate() {
        APP = this
        launchTime = System.currentTimeMillis()
        super.onCreate()
        AppInfo.attachApplication(this)
        if (!BuildConfig.DEBUG) {
            runInCatch {
                Vog.init(this, Log.ERROR)
            }
        }
        AndroidSettings.init(this)
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

        fun toastInfo(rId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastInfo(getString(rId), duration)
        }

        fun toastInfo(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
            ToastyHelper.toast(TYPE_INFO, "$msg", duration)
        }

        fun toastSuccess(sId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastSuccess(getString(sId), duration)
        }

        fun toastSuccess(msg: String, duration: Int = Toast.LENGTH_SHORT) {
            ToastyHelper.toast(TYPE_SUCCESS, msg, duration)
        }

        fun toastError(sId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastError(getString(sId), duration)
        }

        fun toastError(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
            ToastyHelper.toast(TYPE_ERROR, "$msg", duration)
        }

        fun toastWarning(sId: Int, duration: Int = Toast.LENGTH_SHORT) {
            toastWarning(getString(sId), duration)
        }

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
        super.attachBaseContext(base)
        try {
            MultiDex.install(base)
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
        RePlugin.enableDebugger(base, BuildConfig.DEBUG)
    }

    /**
     * RePlugin允许提供各种“自定义”的行为，让您“无需修改源代码”，即可实现相应的功能
     */
    override fun createConfig(): RePluginConfig {
        return RePluginConfig().apply {

            // 允许“插件使用宿主类”。默认为“关闭”
            isUseHostClassIfNotFound = true

            // RePlugin默认会对安装的外置插件进行签名校验，这里先关掉，避免调试时出现签名错误
            verifySign = false
            isUseHostClassIfNotFound = true
            // 针对“安装失败”等情况来做进一步的事件处理
            eventCallbacks = HostEventCallbacks()
            // 若宿主为Release，则此处应加上您认为"合法"的插件的签名，例如，可以写上"宿主"自己的。
            // RePlugin.addCertSignature("AAAAAAAAA");

            // 在Art上，优化第一次loadDex的速度
            // setOptimizeArtLoadDex(true);

        }
    }

    override fun createCallbacks(): RePluginCallbacks {
        return HostCallbacks(this)
    }

    /**
     * 宿主针对RePlugin的自定义行为
     */
    inner class HostCallbacks constructor(context: Context) : RePluginCallbacks(context) {

        override fun onPluginNotExistsForActivity(context: Context?, plugin: String?, intent: Intent?, process: Int): Boolean {
            return super.onPluginNotExistsForActivity(context, plugin, intent, process)
        }

        /**
         * 自定义PluginDexClassLoader
         */
//        override fun createPluginClassLoader(pi: PluginInfo?, dexPath: String?, optimizedDirectory: String?, librarySearchPath: String?, parent: ClassLoader?): PluginDexClassLoader {
//            return MyPluginDexClassLoader(pi, dexPath, optimizedDirectory, librarySearchPath, parent)
//        }
    }

    private inner class HostEventCallbacks : RePluginEventCallbacks(this) {

        override fun onInstallPluginFailed(path: String?, code: RePluginEventCallbacks.InstallResult?) {
            // 大部分可以通过RePlugin.install的返回值来判断是否成功
            GlobalLog.err("onInstallPluginFailed: Failed! path=$path; r=$code")
            super.onInstallPluginFailed(path, code)
        }

        override fun onStartActivityCompleted(plugin: String?, activity: String?, result: Boolean) {
            // 当打开Activity成功时触发此逻辑，可在这里做一些APM、打点统计等相关工作
            super.onStartActivityCompleted(plugin, activity, result)
        }
    }
}