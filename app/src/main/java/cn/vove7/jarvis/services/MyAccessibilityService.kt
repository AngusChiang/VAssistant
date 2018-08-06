package cn.vove7.jarvis.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import cn.vove7.common.ShowListener
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.finder.ViewShowListener
import cn.vove7.common.viewnode.ViewNode
import cn.vove7.datamanager.parse.model.ActionScope
import cn.vove7.common.view.finder.ViewFinderByDesc
import cn.vove7.common.view.finder.ViewFinderById
import cn.vove7.common.view.finder.ViewFinderByText
import cn.vove7.jarvis.view.finder.ViewShowNotifier
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
        Toast.makeText(this, "无障碍服务开启", Toast.LENGTH_SHORT).show()
        Vog.d("Vove :", "无障碍服务开启")
        //代码配置

    }

    private fun updateCurrentApp(pkg: String) {
        currentAppInfo = AppHelper.getAppInfo(this, "", pkg)
        currentScope.activity = currentActivity
        currentScope.packageName = pkg
        Vog.d(this, currentScope.toString())
    }

    /**
     * # 等待Activity表
     * - [CExecutorI] 执行器
     * - pair.first pkg
     * - pair.second activity
     */
    private val locksWaitForActivity = mutableMapOf<ViewShowListener, Pair<String, String>>()

    override fun waitForActivity(finderNotify: ViewShowListener, pkg: String, activityName: String?) {
        locksWaitForActivity[finderNotify] = Pair(pkg, "$activityName")
        thread {
            sleep(200)
            activityNotifier.notifyIfShow()
        }
    }

    /**
     *
     * 等待界面出现指定ViewId
     * viewId 特殊标记
     */
    private val locksWaitForView = mutableMapOf<ViewShowListener, ViewFinder>()

    /**
     * notify when view show
     */
    private val viewNotifier = ViewShowNotifier(locksWaitForView)

    override fun waitForView(finderNotify: ViewShowListener, finder: ViewFinder) {
        locksWaitForView[finderNotify] = finder
        thread {
            sleep(200)
            viewNotifier.notifyShow()
        }
    }

    override fun removeAllNotifier(finderNotify: ViewShowListener) {
        thread {
            synchronized(locksWaitForActivity) {
                locksWaitForActivity.remove(finderNotify)
            }
            synchronized(locksWaitForView) {
                locksWaitForView.remove(finderNotify)
            }
        }
    }

    private fun callAllNotifier() {
        thread {
            viewNotifier.notifyShow()
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
        Vog.d(this, "onGesture  ----> $gestureId")
        return super.onGesture(gestureId)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        Vog.d(this, "onKeyEvent  ----> " + event.toString())
        return super.onKeyEvent(event)
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
     * 构建id
     */
    private fun buildId(id: String): String = rootInActiveWindow.packageName.toString() + ":viewId/" + id


    /**
     * 匹配  ***.***.***:id/view_id
     */
    override fun findNodeById(id: String): List<ViewNode> {
        val list = ViewFinderById(this, id).findAll()
        Vog.d(this, "findNodeById size :${list.size}")
        return list
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
        return ViewFinderById(this, id).findFirst()
    }

    override fun findFirstNodeByDesc(desc: String): ViewNode? {
        return ViewFinderByDesc(this, desc).findFirst()
    }

    override fun findFirstNodeByText(text: String): ViewNode? {
        val l = findNodeByText(text)
        return if (l.isNotEmpty()) l[0] else null
    }

    override fun findFirstNodeByTextWhitFuzzy(text: String): ViewNode? {
        return ViewFinderByText(this,
                ViewFinderByText.MATCH_MODE_FUZZY_WITH_PINYIN, text).findFirst()
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
    private val activityNotifier = object : ShowListener {
        fun fill(data: Pair<String, String>): Boolean {
            val s = ActionScope(data.first, data.second)
            Vog.v(this, "filter $currentScope - $s")
            return currentScope == s
        }

        override fun notifyIfShow() {
            synchronized(locksWaitForActivity) {
                val removes = mutableListOf<ViewShowListener>()
                locksWaitForActivity.forEach { it ->
                    if (fill(it.value)) {
                        it.key.notifyShow()
                        removes.add(it.key)
                    }
                }
                removes.forEach {
                    locksWaitForActivity.remove(it)
                }
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