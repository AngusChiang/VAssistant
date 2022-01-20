package cn.vove7.common.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.FOCUS_INPUT
import cn.vove7.android.common.logi
import cn.vove7.common.NeedAccessibilityException
import cn.vove7.common.accessibility.component.AccPluginService
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.ShellHelper
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.common.utils.gotoAccessibilitySetting2
import cn.vove7.common.utils.whileWaitTime
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.extend.runOnUi
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.math.min

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
abstract class AccessibilityApi : AccessibilityService() {
    abstract fun getService(): AccessibilityService

    val currentScope = ActionScope()
    var currentActivity: String? = null
        private set
        get() = currentScope.activity

    val rootInWindow: AccessibilityNodeInfo?
        get() {
            return try {
                rootInActiveWindow //will occur exception
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    val rootViewNode: ViewNode?
        get() = rootInWindow?.let { ViewNode(it) }

    val currentFocusedEditor: ViewNode?
        get() = findFocus(FOCUS_INPUT)?.let { ViewNode(it) }

    /**
     * 禁用软键盘，并且无法手动弹出
     * @return Boolean
     */
    fun disableSoftKeyboard(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            softKeyboardController.setShowMode(SHOW_MODE_HIDDEN)
        } else {
            GlobalLog.log("7.0以下不支持hideSoftKeyboard")
            false
        }
    }

    fun enableSoftKeyboard(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            softKeyboardController.setShowMode(SHOW_MODE_AUTO)
        } else {
            GlobalLog.log("7.0以下不支持hideSoftKeyboard")
            false
        }
    }

    var currentAppInfo: AppInfo? = null
        protected set

    companion object {
        const val WHICH_SERVICE_BASE = 0
        const val WHICH_SERVICE_GESTURE = 1

        //无障碍基础服务
        var accessibilityService: AccessibilityApi? = null

        //无障碍高级服务 执行手势等操作
        var gestureService: AccessibilityApi? = null

        val currentScope get() = accessibilityService?.currentScope

        val isBaseServiceOn: Boolean
            get() = (accessibilityService != null)
        val isGestureServiceOn: Boolean get() = gestureService != null

        @JvmStatic
        fun isServiceEnable(which: Int): Boolean =
            if (which == WHICH_SERVICE_BASE) isBaseServiceOn
            else isGestureServiceOn

        fun serviceCls(which: Int): Class<*> {
            return Class.forName(if (which == WHICH_SERVICE_BASE) {
                "cn.vove7.jarvis.services.MyAccessibilityService"
            } else {
                "cn.vove7.jarvis.services.GestureService"
            })
        }

        /**
         * 等待无障碍开启，最长等待30s
         * @param waitMillis Long
         * @return Boolean
         * @throws NeedAccessibilityException
         */
        @JvmOverloads
        @JvmStatic
        @Throws(NeedAccessibilityException::class)
        fun waitAccessibility(which: Int = WHICH_SERVICE_BASE, waitMillis: Long = 30000): Boolean {
            requireAccessibility(which, waitMillis, true)
            return true
        }

        fun requireGestureService(): AccessibilityApi {
            return requireAccessibility(WHICH_SERVICE_GESTURE)
        }

        @JvmStatic
        @JvmOverloads
        fun requireAccessibility(
            which: Int = WHICH_SERVICE_BASE, waitMillis: Long = 30000,
            jump: Boolean = true
        ): AccessibilityApi {
            if (!isServiceEnable(which)) {
                if (ShellHelper.hasRootOrAdb() || canWriteSecureSettings()) {
                    autoOpenService(which, true, true)
                } else if(jump) {
                    GlobalApp.toastInfo("请手动开启无障碍服务")
                    PermissionUtils.gotoAccessibilitySetting2(GlobalApp.APP, serviceCls(which))
                    whileWaitTime(min(30000, waitMillis)) {
                        if (isServiceEnable(which)) true
                        else {
                            sleep(500)
                            null
                        }
                    } ?: throw NeedAccessibilityException(which)
                }
            }
            if (isServiceEnable(which)) {
                return if (which == WHICH_SERVICE_BASE)
                    accessibilityService!!
                else gestureService!!
            } else {
                throw NeedAccessibilityException(which)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun autoOpenService(
            which: Int = WHICH_SERVICE_BASE,
            checkAfter: Boolean,
            failByUser: Boolean = false,
            toast: Boolean = true
        ): Boolean {
            if (ShellHelper.hasRootOrAdb() || canWriteSecureSettings()) {
                openServiceSelf(which)
                if (checkAfter) {
                    val b = whileWaitTime(2000) {
                        if (isServiceEnable(which)) true
                        else {
                            sleep(500)
                            null
                        }
                    }
                    "wait acs: $b".logi()
                    return if (b == true) {
                        true
                    } else {
                        runOnUi {
                            if (failByUser) {
                                if(toast) {
                                    GlobalApp.toastInfo("自动开启失败, 请手动开启无障碍服务")
                                }
                                val service = serviceCls(which)
                                PermissionUtils.gotoAccessibilitySetting2(GlobalApp.APP, service)
                            }
                        }
                        false
                    }
                }
            }
            runOnUi {
                if (failByUser) {
                    val service = serviceCls(which)
                    PermissionUtils.gotoAccessibilitySetting2(GlobalApp.APP, service)
                }
            }
            return false
        }

        /**
         * @return 是否成功
         */
        fun openServiceSelf(which: Int): Boolean {
            val serviceEnabled =
                if (which == WHICH_SERVICE_BASE) isBaseServiceOn
                else isGestureServiceOn
            if (serviceEnabled) return true

            val service = serviceCls(which).name

            val (s, b) = when {
                canWriteSecureSettings() -> {
                    "使用WRITE_SECURE_SETTINGS权限" to openServiceBySettings(service)
                }
                ShellHelper.hasRootOrAdb() -> {
                    "使用Root权限" to ShellHelper.openAppAccessService(GlobalApp.APP.packageName, service)
                }
                else -> {
                    "无任何权限" to false
                }
            }
            val msg = "$s 无障碍开启${if (b) "成功" else "失败"} $service"
            if (b) GlobalLog.log(msg)
            else GlobalLog.err(msg)
            return b
        }

        private fun canWriteSecureSettings(): Boolean {
            return PermissionUtils.isAllGranted(GlobalApp.APP,
                arrayOf("android.permission.WRITE_SECURE_SETTINGS"))
        }

        /**
         * 执行开启无障碍,需要系统App权限
         * @return Boolean
         */
        private fun openServiceBySettings(serviceName: String): Boolean {
            val context = GlobalApp.APP
            var enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
            val selfComponentName = ComponentName(context.packageName, serviceName)
            val flattenToString = selfComponentName.flattenToString()

            if (!enabledServicesSetting.contains(flattenToString)) {
                enabledServicesSetting += ":$flattenToString"
            }
            return try {
                Settings.Secure.putString(context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, enabledServicesSetting)
                Settings.Secure.putInt(context.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED, 1)
                true
            } catch (e: Throwable) {
                false
            }
        }


        /**
         * 注册放于静态变量，只用于通知事件。
         */
        private val pluginsServices = ConcurrentSkipListSet<AccPluginService>()

        /**
         * 注册无障碍插件服务
         * @param e AccPluginService
         */
        fun registerPlugin(e: AccPluginService) {
            pluginsServices.add(e)
            e.bindService()
        }

        fun unregisterPlugin(e: AccPluginService) {
            pluginsServices.remove(e)
            e.unBindServer()
        }

        const val ON_APP_CHANGED = 1

        /**
         * 分发事件
         * @param what Int
         * @param data Any?
         */
        @SuppressWarnings("Unchecked")
        fun dispatchPluginsEvent(what: Int, data: Any? = null) {
            if (data == null) return
            when (what) {
                ON_APP_CHANGED -> {
                    Vog.d("dispatchPluginsEvent ---> ON_APP_CHANGED")
                    if (pluginsServices.isNotEmpty()) {
                        launch {
                            pluginsServices.forEach {
                                it.onAppChanged(data as ActionScope)
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

}
