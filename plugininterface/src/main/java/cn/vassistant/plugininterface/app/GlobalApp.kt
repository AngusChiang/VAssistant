package cn.vassistant.plugininterface.app

import android.app.Application
import android.content.Context
import android.content.Intent
import cn.vassistant.plugininterface.BuildConfig
import cn.vassistant.plugininterface.bridges.ServiceBridge
import cn.vassistant.plugininterface.toast.ColorfulToast
import cn.vove7.vtp.log.Vog
import com.qihoo360.replugin.RePluginApplication
import com.qihoo360.replugin.RePluginCallbacks
import com.qihoo360.replugin.RePluginConfig
import com.qihoo360.replugin.RePluginEventCallbacks

/**
 * # GlobalApp
 *
 * @author 17719
 * 2018/8/8
 */

open class GlobalApp : RePluginApplication() {
    override fun onCreate() {
        APP = this
        super.onCreate()
        colorfulToast = ColorfulToast(this).blue()
    }

    companion object {
        //        var toastHandler: ColorfulToast.ToastHandler? = null
        lateinit var APP: Application
        val GApp: Application
            get() = APP

        fun getString(id: Int): String = APP.getString(id)

        fun toastShort(msg: String) {
            (APP as GlobalApp).toastShort(msg)
        }

        fun toastShort(rId: Int) {
            toastShort(getString(rId))
        }

        fun toastLong(msg: String) {
            (APP as GlobalApp).toastLong(msg)
        }

        fun toastLong(rId: Int) {
            toastLong(getString(rId))
        }

        var serviceBridge: ServiceBridge? = null

    }

    private fun toastShort(msg: String) {
        colorfulToast.showShort(msg)
    }

    private fun toastLong(msg: String) {
        colorfulToast.showLong(msg)
    }

    lateinit var colorfulToast: ColorfulToast

    /**
     * RePlugin允许提供各种“自定义”的行为，让您“无需修改源代码”，即可实现相应的功能
     */
    override fun createConfig(): RePluginConfig {
        val c = RePluginConfig()

        // 允许“插件使用宿主类”。默认为“关闭”
        c.isUseHostClassIfNotFound = true

        // FIXME RePlugin默认会对安装的外置插件进行签名校验，这里先关掉，避免调试时出现签名错误
        c.verifySign = !BuildConfig.DEBUG
        c.isUseHostClassIfNotFound = true
        // 针对“安装失败”等情况来做进一步的事件处理
        c.eventCallbacks = HostEventCallbacks(this)
        // FIXME 若宿主为Release，则此处应加上您认为"合法"的插件的签名，例如，可以写上"宿主"自己的。
        // RePlugin.addCertSignature("AAAAAAAAA");

        // 在Art上，优化第一次loadDex的速度
        // c.setOptimizeArtLoadDex(true);
        return c
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
    }

    private inner class HostEventCallbacks(context: Context) : RePluginEventCallbacks(context) {

        override fun onInstallPluginFailed(path: String?, code: RePluginEventCallbacks.InstallResult?) {
            // FIXME 当插件安装失败时触发此逻辑。您可以在此处做“打点统计”，也可以针对安装失败情况做“特殊处理”
            // 大部分可以通过RePlugin.install的返回值来判断是否成功
            Vog.d(this, "onInstallPluginFailed: Failed! path=$path; r=$code")
            super.onInstallPluginFailed(path, code)
        }

        override fun onStartActivityCompleted(plugin: String?, activity: String?, result: Boolean) {
            // FIXME 当打开Activity成功时触发此逻辑，可在这里做一些APM、打点统计等相关工作
            super.onStartActivityCompleted(plugin, activity, result)
        }
    }
}