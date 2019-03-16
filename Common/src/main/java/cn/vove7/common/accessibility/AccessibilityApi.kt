package cn.vove7.common.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.os.Build
import android.provider.Settings
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.FOCUS_INPUT
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.R
import cn.vove7.common.accessibility.component.AccPluginService
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.utils.ThreadPool
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog

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
        get() =
            rootInWindow.let {
                if (it == null) null
                else ViewNode(it)
            }
//
//    override fun getRootViewNode(): ViewNode? {
//        val root = rootInWindow
//        return if (root == null) null
//        else ViewNode(root)
//    }

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

    var currentAppInfo: AppInfo? = null
        protected set

    companion object {
        //无障碍基础服务
        var accessibilityService: AccessibilityApi? = null
        //无障碍高级服务 执行手势等操作 fixme 开启后部分机型掉帧
        var gestureService: AccessibilityApi? = null
        val isBaseServiceOn: Boolean
            get() = (accessibilityService != null)
        val isAdvanServiceOn: Boolean get() = gestureService != null


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


        /**
         * 注册放于静态变量，只用于通知事件。
         */
        private val pluginsServices = mutableSetOf<AccPluginService>()

        /**
         * 注册无障碍插件服务
         * @param e AccPluginService
         */
        fun registerPlugin(e: AccPluginService) {
            synchronized(pluginsServices) {
                pluginsServices.add(e)
                e.bindService()
            }
        }

        fun unregisterPlugin(e: AccPluginService) {
            synchronized(pluginsServices) {
                pluginsServices.remove(e)
                e.unBindServer()
            }
        }

        const val ON_UI_UPDATE = 0
        const val ON_APP_CHANGED = 1
        /**
         * 分发事件
         * @param what Int
         * @param data Any?
         */
        @SuppressWarnings("Unchecked")
        fun dispatchPluginsEvent(what: Int, data: Any? = null) {
            if (data == null) return
            synchronized(pluginsServices) {
                when (what) {
                    ON_UI_UPDATE -> {
//                        pluginsServices.forEach {
//                            ThreadPool.runOnCachePool { it.onUiUpdate(data as AccessibilityNodeInfo) }
//                        }
                    }
                    ON_APP_CHANGED -> {
                        Vog.d("dispatchPluginsEvent ---> ON_APP_CHANGED")
                        ThreadPool.runOnCachePool {
                            pluginsServices.forEach {
                                it.onAppChanged(data as ActionScope)
                            }
                        }
                    }
//                    ON_BIND -> {
//                        pluginsServices.forEach {
//                            thread { it.onBind() }
//                        }
//                    }
                    else -> {
                    }
                }
            }
        }
    }

}
