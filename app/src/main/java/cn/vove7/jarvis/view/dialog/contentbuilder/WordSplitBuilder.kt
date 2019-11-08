package cn.vove7.jarvis.view.dialog.contentbuilder

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import androidx.appcompat.widget.AppCompatCheckedTextView
import android.view.MotionEvent
import android.view.View
import android.widget.CheckedTextView
import android.widget.ProgressBar
import cn.vove7.bottomdialog.interfaces.ContentBuilder
import cn.vove7.bottomdialog.util.ObservableList
import cn.vove7.bottomdialog.util.fadeIn
import cn.vove7.bottomdialog.util.listenListToUpdate
import cn.vove7.common.utils.fadeOut
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.custom.WarpLinearLayout
import cn.vove7.vtp.extend.buildList
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.dialog_pick_text.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * 分词 内容构造器
 * @property rawWords String
 * @property layoutRes Int
 * @property wordList ObservableList<String>
 * @property checkedTextList MutableList<CheckedTextView>
 * @property loadingBar ProgressBar
 * @property content WarpLinearLayout
 * @property loading Boolean
 * @property lexerThread Thread?
 * @property type Int
 * @constructor
 */
class WordSplitBuilder(private val rawWords: String) : ContentBuilder() {
    override val layoutRes: Int = R.layout.dialog_pick_text

    private val wordList by listenListToUpdate(ObservableList.build<String> { }, this)
    private val checkedTextList = mutableListOf<CheckedTextView>()

    private lateinit var loadingBar: ProgressBar
    private lateinit var content: WarpLinearLayout

    private var loading: Boolean = false
        set(value) {
            runOnUi {
                runInCatch {
                    if (value) loadingBar.fadeIn()
                    else loadingBar.fadeOut(endStatus = View.INVISIBLE)
                }
            }
            field = value
        }

    private var lexerJob: Job?

    init {
        loading = true
        lexerJob = GlobalScope.launch {
            BaiduAipHelper.lexer(rawWords).also {
                loading = false
                if (it == null) {
                    wordList.add("分词失败")
                } else {
                    wordList.addAll(it)
                }
                lexerJob = null
            }
        }
    }

    override fun init(view: View) {
        loadingBar = view.loading_bar
        content = view.words_content

        if (loading) loadingBar.fadeIn()
        else loadingBar.fadeOut()
        dialog.setOnDismissListener {
            lexerJob?.cancel()
        }
    }

    override fun updateContent(type: Int, data: Any?) {
        content.removeAllViews()
        wordList.forEachIndexed { i, v ->
            val vv = buildView(i, v)
            content.addView(vv)
            checkedTextList.add(vv)
        }
        splitAndOnTouch(checkedTextList, content)
    }


    private fun buildView(index: Int, it: String): CheckedTextView {
        return AppCompatCheckedTextView(dialog.context).apply {
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

    val type: Int = 0
    @SuppressLint("ClickableViewAccessibility")
    private fun splitAndOnTouch(checkedTextList: List<CheckedTextView>, content: WarpLinearLayout) {
        var startView: CheckedTextView? = null
        var endView: CheckedTextView? = null
        var isMove = false
        var initStatus = false
        var lastCheckStatus = Array(checkedTextList.size) { false }
        content.setOnTouchListener { _, event ->
            Vog.v(" ---> ${event.x} ${event.y}")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startView = findViewByPos(checkedTextList,
                            event.x.toInt(), event.y.toInt())
                        ?: return@setOnTouchListener false
                    Vog.d("buildView ---> ACTION_DOWN")
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
                    Vog.d("buildView ---> ACTION_MOVE ONE")
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
            setTextColor(if (isChecked) Color.WHITE else Color.BLACK)
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
    private fun negateAll(checkedTextList: List<CheckedTextView>, backStatus: Array<Boolean>,
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
//            Vog.d("findViewByPos ---> ${it.text} ${rect2String(rect)}")
            if (rect.contains(x, y - 20))//错位
                return it.also {
                    Vog.d("findViewByPos --->  当前: ${it.text}")
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
    fun getCheckedText(): String = buildString {
        checkedTextList.forEach {
            if (it.isChecked) {
                append(it.text)
            }
        }
    }

    private fun backupOldCheckStatus(list: List<CheckedTextView>): Array<Boolean> {
        return buildList<Boolean> {
            list.forEach {
                add(it.isChecked)
            }
        }.toTypedArray()
    }


}