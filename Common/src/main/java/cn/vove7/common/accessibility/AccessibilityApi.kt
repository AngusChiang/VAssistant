package cn.vove7.common.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.os.Build
import android.provider.Settings
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.FOCUS_INPUT
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.vtp.app.AppInfo

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
abstract class AccessibilityApi : AccessibilityService(),
        AccessibilityBridge {
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
                null
            }
        }

    override fun onCreate() {
        accessibilityService = this
        super.onCreate()
    }

    val currentFocusedEditor: ViewNode?
        get() = findFocus(FOCUS_INPUT).let {
            if (it == null) null else ViewNode(it)
        }

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

    /**
     * 省电模式
     */
    abstract fun powerSavingMode()

    /**
     * 关闭省电
     */
    abstract fun disablePowerSavingMode()


    override fun onDestroy() {
        accessibilityService = null
        super.onDestroy()
    }

    var currentAppInfo: AppInfo? = null
        protected set

    companion object {
        var accessibilityService: AccessibilityApi? = null
        fun isOpen(): Boolean {
            return accessibilityService != null
        }

        /**
         * 执行开启无障碍,需要系统App权限
         */
        fun openServiceSelf() {
            val context = GlobalApp.APP
            var enabledServicesSetting = Settings.Secure.getString(
                    context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            val selfComponentName = ComponentName(context.packageName,
                    "cn.vove7.jarvis.services.MyAccessibilityService")
            val flattenToString = selfComponentName.flattenToString()
            if (enabledServicesSetting == null ||
                    !enabledServicesSetting.contains(flattenToString)) {
                enabledServicesSetting += flattenToString
            }
            Settings.Secure.putString(context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, enabledServicesSetting)
            Settings.Secure.putInt(context.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED, 1)
        }
    }

}

interface AccessibilityBridge {
    /**
     * 等待出现指定View  with /id/text/desc
     * 特殊标记
     */
    fun waitForView(executor: CExecutorI, finder: ViewFinder)

    fun getRootViewNode(): ViewNode?
    fun waitForActivity(executor: CExecutorI, scope: ActionScope)
    /**
     * remove all notifier when was interrupted
     */
    fun removeAllNotifier(executor: CExecutorI)
}


