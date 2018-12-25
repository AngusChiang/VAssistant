package cn.vove7.jarvis.view.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.AppCompatCheckedTextView
import android.view.MotionEvent
import android.view.View
import android.widget.CheckedTextView
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.fragments.base.BaseBottomDialogWithToolbar
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.custom.WarpLinearLayout
import cn.vove7.vtp.log.Vog
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

/**
 * # WordSplitDialog
 *  分词
 * @author Administrator
 * @param type 滑动时 反转方式
 * 2018/10/28
 */
class WordSplitDialog(context: Context, val rawWords: String, val type: Int = 0)
    : BaseBottomDialogWithToolbar(context, "分词") {

    val toast: ColorfulToast by lazy { ColorfulToast(context) }
    private val checkedTextList = mutableListOf<CheckedTextView>()
    private var wordList = mutableListOf<String>()
    override fun onCreateContentView(parent: View): View = layoutInflater.inflate(R.layout.dialog_pick_text, null)

    private var lexerThread: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingBar()
        lexerThread = thread {
            BaiduAipHelper.lexer(rawWords).also {
                if (Thread.currentThread().isInterrupted) return@thread
                lexerThread = null
                if (it == null) {
                    wordList.add("分词失败")
                } else {
                    wordList.addAll(it)
                }
                runOnUi {
                    init()
                }
                hideLoadingBar()
            }
        }
        setOnDismissListener {
            lexerThread?.interrupt()
        }
    }

    fun init() {
        noAutoDismiss()
        positiveButton(text = "快速搜索") {
            val st = getCheckedText(checkedTextList)
            if (st.isEmpty()) {
                toast.showShort(R.string.text_select_nothing)
                return@positiveButton
            }
            SystemBridge.quickSearch(st)
        }
        negativeButton(text = "复制") {
            val st = getCheckedText(checkedTextList)
            if (st.isEmpty()) {
                toast.showShort("已复制原文")
                SystemBridge.setClipText(rawWords)
                return@negativeButton
            }
            SystemBridge.setClipText(getCheckedText(checkedTextList))
            toast.showShort(R.string.text_copied)
        }
        neutralButton(text = "分享") {
            val st = getCheckedText(checkedTextList)
            if (st.isEmpty()) {
                toast.showShort(R.string.text_select_nothing)
                return@neutralButton
            }
            SystemBridge.shareText(st)
            dismiss()
        }
        val content = findViewById<WarpLinearLayout>(R.id.words_content)!!
        runOnUi {
            wordList.withIndex().forEach { kv ->
                val v = buildView(kv.index, kv.value)
                content.addView(v)
                checkedTextList.add(v)
            }
        }
        splitAndOnTouch(checkedTextList, content)
    }


    private fun buildView(index: Int, it: String): CheckedTextView {
        return AppCompatCheckedTextView(context).apply {
            setBackgroundResource(R.drawable.pickable_bg)
            text = it
            setPadding(20, 10, 20, 10)
            textSize = 16f
            setTextColor(Color.BLACK)
            isChecked = false
            isFocusable = false
            tag = index
        }
    }

    /**
     * 根据坐标 判断当前手指下View
     * @param list List<CheckedTextView>
     * @param x Int
     * @param y Int
     * @return CheckedTextView?
     */
    private fun findViewByPos(list: List<CheckedTextView>, x: Int, y: Int): CheckedTextView? {
        list.forEach {
            val rect = Rect()
            rect.left = it.left
            rect.right = it.right
            rect.top = it.top
            rect.bottom = it.bottom
//            Vog.d(this, "findViewByPos ---> ${it.text} ${rect2String(rect)}")
            if (rect.contains(x, y))
                return it.also {
                    Vog.d(this, "findViewByPos --->  当前: ${it.text}")
                }
        }
        return null
    }

    private fun rect2String(e: Rect): String {
        return "${e.left}-${e.right} ${e.top}-${e.bottom}"
    }

    /**
     * 获得选取连接文本
     * @param list List<CheckedTextView>
     * @return String
     */
    fun getCheckedText(list: List<CheckedTextView>): String {
        val b = StringBuilder()
        list.forEach {
            if (it.isChecked) {
                b.append(it.text)
            }
        }
        return b.toString().also {
            Vog.d(this, "getCheckedText ---> $it")
        }
    }

    /**
     * 获得选取的文本List
     * @param list List<CheckedTextView>
     * @return String
     */
    fun getCheckedList(list: List<CheckedTextView>): List<String> {
        val rlist = mutableListOf<String>()
        list.forEach {
            if (it.isChecked) {
                rlist.add(it.text.toString())
            }

        }
        return rlist
    }

    private fun backupOldCheckStatus(list: List<CheckedTextView>): Array<Boolean> {
        val rlist = mutableListOf<Boolean>()
        list.forEach {
            rlist.add(it.isChecked)
        }
        return rlist.toTypedArray()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun splitAndOnTouch(checkedTextList: List<CheckedTextView>, content: WarpLinearLayout) {
        var startView: CheckedTextView? = null
        var endView: CheckedTextView? = null
        var isMove = false
        var initStatus = false
        var lastCheckStatus = Array(checkedTextList.size) { false }
        content.setOnTouchListener { _, event ->
            Vog.v(this, " ---> ${event.x} ${event.y}")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startView = findViewByPos(checkedTextList,
                            event.x.toInt(), event.y.toInt())
                            ?: return@setOnTouchListener false
                    Vog.d(this, "buildView ---> ACTION_DOWN")
                    lastCheckStatus = backupOldCheckStatus(checkedTextList)
                    requestDisIntercept(content, true)//阻止父级拦截
                    endView = startView
                    initStatus = startView!!.isChecked
                    return@setOnTouchListener true//触发
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMove) {//点击
                        startView?.toggle()
                        switchView(startView)
                    }
                    startView = null
                    isMove = false
                    endView = null
                    requestDisIntercept(content, false)//释放
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {//移动
                    val it = findViewByPos(checkedTextList,
                            event.x.toInt(), event.y.toInt()) ?: return@setOnTouchListener true
                    if (it == endView) {
                        return@setOnTouchListener true
                    }
                    isMove = true
                    endView = it
                    val b = min(it.tag as Int, startView!!.tag as Int)
                    val e = max(it.tag as Int, startView!!.tag as Int)

                    when (type) {
                        0 -> negateAll(checkedTextList, lastCheckStatus, b, e)
                        1 -> changeChecked(checkedTextList, lastCheckStatus, b, e, initStatus)
                    }
                    Vog.d(this, "buildView ---> ACTION_MOVE ONE")
                    return@setOnTouchListener true
                }
                else -> {
                }
            }
            return@setOnTouchListener true
        }
    }

    fun switchView(view: CheckedTextView?) {
        view?.apply {
            setTextColor(if(isChecked) Color.WHITE else Color.BLACK)
        }
    }

    /**
     * 以初始位置 为 status，区间内全部按status 外部保持
     * @param checkedTextList List<CheckedTextView>
     * @param backStatus Array<Boolean>
     * @param start Int
     * @param end Int
     * @param initStatus Boolean
     */
    private fun changeChecked(checkedTextList: List<CheckedTextView>, backStatus: Array<Boolean>,
                              start: Int, end: Int, initStatus: Boolean) {
        //区间设置选中，其他恢复
        checkedTextList.forEach {
            val index = it.tag as Int
            if (index in start..end) {
                it.isChecked = !initStatus //反向
            } else {
                try {
                    it.isChecked = backStatus[index]
                } catch (e: Exception) {
                    e.printStackTrace()
                    it.isChecked = false
                }
            }
        }
    }

    /**
     * 区间内取反，外部保持
     * @param checkedTextList List<CheckedTextView>
     * @param backStatus Array<Boolean>
     * @param start Int
     * @param end Int
     */
    fun negateAll(checkedTextList: List<CheckedTextView>, backStatus: Array<Boolean>,
                  start: Int, end: Int) {
        checkedTextList.forEach {
            val index = it.tag as Int
            if (index in start..end) {
                it.isChecked = !backStatus[index]//反向
                switchView(it)
            } else {
                try {//恢复
                    it.isChecked = backStatus[index]
                } catch (e: Exception) {
                    e.printStackTrace()
                    it.isChecked = false
                }
                switchView(it)
            }
        }
    }

    /**
     * 请求父级是否拦截Touch事件
     * @param targetView View
     * @param disAllow Boolean
     */
    private fun requestDisIntercept(targetView: View, disAllow: Boolean) {
        var par = targetView.parent
        while (par != null) {
            par.requestDisallowInterceptTouchEvent(disAllow)
            par = par.parent
        }
    }

}