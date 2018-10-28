package cn.vove7.jarvis.services

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
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
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.notifier.ActivityShowListener
import cn.vove7.common.view.notifier.UiViewShowNotifier
import cn.vove7.common.view.notifier.ViewShowListener
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.plugins.AccPluginsService
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.SystemHelper
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * 基于
 * Created by Vove on 2018/1/13.
 * cn.vove7
 */
class MyAccessibilityService : AccessibilityApi() {
    private lateinit var pkgman: PackageManager

    override fun onServiceConnected() {
//        accessibilityService = this

        pkgman = packageManager
        updateCurrentApp(packageName, "")
        ColorfulToast(this).yellow().showShort("无障碍服务开启")

        startPluginService()
    }

    private fun startPluginService() {
        thread {
            registerEvent(activityNotifier)
            if (AppConfig.isAdBlockService)
                registerEvent(AdKillerService)
        }
    }


    private fun updateCurrentApp(pkg: String, activityName: String) {
        if (currentScope.packageName == pkg && activityName == currentActivity) return
        AdvanAppHelper.getAppInfo(pkg).also {
            if (it == null || it.isInputMethod(this)) {//过滤输入法
                return
            } else currentAppInfo = it
        }
        currentActivity = activityName
        Vog.d(this, "updateCurrentApp ---> $pkg")
        Vog.d(this, "updateCurrentApp ---> $activityName")
        currentScope.activity = currentActivity
        currentScope.packageName = pkg
        Vog.d(this, currentScope.toString())
        dispatchPluginsEvent(ON_APP_CHANGED, currentScope)//发送事件
    }

    override fun getRootViewNode(): ViewNode? {
        val root = rootInWindow
        return if (root == null) null
        else ViewNode(root)
    }


    /**
     * # 等待Activity表
     * - [CExecutorI] 执行器
     * - pair.first pkg
     * - pair.second activity
     */
    private val locksWaitForActivity = mutableMapOf<ActivityShowListener, ActionScope>()

    override fun waitForActivity(executor: CExecutorI, scope: ActionScope) {
        locksWaitForActivity[executor] = scope
        thread {
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
        thread {
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
     *
     */
    private fun callAllNotifier() {
        viewNotifierThread?.interrupt()
        viewNotifierThread = thread {
            viewNotifier.notifyIfShow()
        }
        dispatchPluginsEvent(ON_UI_UPDATE, rootInWindow)
    }

    private val rootInWindow: AccessibilityNodeInfo?
        get() {
            return try {
                rootInActiveWindow
            } catch (e: Exception) {
                null
            }
        }

    /**
     *
     * @param event AccessibilityEvent?
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!SystemHelper.isScreenOn(this))//(火)?息屏下
            return
        try {
            if (null == event || null == event.source) {
                return
            }
        } catch (e: Exception) {// NullPoint in event.source
            GlobalLog.err(e.message)
            return
        }
        Vog.v(this, "class :$currentAppInfo - ${event.className} \n" +
                AccessibilityEvent.eventTypeToString(event.eventType))
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//界面切换
            val classNameStr = event.className
            val pkg = event.packageName as String?
            Vog.v(this, "onAccessibilityEvent ---> $classNameStr $pkg")
            if (packageName == pkg) {//fix 悬浮窗造成阻塞
                Vog.d(this, "onAccessibilityEvent ---> 自身(屏蔽悬浮窗)")
                return
            }
//            Vog.d(this, "TYPE_WINDOW_STATE_CHANGED --->\n $pkg ${event.className}")
            if (classNameStr != null && pkg != null)
                updateCurrentApp(pkg, classNameStr.toString())

            callAllNotifier()
        }
        if (blackPackage.contains(currentScope.packageName)) {//black list
            Vog.v(this, "onAccessibilityEvent ---> in black")
            return
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
            TYPE_WINDOW_CONTENT_CHANGED -> {//"帧"刷新
                val node = event.source
                callAllNotifier()
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> {
//                startTraverse(rootInActiveWindow)
//                callAllNotifier()
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> {
//                startTraverse(rootInActiveWindow)
//                callAllNotifier()
            }
            TYPE_VIEW_SCROLLED -> {
                callAllNotifier()
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


    private fun nodeSummary(node: AccessibilityNodeInfo?): String {
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

    private fun getT(d: Int): String {
        val builder = StringBuilder()
        for (i in 0..d)
            builder.append("|")
        builder.append("|")
        return builder.toString()
    }

    override fun onGesture(gestureId: Int): Boolean {
        Vog.d(this, "onGesture  ----> $gestureId")
        return super.onGesture(gestureId)
    }

    private val delayHandler = Handler()
    private var startupRunner: Runnable = Runnable {
        MainService.instance?.onCommand(AppBus.ORDER_START_RECO)
    }
    private var stopRunner: Runnable = Runnable {
        MainService.instance?.onCommand(AppBus.ORDER_STOP_EXEC)
    }
//    private var delayUp = 600L

    private var v2 = false // 单击上下键 取消识别
    private var v3 = false // 是否触发长按唤醒
    var lastEvent: KeyEvent? = null
    /**
     * 按键监听
     * @param event KeyEvent
     * @return Boolean
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        Vog.v(this, "onKeyEvent  ----> " + event.toString())
        try {
            if (!AppConfig.volumeWakeUpWhenScreenOff &&
                    !SystemHelper.isScreenOn(GlobalApp.APP))//(火)?息屏下
                return super.onKeyEvent(event)
        } catch (e: Exception) {
            GlobalLog.err(e)
            return super.onKeyEvent(event)
        }
        lastEvent = event
        when (event.action) {
            KeyEvent.ACTION_DOWN -> when (event.keyCode) {
                KEYCODE_VOLUME_DOWN -> {
                    return when {
                        MainService.recoIsListening -> {//下键取消聆听
                            v2 = true
                            MainService.instance?.onCommand(AppBus.ORDER_CANCEL_RECO)//up speed
                            true
                        }
                        MainService.exEngineRunning -> {//长按下键
                            //正在执行才会触发
                            postLongDelay(stopRunner)
                            true
                        }
                        SpeechSynService.speaking -> {
                            SpeechSynService.stop()
                            true
                        }
                        else -> super.onKeyEvent(event)
                    }
                }
                KEYCODE_HEADSETHOOK, KEYCODE_VOLUME_UP -> {
                    when {
                        MainService.recoIsListening -> {//按下停止聆听
                            v2 = true
                            MainService.instance?.onCommand(AppBus.ORDER_STOP_RECO)
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
                    KEYCODE_VOLUME_DOWN ->
                        if (v3) {
                            return removeDelayIfInterrupt(event, stopRunner) || super.onKeyEvent(event)
                        }
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun postLongDelay(runnable: Runnable) {
        v3 = true
        delayHandler.postDelayed(runnable, AppConfig.volumeKeyDelayUp)
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
        if ((event.eventTime - event.downTime) < (AppConfig.volumeKeyDelayUp - 100)) {//时间短 移除runner 调节音量
            delayHandler.removeCallbacks(runnable)
            when (event.keyCode) {
                KEYCODE_VOLUME_UP -> SystemBridge.volumeUp()
                KEYCODE_HEADSETHOOK -> {
                    SystemBridge.switchMusicStatus()//
//                    Vog.d(this,"removeDelayIfInterrupt ---> KEYCODE_HEADSETHOOK resume")
//                    if (lastEvent != null)
//                        super.onKeyEvent(lastEvent)
//                    super.onKeyEvent(event)

                }
                KEYCODE_VOLUME_DOWN -> SystemBridge.volumeDown()
                else -> return false
            } //其他按键
        } else {
//            if (event.keyCode == KEYCODE_VOLUME_UP && event.eventTime - event.downTime > 3000) {//长按松下,结束聆听
//                //
//                MainService.instance?.onCommand(MainService.ORDER_STOP_EXEC)
//                return true
//            }
        }
        return true
    }

    override fun onInterrupt() {
        Vog.d(this, "onInterrupt ")

    }

    override fun onDestroy() {
        accessibilityService = null
        super.onDestroy()
    }

    override fun getService(): AccessibilityService = this

    /**
     *  Notifier By [currentScope]
     */
    private val activityNotifier = object : AccPluginsService() {
        override fun onUiUpdate(root: AccessibilityNodeInfo?) {
//            onAppChanged(currentScope)//...
        }

        fun fill(data: ActionScope): Boolean {
            Vog.v(this, "filter $currentScope - $data")
            return currentScope == data
        }

        override fun onAppChanged(appScope: ActionScope) {
            synchronized(locksWaitForActivity) {
                val removes = mutableListOf<ActivityShowListener>()
                kotlin.run out@{
                    locksWaitForActivity.forEach { it ->
                        if (fill(it.value)) {
                            it.key.notifyShow(currentScope)
                            removes.add(it.key)
                        }
                        if (Thread.currentThread().isInterrupted) {
                            Vog.d(this, "activityNotifier 线程关闭")
                            return@out
                        }
                    }
                }
                removes.forEach { locksWaitForActivity.remove(it) }
                removes.clear()
            }
        }
    }

    companion object {

        private val absCls = arrayOf("AbsListView", "ViewGroup", "CategoryPairLayout")
        fun inAbs(n: String): Boolean {
            absCls.forEach {
                if (n.contains(it))
                    return true
            }
            return false
        }

        val blackPackage = hashSetOf("com.android.chrome", "com.android.systemui")
        private const val ON_UI_UPDATE = 0
        private const val ON_APP_CHANGED = 1
//        private const val ON_BIND = 2

        /**
         * 注册放于静态变量，只用于通知事件。
         */
        private val pluginsServices = mutableSetOf<PluginsService>()

        fun registerEvent(e: PluginsService) {
            synchronized(pluginsServices) {
                pluginsServices.add(e)
                e.bindService()
            }
        }

        fun unregisterEvent(e: PluginsService) {
            synchronized(pluginsServices) {
                pluginsServices.remove(e)
                e.unBindServer()
            }
        }

        /**
         * 分发事件
         * @param what Int
         * @param data Any?
         */
        private fun dispatchPluginsEvent(what: Int, data: Any? = null) {
            if (data == null) return
            synchronized(pluginsServices) {
                when (what) {
                    ON_UI_UPDATE -> {
                        pluginsServices.forEach {
                            thread { it.onUiUpdate(data as AccessibilityNodeInfo?) }
                        }
                    }
                    ON_APP_CHANGED -> {
                        pluginsServices.forEach {
                            thread { it.onAppChanged(data as ActionScope) }
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

fun AppInfo.isInputMethod(context: Context): Boolean {

    val pm = context.packageManager
    val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES)
    pkgInfo.services?.forEach {
        if (it.permission == Manifest.permission.BIND_INPUT_METHOD) {
            Vog.d(this, "isInputMethod ---> 输入法：$packageName")
            return true
        }
    }
    return false
}
