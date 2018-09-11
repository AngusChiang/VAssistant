package cn.vove7.jarvis.services

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
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.executor.CExecutorI
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.notifier.ActivityShowListener
import cn.vove7.common.view.notifier.UiViewShowNotifier
import cn.vove7.common.view.notifier.ViewShowListener
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.plugins.AccPluginsService
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper
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
        accessibilityService = this
        pkgman = packageManager
        updateCurrentApp(packageName)
        ColorfulToast(this).yellow().showShort("无障碍服务开启")

        //代码配置
        registerEvent(activityNotifier)

    }

    private fun updateCurrentApp(pkg: String) {
        currentAppInfo = AppHelper.getAppInfo(this, "", pkg)
        currentScope.activity = currentActivity
        currentScope.packageName = pkg
        Vog.d(this, currentScope.toString())
        dispatchPluginsEvent(ON_APP_CHANGED, currentScope)
    }

    override fun getRootViewNode(): ViewNode? {
        if (rootInActiveWindow == null) {
            return null
        }
        return ViewNode(rootInActiveWindow)
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
                val values = locksWaitForView.values
                var success = false
                var a = true
                while (values.contains(executor) && a) {
                    a = values.remove(executor)
                    if (a) success = true
                }
                Vog.d(this, "removeAllNotifier locksWaitForView $success")
            }
        }
    }

    var viewNotifierThread: Thread? = null

    private fun callAllNotifier() {
        viewNotifierThread?.interrupt()
        viewNotifierThread = thread {
            viewNotifier.notifyIfShow()
        }
        dispatchPluginsEvent(ON_UI_UPDATE, rootInActiveWindow)
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (null == event || null == event.source) {
            return
        }
        val classNameStr = event.className
        val pkg = event.packageName as String
        if (classNameStr.startsWith(pkg)) {//解析当前App Activity
            currentActivity = classNameStr.substring(classNameStr.lastIndexOf('.') + 1)
            updateCurrentApp(pkg)
        }
        Vog.v(this, "class :${currentAppInfo?.name} - ${currentAppInfo?.packageName} - $currentActivity \n" +
                AccessibilityEvent.eventTypeToString(event.eventType))
        val eventType = event.eventType
        //根据事件回调类型进行处理
        when (eventType) {
            //通知栏发生改变
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
//                callAllNotifier()
            }
            //窗口的状态发生改变
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {//窗口切换
                startTraverse(rootInActiveWindow)
                callAllNotifier()
            }
            TYPE_WINDOWS_CHANGED -> {
                callAllNotifier()
            }
            TYPE_WINDOW_CONTENT_CHANGED -> {//"帧"刷新
                val node = event.source
                startTraverse(node)
                callAllNotifier()
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> {
                startTraverse(rootInActiveWindow)
//                callAllNotifier()
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> {
                startTraverse(rootInActiveWindow)
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
        MainService.instance?.onCommand(MainService.ORDER_SATRT_RECO)
    }
    var stopRunner: Runnable = Runnable { AppBus.post(MainService.ORDER_STOP_EXEC) }
    var delayUp = 600L

    var v2 = false
    /**
     * 按键监听
     * @param event KeyEvent
     * @return Boolean
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
//        Vog.d(this, "onKeyEvent  ----> " + event.toString())
        when (event.action) {
            KeyEvent.ACTION_DOWN -> when (event.keyCode) {
                KEYCODE_VOLUME_DOWN -> {
                    if (MainService.recoIsListening) {//下键取消聆听
                        v2 = true
                        MainService.instance?.onCommand(MainService.ORDER_CANCEL_RECO)//up speed
                    } else {
                        postLongDelay(stopRunner)
                    }
                    return true
                }
                KEYCODE_VOLUME_UP -> {
                    if (MainService.recoIsListening) {//按下停止聆听
                        v2 = true
                        MainService.instance?.onCommand(MainService.ORDER_STOP_RECO)
                    } else {
                        postLongDelay(startupRunner)
                    }
                    return true

                }
//                KEYCODE_HOME -> {
//                    postLongDelay(startupRunner)
//                    return true
//                }
            }
            KeyEvent.ACTION_UP -> {
                when (event.keyCode) {
                    KEYCODE_VOLUME_UP, KEYCODE_APP_SWITCH ->
                        return removeDelayIfInterrupt(event, startupRunner) || super.onKeyEvent(event)
                    KEYCODE_VOLUME_DOWN ->
                        return removeDelayIfInterrupt(event, stopRunner) || super.onKeyEvent(event)
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun postLongDelay(runnable: Runnable) {
        delayHandler.postDelayed(runnable, delayUp)
    }

    private fun removeDelayIfInterrupt(event: KeyEvent, runnable: Runnable): Boolean {
        if (v2) {//防止弹出音量调节
            v2 = false
            return true
        }
        if ((event.eventTime - event.downTime) < (delayUp - 100)) {//时间短 移除runner 调节音量
            delayHandler.removeCallbacks(runnable)
            when (event.keyCode) {
                KEYCODE_VOLUME_UP -> SystemBridge().volumeUp()
                KEYCODE_VOLUME_DOWN -> SystemBridge().volumeDown()
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
     * 匹配  ***.***.***:id/view_id
     */
    override fun findNodeById(id: String): List<ViewNode> {
        return ViewFindBuilder()
                .id(id).find()
                .also { Vog.d(this, "findNodeById size :${it.size}") }
    }

    override fun findFirstNodeByIdAndText(id: String, text: String): ViewNode? {
        return ViewFindBuilder()
                .containsText(text)
                .id(id)
                .findFirst()
                .also { Vog.d(this, "findFirstNodeByIdAndText $it") }
    }

    override fun findFirstNodeById(id: String): ViewNode? {
        return ViewFindBuilder().id(id).findFirst()
    }

    override fun findFirstNodeByDesc(desc: String): ViewNode? {
        return ViewFindBuilder().desc(desc).findFirst()
    }

    override fun findFirstNodeByText(text: String): ViewNode? {
        val l = findNodeByText(text)
        return if (l.isNotEmpty()) l[0] else null
    }

    override fun findFirstNodeByTextWhitFuzzy(text: String): ViewNode? {
        return ViewFindBuilder().similaryText(text).findFirst()
    }

    override fun findNodeByText(text: String): List<ViewNode> {
        val list = mutableListOf<ViewNode>()
        if (rootInActiveWindow != null)
            for (node in rootInActiveWindow.findAccessibilityNodeInfosByText(text)) {
                if (node.text == null) continue
                val newNode = ViewNode(node)
                newNode.similarityText = TextHelper.compareSimilarity(text, node.text.toString())
                list.add(newNode)
            }
        list.sort()
        Vog.d(this, "size :${list.size}")
        return list
    }

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

        private const val ON_UI_UPDATE = 0
        private const val ON_APP_CHANGED = 1

        /**
         * 注册放于静态变量，只用于通知事件。
         */
        private val pluginsServices = mutableSetOf<OnAccessibilityEvent>()

        public fun registerEvent(e: OnAccessibilityEvent) {
            synchronized(pluginsServices) {
                pluginsServices.add(e)
            }
        }

        public fun unregisterEvent(e: OnAccessibilityEvent) {
            synchronized(pluginsServices) {
                pluginsServices.remove(e)
            }
        }

        /**
         * 分发事件
         * @param what Int
         * @param data Any?
         */
        private fun dispatchPluginsEvent(what: Int, data: Any? = null) {
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
                    else -> {
                    }
                }
            }
        }
    }

}