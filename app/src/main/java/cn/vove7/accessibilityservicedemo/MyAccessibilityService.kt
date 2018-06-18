package cn.vove7.accessibilityservicedemo

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOWS_CHANGED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import android.webkit.JavascriptInterface
import android.widget.Toast
import cn.vove7.accessibilityservicedemo.model.ViewNode
import cn.vove7.parseengine.model.AppInfo
import cn.vove7.vtp.app.AppUtil
import cn.vove7.vtp.log.Vog

/**
 * Created by Vove on 2018/1/13.
 * cn.vove7
 */

class MyAccessibilityService : AccessibilityService(), ViewOperation {

    var currentActivity: String = ""

    var currentAppInfo: AppInfo? = null
    private lateinit var pkgman: PackageManager

    override fun onServiceConnected() {
        pkgman = packageManager
        updateCurrentApp(packageName)
        Toast.makeText(this, "无障碍服务开启", Toast.LENGTH_SHORT).show()
        Log.d("Vove :", "无障碍服务开启")
        //代码配置
        accessibilityService = this

    }

    private fun updateCurrentApp(pkg: String) {
        val app = AppUtil.getAppInfo(pkg)
        if (app != null)
            currentAppInfo = AppInfo(app.loadLabel(pkgman).toString(), app.packageName, app.loadIcon(pkgman))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (null == event || null == event.source) {
            return
        }
        val classNameStr = event.className
        val pkg = event.packageName as String
        if (classNameStr.startsWith(pkg)) {
            updateCurrentApp(pkg)
            currentActivity = classNameStr.substring(pkg.length)
        }
        Vog.v(this, "class :${currentAppInfo?.name} - $currentActivity " +
                AccessibilityEvent.eventTypeToString(event.eventType))
        val eventType = event.eventType
        //根据事件回调类型进行处理
        when (eventType) {
        //当通知栏发生改变时
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
            }
        //当窗口的状态发生改变时
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {//窗口切换
                startTraverse(rootInActiveWindow)
            }
            TYPE_WINDOWS_CHANGED -> {
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> {
                startTraverse(rootInActiveWindow)
            }
            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> {
                startTraverse(rootInActiveWindow)
            }
            TYPE_WINDOW_CONTENT_CHANGED -> {//"帧"刷新
                val node = event.source
                startTraverse(node)
            }
        }
    }

    val isTr = false
    private fun startTraverse(rootNode: AccessibilityNodeInfo?) {
        if (isTr) {
            val builder = StringBuilder("\n" + rootNode?.packageName + "\n")
            traverseAllNode(builder, 0, rootNode)
            Vog.v(this, "onAccessibilityEvent  ---->" + builder.toString() + " \n\n\n")
        }
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

        if (!outputPar) {
            if (!isPar(node.className.toString())) {
                builder.append(getT(dep)).append(nodeSummary(node))
            }
        } else if (node.isVisibleToUser)
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
            Log.e("Error :", "traverseAllNode  ----> ${e.message}")
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
        return String.format("[%s] [%s] [%s] [%s] [%s] %n",
                cls, node.contentDescription,
                (id?.substring(id.lastIndexOf('/') + 1) ?: "null"),
                node.text, rect
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


    override fun onCreate() {
        super.onCreate()
        //getServiceInfo().flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        /*
       监听手势 辅助性服务可以侦听特定的手势，对做出这个手势的用户给予响应。
       这个特性,添加在Android 4.1(API级别16),要求你的辅助性服务的请求通过触摸功能来激活的。
       你的服务可以请求通过设置服务的AccessibilityServiceInfo实例的成员标志为
       FLAG_REQUEST_TOUCH_EXPLORATION_MODE来激活
       当这个功能被激活时,你的服务接收通知的辅助性手势通过你的服务的onGesture()回调方法,就可以对用户的行为做出响应
       */
    }

    override fun onInterrupt() {
        Log.d("#############", "onInterrupt")

    }

    override fun onDestroy() {
        accessibilityService = null
        super.onDestroy()
    }

    @JavascriptInterface
    override fun findNodeById(id: String): List<ViewNode> {
        val list = mutableListOf<ViewNode>()

        val rootNode = rootInActiveWindow
        val buildId = rootNode.packageName.toString() + ":id/" + id
        Vog.d(this, "findNodeById  ----> $buildId")

        for (node in rootNode.findAccessibilityNodeInfosByViewId(buildId)) {
            list.add(ViewNode(node))
        }
        Vog.d(this, "size :${list.size}")
        return list
    }

    @JavascriptInterface
    override fun findFirstNodeById(id: String): ViewNode? {
        val l = findNodeById(id)
        return if (l.isNotEmpty()) l[0] else null
    }

    @JavascriptInterface
    override fun findFirstNodeByText(text: String): ViewNode? {
        val l = findNodeByText(text)
        return if (l.isNotEmpty()) l[0] else null
    }

    @JavascriptInterface
    override fun findNodeByText(text: String): List<ViewNode> {
        val list = mutableListOf<ViewNode>()
        val rootNode = rootInActiveWindow
        for (node in rootNode.findAccessibilityNodeInfosByText(text)) {
            list.add(ViewNode(node))
        }
        Vog.d(this, "size :${list.size}")
        return list
    }

    companion object {

        fun isOk(): Boolean {
            return accessibilityService != null
        }

        var accessibilityService: MyAccessibilityService? = null

        private val absCls = arrayOf("AbsListView", "ViewGroup")
        fun inAbs(n: String): Boolean {
            absCls.forEach {
                if (n.contains(it))
                    return true
            }
            return false
        }
    }

    @JavascriptInterface
    override fun home(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    @JavascriptInterface
    override fun back(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    @JavascriptInterface
    override fun recentApp(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    @JavascriptInterface
    override fun showNotification(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
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

interface ViewOperation {
    fun findFirstNodeById(id: String): ViewNode?
    fun findNodeById(id: String): List<ViewNode>
    fun findFirstNodeByText(text: String): ViewNode?
    fun findNodeByText(text: String): List<ViewNode>

    fun back(): Boolean
    fun recentApp(): Boolean
    fun home(): Boolean
    fun showNotification(): Boolean
}