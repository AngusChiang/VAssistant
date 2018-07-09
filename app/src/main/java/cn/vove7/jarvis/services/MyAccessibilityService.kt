package cn.vove7.jarvis.services

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import cn.vove7.datamanager.parse.model.ActionScope
import cn.vove7.executorengine.Executor
import cn.vove7.executorengine.bridge.AccessibilityBridge
import cn.vove7.executorengine.model.ViewNode
import cn.vove7.jarvis.view.finder.ViewFinder
import cn.vove7.jarvis.view.finder.ViewFinderByDesc
import cn.vove7.jarvis.view.finder.ViewFinderById
import cn.vove7.jarvis.view.finder.ViewFinderByText
import cn.vove7.jarvis.view.notifier.ViewShowNotifier
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.app.AppUtil
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * Created by Vove on 2018/1/13.
 * cn.vove7
 */

class MyAccessibilityService : AccessibilityBridge() {
    private var currentActivity: String = ""

    private var currentAppInfo: AppInfo? = null
    private lateinit var pkgman: PackageManager

    override fun onServiceConnected() {
        pkgman = packageManager
        updateCurrentApp(packageName)
        Toast.makeText(this, "无障碍服务开启", Toast.LENGTH_SHORT).show()
        Vog.d("Vove :", "无障碍服务开启")
        //代码配置
        accessibilityService = this

    }

    private fun updateCurrentApp(pkg: String) {
        currentAppInfo = AppUtil.getAppInfo(this, "", pkg)
        currentScope.activity = currentActivity
        currentScope.packageName = pkg
        Vog.d(this, currentScope.toString())
    }

    /**
     * # 等待Activity表
     * - [Executor] 执行器
     * - pair.first pkg
     * - pair.second activity
     */
    private val locksWaitForActivity = mutableMapOf<Executor, Pair<String, String>>()

    override fun waitForActivity(executor: Executor, pkg: String, activityName: String?) {
        locksWaitForActivity[executor] = Pair(pkg, "$activityName")
        thread {
            sleep(200)
            activityNotifier.notifyIfShow()
        }
    }

    /**
     * id,desc,appPkg 共用
     */
    private val locksWaitForView = mutableMapOf<Executor, Pair<String, String>>()

    /**
     * 等待界面出现指定ViewId
     * viewId 特殊标记
     */
    override fun waitForAppViewId(executor: Executor, pkg: String, viewId: String) {
        locksWaitForView[executor] = Pair(pkg, viewId)
        thread {
            sleep(200)
            appAndIdNotifier.notifyIfShow()
        }
    }

    /**
     * 等待出现指定ViewId
     * 特殊标记
     */
    override fun waitForViewId(executor: Executor, viewId: String) {
        locksWaitForView[executor] = Pair("", viewId)
        thread {
            sleep(200)
            viewIdNotifier.notifyIfShow()
        }
    }

    /**
     * 等待出现指定View Desc
     */
    override fun waitForViewDesc(executor: Executor, desc: String) {
        locksWaitForView[executor] = Pair("", desc)
        thread {
            sleep(200)
            viewDescNotifier.notifyIfShow()
        }
    }

    /**
     * 等待出现指定View Text
     */
    override fun waitForViewText(executor: Executor, text: String) {
        locksWaitForView[executor] = Pair("", text)
        thread {
            sleep(200)
            viewTextNotifier.notifyIfShow()
        }
    }

    override fun removeAllNotifier(executor: Executor) {
        thread {
            synchronized(locksWaitForActivity) {
                locksWaitForActivity.remove(executor)
            }
            synchronized(locksWaitForView) {
                locksWaitForView.remove(executor)
            }
        }
    }

    private fun callAllNotifier() {
        thread {
            appAndIdNotifier.notifyIfShow()
            viewDescNotifier.notifyIfShow()
            viewIdNotifier.notifyIfShow()
        }
        thread {
            activityNotifier.notifyIfShow()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (null == event || null == event.source) {
            return
        }
        val classNameStr = event.className
        val pkg = event.packageName as String
        if (classNameStr.startsWith(pkg)) {
            currentActivity = classNameStr.substring(classNameStr.lastIndexOf('.') + 1)
            updateCurrentApp(pkg)
        }
        Vog.v(this, "class :${currentAppInfo?.name} - ${currentAppInfo?.packageName} - $currentActivity " +
                AccessibilityEvent.eventTypeToString(event.eventType))
        val eventType = event.eventType
        //根据事件回调类型进行处理
        when (eventType) {
        //通知栏发生改变
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
            }
        //窗口的状态发生改变
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {//窗口切换
                startTraverse(rootInActiveWindow)
                callAllNotifier()
            }
            TYPE_WINDOWS_CHANGED -> {
                callAllNotifier()
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> {
                startTraverse(rootInActiveWindow)
                callAllNotifier()
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> {
                startTraverse(rootInActiveWindow)
                callAllNotifier()
            }
            TYPE_WINDOW_CONTENT_CHANGED -> {//"帧"刷新
                val node = event.source
                startTraverse(node)
                callAllNotifier()
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
    val outputPar = false

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
        Log.d("Vove :", "onGesture  ----> $gestureId")
        return super.onGesture(gestureId)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        Log.d("Vove :", "onKeyEvent  ----> " + event.toString())
        return super.onKeyEvent(event)
    }


    override fun onInterrupt() {
        Vog.d(this, "onInterrupt ")

    }

    override fun onDestroy() {
        accessibilityService = null
        super.onDestroy()
    }

    /**
     * 构建id
     */
    private fun buildId(id: String): String = rootInActiveWindow.packageName.toString() + ":viewId/" + id


    /**
     * 匹配  ***.***.***:id/view_id
     */
    override fun findNodeById(id: String): List<ViewNode> {
        val list = ViewFinderById(this, id).findAll()

        val rList = mutableListOf<ViewNode>()
        list.forEach {
            rList.add(ViewNode(it))
        }
        Vog.d(this, "findNodeById size :${rList.size}")
        return rList
    }

    //TODO :autoFindByText
    override fun autoFindByText() {
    }

    //TODO :utilFindById
    override fun utilFindById() {
    }

    /**
     * 一直返回，直到这个Activity
     */
//TODO :backUtilActivity
    override fun backUtilActivity() {
    }

    /**
     * 一直上滑，直到出现
     */
//TODO :scrollUpUtilFind
    override fun scrollUpUtilFind() {
    }

    override fun findFirstNodeByIdAndText(id: String, text: String): ViewNode? {
        val l = findNodeByText(text)
        l.forEach {
            Vog.d(this, "findFirstNodeByIdAndText ${it.node.viewIdResourceName}")
            if (it.node.viewIdResourceName != null && it.node.viewIdResourceName.endsWith("/$id")) {
                Vog.d(this, "findFirstNodeByIdAndText find it")
                return it
            }
        }
        Vog.d(this, "findFirstNodeByIdAndText didn't find it")
        return null
    }

    override fun findFirstNodeById(id: String): ViewNode? {
        val node = ViewFinderById(this, id).findFirst()
        return if (node != null) ViewNode(node) else null
    }

    override fun findFirstNodeByDesc(desc: String): ViewNode? {
        val node = ViewFinderByDesc(this, desc).findFirst()
        return if (node != null) {
            ViewNode(node)
        } else null
    }

    override fun findFirstNodeByText(text: String): ViewNode? {
        val l = findNodeByText(text)
        return if (l.isNotEmpty()) l[0] else null
    }

    override fun findFirstNodeByTextWhitFuzzy(text: String): ViewNode? {
        val node = ViewFinderByText(this,
                ViewFinderByText.MATCH_TYPE_FUZZY_WITH_PINYIN, text).findFirst()
        return if (node != null)
            ViewNode(node)
        else null
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
    private val activityNotifier = object : ViewShowNotifier(null, locksWaitForActivity) {
        override fun filter(data: Pair<String, String>): Boolean {
            val s = ActionScope(data.first, data.second)
            Vog.v(this, "filter $currentScope - $s")
            return currentScope == s
        }

        override fun logTag(): String = "ActivityNotifier"

        override fun notifyCondition(data: Pair<String, String>): Boolean = true
    }
    /**
     * Notifier By Desc
     */
    private val viewDescNotifier = object : ViewShowNotifier(ViewFinderByDesc(this), locksWaitForView) {
        override fun logTag(): String = "DescNotifier"
        override fun buildFinder(data: String): ViewFinder? {
            (viewFinder as ViewFinderByDesc).desc = data
            return viewFinder
        }
    }
    /**
     * Notifier By Text
     */
    private val viewTextNotifier = object : ViewShowNotifier(ViewFinderByText(this), locksWaitForView) {
        override fun logTag(): String = "DescNotifier"
        override fun buildFinder(data: String): ViewFinder? {
            (viewFinder as ViewFinderByText).text = data
            return viewFinder
        }
    }
    private val viewIdNotifier = object : ViewShowNotifier(ViewFinderById(this), locksWaitForView) {
        override fun logTag(): String = "IdNotifier"
        override fun buildFinder(data: String): ViewFinder? {
            (viewFinder as ViewFinderById).viewId = data
            return viewFinder
        }
    }
    /**
     * Notifier By App&&ViewId
     */
    private val appAndIdNotifier = object : ViewShowNotifier(ViewFinderById(this), locksWaitForView) {
        override fun filter(data: Pair<String, String>): Boolean {
            return currentScope.packageName.startsWith(data.first)
        }

        override fun logTag(): String = "AppAndIdNotifier"

        override fun buildFinder(data: String): ViewFinder? {
            (viewFinder as ViewFinderById).viewId = data
            return viewFinder
        }
    }


    companion object {
        var accessibilityService: AccessibilityBridge? = null

        private val absCls = arrayOf("AbsListView", "ViewGroup", "CategoryPairLayout")
        fun inAbs(n: String): Boolean {
            absCls.forEach {
                if (n.contains(it))
                    return true
            }
            return false
        }
    }
/*
//根据文本,返回文本对应的View
AccessibilityNodeInfo source = event.getSource();
AccessibilityNodeInfo listNode = source.getChild(0).getChild(1);
List<AccessibilityNodeInfo> itemList = listNode.findAccessibilityNodeInfosByText("文本");
//通常需要配合getParent(),或者getChild(),方法一起使用;
//拿到View之后,就可以调用"单击事件","滚动事件",等所有支持的事件了
//info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
//检测当前是那个界面,也是通过查找这个界面固有的文本信息,来判断;
*/


}
