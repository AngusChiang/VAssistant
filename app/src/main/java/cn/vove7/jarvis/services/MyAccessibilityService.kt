package cn.vove7.jarvis.services

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.accessibility.AccessibilityBridge
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.activities
import cn.vove7.common.utils.isInputMethod
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.notifier.ActivityShowListener
import cn.vove7.common.view.notifier.UiViewShowNotifier
import cn.vove7.common.view.notifier.ViewShowListener
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.plugins.*
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.statusbar.AccessibilityStatusAnimation
import cn.vove7.jarvis.view.statusbar.StatusAnimation
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.SystemHelper
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * 基于
 * Created by Vove on 2018/1/13.
 * cn.vove7
 */
class MyAccessibilityService : AccessibilityApi(), AccessibilityBridge {
    private lateinit var pkgman: PackageManager
    private val accAni: StatusAnimation by lazy { AccessibilityStatusAnimation() }
    override fun onServiceConnected() {
//        accessibilityService = this

        pkgman = packageManager
        updateCurrentApp(packageName, "")
        accAni.showAndHideDelay("服务开启", 5000L)

        startPluginService()
    }

    /**
     * # 等待app|Activity表
     * - [CExecutorI] 执行器
     * - pair.first pkg
     * - pair.second activity
     */
    private val locksWaitForActivity = mutableMapOf<ActivityShowListener, ActionScope>()

    /**
     *  Notifier By [currentScope]
     */
    private val activityNotifier = AppChangNotifier(locksWaitForActivity)

    private fun startPluginService() {
        runOnPool {
            //注册无障碍组件
            registerPlugin(activityNotifier)
            if (AppConfig.isAdBlockService)
                registerPlugin(AdKillerService)
            if (AppConfig.fixVoiceMico) {
                registerPlugin(VoiceWakeupStrategy)
            }
//            if (BuildConfig.DEBUG) {
//                registerPlugin(AutoLearnService)
//            }
        }
    }


    @Synchronized
    private fun updateCurrentApp(pkg: String, activityName: String) {
        if (currentScope.packageName == pkg && activityName == currentActivity) return
        AdvanAppHelper.getAppInfo(pkg).also {
            // 系统界面??
            currentAppInfo = try {//todo 防止阻塞
                if (it == null || it.isInputMethod(this) || !it.activities().contains(activityName)) {//过滤输入法、非activity
                    return
                } else it
            } catch (e: Exception) {
                GlobalLog.err(e)
                it
            }
        }
        Vog.d(this, "updateCurrentApp ---> $pkg")
        Vog.d(this, "updateCurrentApp ---> $activityName")
        currentScope.activity = activityName
        currentScope.packageName = pkg
        Vog.d(this, currentScope.toString())
        dispatchPluginsEvent(ON_APP_CHANGED, currentScope)//发送事件
    }

    override fun getRootViewNode(): ViewNode? {
        val root = rootInWindow
        return if (root == null) null
        else ViewNode(root)
    }


    override fun waitForActivity(executor: CExecutorI, scope: ActionScope) {
        locksWaitForActivity[executor] = scope
        runOnCachePool {
            sleep(200)
            activityNotifier.onAppChanged(currentScope)
        }
    }

    /**
     *
     * 等待界面出现指定ViewId
     * viewId 特殊标记
     */
    private val locksWaitForView = hashMapOf<ViewFinder, ViewShowListener>()

    /**
     * notify when view show
     */
    private val viewNotifier = UiViewShowNotifier(locksWaitForView)

    override fun waitForView(executor: CExecutorI, finder: ViewFinder) {
        locksWaitForView[finder] = executor
        viewNotifierThread?.interrupt()
        viewNotifierThread = thread {
            try {
                sleep(200)
            } catch (e: InterruptedException) {
                return@thread
            }
            viewNotifier.notifyIfShow()
        }
    }

    override fun removeAllNotifier(executor: CExecutorI) {
        runOnCachePool {
            synchronized(locksWaitForActivity) {
                val a = locksWaitForActivity.remove(executor)
                Vog.d(this, "removeAllNotifier locksWaitForActivity ${a != null}")
            }
            synchronized(locksWaitForView) {
                //                val success = values.remove(executor)
                val removeList = mutableListOf<ViewFinder>()
                locksWaitForView.forEach {
                    if (it.value == executor) removeList.add(it.key)
                }
                Vog.d(this, "removeAllNotifier locksWaitForView ${removeList.size}")
                removeList.forEach {
                    locksWaitForView.remove(it)
                }
                removeList.clear()
            }
        }
    }

    var viewNotifierThread: Thread? = null

    /**
     * 通知UI更新（UI驱动事件）
     */
    private fun callAllNotifier() {
        viewNotifierThread?.interrupt()
        viewNotifierThread = thread {
            viewNotifier.notifyIfShow()
        }
        dispatchPluginsEvent(ON_UI_UPDATE, rootInWindow)
    }

    var lastContentChangedTime = 0L
    /**
     * fixme 导致 FastHub 代码浏览异常卡顿
     * @param event AccessibilityEvent?
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventSource = try {
            event?.source
        } catch (e: Exception) {// NullPoint in event.source
            GlobalLog.err(e, "mas206")
            return
        }
        //熄屏|低电量
        if (AppConfig.disableAccessibilityOnLowBattery && PowerEventReceiver.powerSavingMode &&
                !PowerEventReceiver.isCharging) {//开启低电量模式
            Vog.d(this, "onAccessibilityEvent ---> 低电量")
            return
        }

        if (!SystemHelper.isScreenOn(this)) {//(火)?息屏下
            Vog.d(this, "onAccessibilityEvent ---> 熄屏")
            return
        }
        if (null == event || null == eventSource)
            return

        Vog.v(this, "class :$currentAppInfo - $currentActivity ${event.className} \n" +
                AccessibilityEvent.eventTypeToString(event.eventType))
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//界面切换
            val classNameStr = event.className
            val pkg = event.packageName as String?
            Vog.v(this, "WINDOW_STATE_CHANGED ---> $classNameStr $pkg")
//            if (packageName == pkg) {//fix 悬浮窗造成阻塞
//                Vog.d(this, "onAccessibilityEvent ---> 自身(屏蔽悬浮窗)")
//                return
//            }
            runOnCachePool {
                if (classNameStr != null && pkg != null)
                    updateCurrentApp(pkg, classNameStr.toString())
            }
        }
        runOnCachePool {
            if (blackPackage.contains(currentScope.packageName)) {//black list
                Vog.v(this, "onAccessibilityEvent ---> in black")
                return@runOnCachePool
            }
            //根据事件回调类型进行处理
            when (eventType) {
                //通知栏发生改变
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
//                callAllNotifier()
                }
                TYPE_WINDOWS_CHANGED -> {
                    callAllNotifier()
                }
                TYPE_WINDOW_CONTENT_CHANGED -> {//"帧"刷新  限制频率
                    System.currentTimeMillis().also {
                        if (it - lastContentChangedTime < 300) {
                            Vog.d(this, "onAccessibilityEvent ---> lock")
                            return@runOnCachePool
                        }
                        lastContentChangedTime = it
                    }
                    callAllNotifier()
                }
//            TYPE_VIEW_SCROLLED -> {
//                callAllNotifier()
//            }
                TYPE_VIEW_CLICKED -> {
//                lastScreenEvent = event
                    callAllNotifier()
//                try {
//                    Vog.d(this, "onAccessibilityEvent ---> 点击 :${ViewNode(event.source)}")
//                } catch (e: Exception) {
//                }
                }

            }
        }
    }

    private fun startTraverse(rootNode: AccessibilityNodeInfo?) {
        return
//        if (BuildConfig.DEBUG) {
//            val builder = StringBuilder("\n" + rootNode?.packageName + "\n")
//            traverseAllNode(builder, 0, rootNode)
//            Vog.v(this, "onAccessibilityEvent  ---->" + builder.toString() + " \n\n\n")
//        }
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
            Vog.d(this, "error traverseAllNode  ----> ${e.message}")
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
    private var startupRunner: Runnable = Runnable {
        MainService.instance?.onCommand(AppBus.ORDER_START_RECOG)
    }

    private var stopRunner: Runnable = Runnable {
        MainService.instance?.onCommand(AppBus.ORDER_STOP_EXEC)
    }

    private var stopSpeakRunner: Runnable = Runnable {
        MainService.instance?.speechSynService?.stop(true)
    }
//    private var delayUp = 600L

    private var v2 = false // 单击上下键 取消识别
    private var v3 = false // 是否触发长按唤醒
    /**
     * 按键监听
     * 熄屏时无法监听
     * @param event KeyEvent
     * @return Boolean
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        Vog.v(this, "onKeyEvent  ----> " + event.toString())
//        try {
//            if (!AppConfig.volumeWakeUpWhenScreenOff &&
//                    !SystemHelper.isScreenOn(GlobalApp.APP))//(火)?息屏下
//                return super.onKeyEvent(event)
//        } catch (e: Exception) {
//            GlobalLog.err(e)
//            return super.onKeyEvent(event)
//        }
        when (event.action) {
            KeyEvent.ACTION_DOWN -> when (event.keyCode) {
                KEYCODE_VOLUME_DOWN -> {
                    return when {
                        MainService.recoIsListening -> {//下键取消聆听
                            v2 = true
                            MainService.instance?.onCommand(AppBus.ORDER_CANCEL_RECOG)//up speed
                            true
                        }
                        MainService.speaking -> {
                            postLongDelay(stopSpeakRunner)
                            true
                        }
                        MainService.exEngineRunning -> {//长按下键
                            //正在执行才会触发
                            postLongDelay(stopRunner)
                            true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
                KEYCODE_HEADSETHOOK -> {
                    when {
                        MainService.recoIsListening -> {//按下停止聆听
                            v2 = true
                            MainService.instance?.onCommand(AppBus.ORDER_STOP_RECOG)
                            return true
                        }
                        AppConfig.wakeUpWithHeadsetHook -> {//长按耳机中键唤醒
                            postLongDelay(startupRunner)
                            return true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
                KEYCODE_VOLUME_UP -> {
                    when {
                        MainService.recoIsListening -> {//按下停止聆听
                            v2 = true
                            MainService.instance?.onCommand(AppBus.ORDER_STOP_RECOG)
                            return true
                        }
                        AppConfig.isLongPressVolUpWakeUp -> {//长按唤醒
                            postLongDelay(startupRunner)
                            return true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
//                KEYCODE_HOME -> {
//                    postLongDelay(startupRunner)
//                    return true
//                }
            }
            KeyEvent.ACTION_UP -> {
                when (event.keyCode) {
                    KEYCODE_HEADSETHOOK, KEYCODE_VOLUME_UP ->
                        if (v3) {
                            return removeDelayIfInterrupt(event, startupRunner) || super.onKeyEvent(event)
                        }
                    KEYCODE_VOLUME_DOWN -> {
                        if (v3) {
                            when {
                                MainService.instance?.speechSynService?.speaking == true -> {
                                    return removeDelayIfInterrupt(event, stopSpeakRunner) || super.onKeyEvent(event)
                                }
                                MainService.exEngineRunning -> {//长按下键
                                    return removeDelayIfInterrupt(event, stopRunner) || super.onKeyEvent(event)
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
        Vog.d(this, "removeDelayIfInterrupt ---> $runnable")
        if ((event.eventTime - event.downTime) < (AppConfig.volumeKeyDelayUp)) {//时间短 移除runner 调节音量
            delayHandler.removeCallbacks(runnable)
            when (event.keyCode) {
                KEYCODE_VOLUME_UP -> SystemBridge.volumeUp()
                KEYCODE_HEADSETHOOK -> {
                    SystemBridge.switchMusicStatus()//
                }
                KEYCODE_VOLUME_DOWN -> SystemBridge.volumeDown()
                else -> return false
            } //其他按键
        }
        return true
    }

    override fun onInterrupt() {
        Vog.d(this, "onInterrupt ")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterPlugin(activityNotifier)
        if (AppConfig.isAdBlockService)
            unregisterPlugin(AdKillerService)
        if (AppConfig.fixVoiceMico) {
            unregisterPlugin(VoiceWakeupStrategy)
        }
    }

    override fun getService(): AccessibilityService = this

    companion object {

        private val absCls = arrayOf("AbsListView", "ViewGroup", "CategoryPairLayout")
        fun inAbs(n: String): Boolean {
            absCls.forEach {
                if (n.contains(it))
                    return true
            }
            return false
        }

        val blackPackage = hashSetOf("com.android.chrome", "com.android.systemui", GlobalApp.APP.packageName)

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

    override fun powerSavingMode() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            disableSelf()
//        }
        accAni.failed("省电模式，服务关闭")
    }

    override fun disablePowerSavingMode() {
        accAni.showAndHideDelay("服务恢复", 5000L)
    }

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
                    MainService.switchReco()
                }
            })
        }
    }
}
