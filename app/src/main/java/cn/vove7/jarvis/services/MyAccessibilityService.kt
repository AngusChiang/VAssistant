package cn.vove7.jarvis.services

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.view.accessibility.AccessibilityEvent.eventTypeToString
import android.view.accessibility.AccessibilityNodeInfo
import cn.daqinjia.android.common.logd
import cn.daqinjia.android.common.logv
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.GlobalActionExecutor
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.common.utils.StubbornFlag
import cn.vove7.common.utils.runInCatch
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.plugins.VoiceWakeupStrategy
import cn.vove7.jarvis.view.statusbar.AccessibilityStatusAnimation
import cn.vove7.jarvis.view.statusbar.StatusAnimation
import cn.vove7.vtp.log.Vog

/**
 * 无障碍基础服务
 * Created by Vove on 2018/1/13.
 * cn.vove7
 */
class MyAccessibilityService : AccessibilityApi() {
    private val accAni: StatusAnimation by lazy { AccessibilityStatusAnimation() }
    override fun onCreate() {
        accessibilityService = this
        super.onCreate()
    }

    override fun onServiceConnected() {

        updateCurrentApp(packageName, "")
        accAni.showAndHideDelay("服务开启", 5000L)

        startPluginService()
        if (AppConfig.showAccessNavButton) {
            showNavButton()
        }
    }

    private val onNavClick by lazy {
        @TargetApi(Build.VERSION_CODES.O)
        object : AccessibilityButtonController.AccessibilityButtonCallback() {
            override fun onClicked(controller: AccessibilityButtonController?) {
                when (AppConfig.homeFun) {
                    0 -> startActivity(ScreenAssistActivity.createIntent())
                    1 -> MainService.switchRecog()
                    else -> startActivity(ScreenAssistActivity.createIntent())
                }
            }
        }
    }

    fun showNavButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ser = serviceInfo
            ser.flags = ser.flags or FLAG_REQUEST_ACCESSIBILITY_BUTTON
            serviceInfo = ser
            accessibilityButtonController.registerAccessibilityButtonCallback(onNavClick)
        }
    }

    fun hideNavButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //0x00000100
            val ser = serviceInfo
            ser.flags = ser.flags and 0x0ffffeff
            serviceInfo = ser
            accessibilityButtonController.unregisterAccessibilityButtonCallback(onNavClick)
        }
    }

    private fun startPluginService() {
        launch {
            //注册无障碍组件
            if (AppConfig.isAdBlockService)
                registerPlugin(AdKillerService)
            if (AppConfig.fixVoiceMicro) {
                registerPlugin(VoiceWakeupStrategy)
            }
//            if (BuildConfig.DEBUG) {
//                registerPlugin(AutoLearnService)
//            }
        }
    }

    private fun pageIsView(pageName: String): Boolean = try {
        Class.forName(pageName) is View
    } catch (e: ClassNotFoundException) {
        false
    }


    /**
     * 更新当前[currentScope]
     * @param pkg String
     * @param pageName String Activity or Dialog
     */
    private fun updateCurrentApp(pkg: String, pageName: String) {
        synchronized(MyAccessibilityService::class.java) {
            if (currentScope.packageName == pkg && pageName == currentActivity) return
            currentAppInfo = AdvanAppHelper.getAppInfo(pkg).let {
                // 系统界面??
                try {//todo 防止阻塞
                    //过滤输入法、非activity
                    if (
                            it == null ||
                            pageName.startsWith("android.widget") ||
                            pageName.startsWith("android.view") ||
                            pageName.startsWith("android.inputmethodservice") ||
                            pageIsView(pageName)
                    ) {
                        return
                    } else it
                } catch (e: Exception) {
                    GlobalLog.err(e.message)
                    it
                }
            }
            Vog.v("updateCurrentApp ---> $pkg")
            Vog.v("updateCurrentPage ---> $pageName")
            currentScope.activity = pageName
            currentScope.packageName = pkg
            Vog.e(currentScope.toString())
            dispatchPluginsEvent(ON_APP_CHANGED, currentScope)//发送事件
        }
    }

    /**
     * @param event AccessibilityEvent?
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val eventType = event.eventType

        if (BuildConfig.DEBUG) runInCatch {
            event.logv()
            ("""currentAppInfo: $currentAppInfo
currentActivity: $currentActivity 
eventType: ${eventTypeToString(eventType)}
event: pkg: ${event.packageName} cls: ${event.className}""").logd()
        }
        when (eventType) {
            TYPE_WINDOW_STATE_CHANGED -> {
                //界面切换
                val classNameStr = event.className
                val pkg = event.packageName as String?
                if (!classNameStr.isNullOrBlank() && pkg != null) {
                    launch {
                        updateCurrentApp(pkg, classNameStr.toString())
                    }
                }
            }
        }
    }

    /**
     * is output ViewGroup
     */
    private val outputPar = false

    /**
     * 遍历AccessibilityEvent
     */
    private fun traverseAllNode(builder: StringBuilder, dep: Int, node: AccessibilityNodeInfo?) {
        if (node == null) return

        if (!outputPar && !isPar(node.className.toString())) {
            builder.append(getT(dep)).append(nodeSummary(node))
        } else if (node.isVisibleToUser)//
            builder.append(getT(dep)).append(nodeSummary(node))
        (0 until node.childCount).forEach { i ->
            val childNode = node.getChild(i)
            traverseAllNode(builder, dep + 1, childNode)
        }
    }

    private fun isPar(className: String): Boolean {
        return try {
            val cls = Class.forName(className as String?) as Class
            val co = cls.getDeclaredConstructor(Context::class.java)
            co.isAccessible = true
            co.newInstance(this) is ViewGroup

        } catch (e: Exception) {
            Vog.d("error traverseAllNode  ----> ${e.message}")
            inAbs(className)
        }
    }

    private fun getT(d: Int): String {
        val builder = StringBuilder()
        for (i in 0..d)
            builder.append("|")
        builder.append("|")
        return builder.toString()
    }

    private val delayHandler = Handler()

    /////////////////////////////////////////////////

    //按键监听
    private var startupRunner: Runnable = Runnable {
        MainService.onCommand(AppBus.ACTION_START_RECOG)
    }

    private var stopExecRunner: Runnable = Runnable {
        MainService.onCommand(AppBus.ACTION_STOP_EXEC)
    }

    private var stopSpeakRunner: Runnable = Runnable {
        MainService.speechSynService?.stop(true)
    }
//    private var delayUp = 600L

    private var v2 = false // 单击上下键 取消识别
    private var v3 = false // 是否有触发长按

    /**
     * 点按音量加，再长按触发音量增大
     */
    private var v4 by StubbornFlag<Long?>(null)


    var keyListener: ((event: KeyEvent) -> Unit)? = null

    /**
     * 按键监听
     * 熄屏时无法监听
     * @param event KeyEvent
     * @return Boolean
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        val lis = keyListener
        if (lis != null) {
            lis.invoke(event)
            return true
        }
//        Vog.v("onKeyEvent  ----> $event")
        when (event.action) {
            ACTION_DOWN -> when (event.keyCode) {
                KEYCODE_VOLUME_DOWN -> {
                    return when {
                        MainService.recogIsListening -> {//下键取消聆听
                            v2 = true
                            MainService.onCommand(AppBus.ACTION_CANCEL_RECOG)//up speed
                            true
                        }
                        MainService.speaking -> {
                            postLongDelay(stopSpeakRunner)
                            true
                        }
                        MainService.exEngineRunning -> {//长按下键
                            //正在执行才会触发
                            postLongDelay(stopExecRunner)
                            true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
                //耳机
                KEYCODE_HEADSETHOOK -> {
                    return when {
                        MainService.recogIsListening -> {//按下停止聆听
                            v2 = true
                            MainService.onCommand(AppBus.ACTION_STOP_RECOG)
                            true
                        }
                        AppConfig.wakeUpWithHeadsetHook -> {//长按耳机中键唤醒
                            postLongDelay(startupRunner)
                            true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
                in AppConfig.wakeupKeys -> {
                    return when {
                        MainService.recogIsListening -> {//按下停止聆听
                            v2 = true
                            MainService.onCommand(AppBus.ACTION_STOP_RECOG)
                            true
                        }
                        AppConfig.isLongPressKeyWakeUp -> {//长按自定义键唤醒
                            postLongDelay(startupRunner)
                            true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
                KEYCODE_VOLUME_UP -> {
                    v4?.apply {
                        //点按后 长按
                        if ((this + 1000) > System.currentTimeMillis()) {
                            return super.onKeyEvent(event)
                        }
                    }
                    return when {
                        MainService.recogIsListening -> {//按下停止聆听
                            v2 = true
                            MainService.onCommand(AppBus.ACTION_STOP_RECOG)
                            true
                        }
                        AppConfig.isLongPressKeyWakeUp -> {//长按唤醒
                            postLongDelay(startupRunner)
                            true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
//                KEYCODE_HOME -> {
//                    postLongDelay(startupRunner)
//                    return true
//                }
            }
            ACTION_UP -> {
                when (event.keyCode) {
                    KEYCODE_HEADSETHOOK, KEYCODE_VOLUME_UP, in AppConfig.wakeupKeys ->
                        if (v3) {
                            return removeDelayIfInterrupt(event, startupRunner) || super.onKeyEvent(event)
                        }
                    KEYCODE_VOLUME_DOWN -> {
                        if (v3) {
                            when {
                                MainService.speechSynService?.speaking == true -> {
                                    return removeDelayIfInterrupt(event, stopSpeakRunner) || super.onKeyEvent(event)
                                }
                                MainService.exEngineRunning -> {//长按下键
                                    return removeDelayIfInterrupt(event, stopExecRunner) || super.onKeyEvent(event)
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun postLongDelay(runnable: Runnable) {
        v3 = true
        delayHandler.postDelayed(runnable, AppConfig.volumeKeyDelayUp.toLong())
    }

    private fun removeDelayIfInterrupt(event: KeyEvent, runnable: Runnable): Boolean {
        if (v3) {
            v3 = false
            v2 = false // ???
        } else return false
        if (v2) {//防止弹出音量调节
            v2 = false
            return true
        }
        Vog.d("removeDelayIfInterrupt ---> $runnable")
        if ((event.eventTime - event.downTime) < (AppConfig.volumeKeyDelayUp)) {//时间短 移除runner 调节音量
            delayHandler.removeCallbacks(runnable)
            when (event.keyCode) {
                KEYCODE_VOLUME_UP -> {
                    //标记
                    v4 = System.currentTimeMillis()
                    SystemBridge.volumeUp()
                }
                KEYCODE_HEADSETHOOK -> SystemBridge.switchMusicStatus()
                KEYCODE_VOLUME_DOWN -> SystemBridge.volumeDown()
                KEYCODE_BACK -> GlobalActionExecutor.back()
                KEYCODE_HOME -> GlobalActionExecutor.home()
                KEYCODE_APP_SWITCH -> GlobalActionExecutor.recents()
                else -> {
                    return false
                }
            } //其他按键
        }
        return true
    }

    override fun onInterrupt() {
        Vog.d("onInterrupt ")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (AppConfig.isAdBlockService)
            unregisterPlugin(AdKillerService)
        if (AppConfig.fixVoiceMicro) {
            unregisterPlugin(VoiceWakeupStrategy)
        }
        accessibilityService = null
    }

    override fun getService(): AccessibilityService = this

//    private val blackPackage = hashSetOf<String>()
//    val baseBlackPackage = listOf(//初始黑名单
//            "com.android.chrome",
//            "com.android.systemui",
//            "net.oneplus.launcher",
//            "com.fastaccess.github",
//            GlobalApp.APP.packageName
//    )

    companion object {

        private val absCls = arrayOf("AbsListView", "ViewGroup", "CategoryPairLayout")
        fun inAbs(n: String): Boolean {
            absCls.forEach {
                if (n.contains(it))
                    return true
            }
            return false
        }

//        private const val ON_BIND = 2


        fun nodeSummary(node: AccessibilityNodeInfo?): String {
            if (node == null) return "null\n"
            val clsName = node.className
            val id = node.viewIdResourceName
            val rect = Rect()
            node.getBoundsInScreen(rect)
            val cls = clsName.substring(clsName.lastIndexOf('.') + 1)
            return String.format("[%-20s] [%-20s] [%-20s] [%-20s] [%-20s] %n",
                    cls, (id),//?.substring(viewId.lastIndexOf('/') + 1) ?: "null"),
                    node.contentDescription, node.text, rect
            )
        }

    }

//    override fun powerSavingMode() {
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////            disableSelf()
////        }
//        accAni.failed("省电模式，服务关闭")
//    }

//    @Synchronized
//    override fun loadBlackList(ps: Set<String>?) {
//        blackPackage.clear()
//        if (ps != null) blackPackage.addAll(ps)
//        else blackPackage.addAll(SpHelper(this)
//                .getStringSet("acc_black_list") ?: emptyList())
//        blackPackage.addAll(baseBlackPackage)
//        Vog.d("loadBlackList ---> $blackPackage ${blackPackage.size}")
//    }

//    override fun disablePowerSavingMode() {
//        accAni.showAndHideDelay("服务恢复", 5000L)
//    }

    /**
     * 无障碍小人
     * @param b Boolean
     */
    fun setAccessibilityButton(b: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceInfo.flags = serviceInfo.flags or FLAG_REQUEST_ACCESSIBILITY_BUTTON
            serviceInfo = serviceInfo
            accessibilityButtonController.registerAccessibilityButtonCallback(object : AccessibilityButtonController.AccessibilityButtonCallback() {
                override fun onClicked(controller: AccessibilityButtonController?) {
                    super.onClicked(controller)
                    MainService.switchRecog()
                }
            })
        }
    }
}
