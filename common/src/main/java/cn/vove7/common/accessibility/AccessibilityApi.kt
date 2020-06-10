package cn.vove7.common.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.FOCUS_INPUT
import cn.vove7.common.NeedAccessibilityException
import cn.vove7.common.accessibility.component.AccPluginService
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.RootHelper
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.common.utils.gotoAccessibilitySetting2
import cn.vove7.common.utils.whileWaitTime
import cn.vove7.vtp.app.AppInfo
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
        //无障碍基础服务
        var accessibilityService: AccessibilityApi? = null

        //无障碍高级服务 执行手势等操作 fixme 开启后部分机型掉帧
        var gestureService: AccessibilityApi? = null

        val currentScope get() = accessibilityService?.currentScope

        val isBaseServiceOn: Boolean
            get() = (accessibilityService != null)
        val isAdvanServiceOn: Boolean get() = gestureService != null

        /**
         * 等待无障碍开启，最长等待30s
         * @param waitMillis Long
         * @return Boolean
         * @throws NeedAccessibilityException
         */
        @JvmOverloads
        @JvmStatic
        @Throws(NeedAccessibilityException::class)
        fun waitAccessibility(waitMillis: Long = 30000): Boolean {
            if (isBaseServiceOn) return true
            else PermissionUtils.gotoAccessibilitySetting2(GlobalApp.APP, Class.forName("cn.vove7.jarvis.services.MyAccessibilityService"))

            return whileWaitTime(min(30000, waitMillis)) {
                if (isBaseServiceOn)
                    true
                else {
                    sleep(500)
                    null
                }
            } ?: throw NeedAccessibilityException()
        }

        fun requireAccessibility() {
            if (!isBaseServiceOn) {
                PermissionUtils.gotoAccessibilitySetting2(GlobalApp.APP, Class.forName("cn.vove7.jarvis.services.MyAccessibilityService"))
                throw NeedAccessibilityException()
            }
        }


        /**
         * @return 是否成功
         */
        fun openServiceSelf(what: Int): Boolean {
            if (isBaseServiceOn) return true

            val service = if (what == 0) {
                "cn.vove7.jarvis.services.MyAccessibilityService"
            } else {
                "cn.vove7.jarvis.services.GestureService"
            }

            val (s, b) = when {
                RootHelper.hasRoot() -> {
                    "使用Root权限" to RootHelper.openAppAccessService(GlobalApp.APP.packageName, service)
                }
                canWriteSecureSettings() -> {
                    "使用WRITE_SECURE_SETTINGS权限" to openServiceBySettings(service)
                }
                else -> {
                    "无任何权限" to false
                }
            }
            val msg = "$s 无障碍开启${if (b) "成功" else "失败"}"
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
