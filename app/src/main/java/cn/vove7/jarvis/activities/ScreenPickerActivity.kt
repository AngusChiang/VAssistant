package cn.vove7.jarvis.activities

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.view.finder.ScreenTextFinder
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # ScreenPickerActivity
 *
 * @author Administrator
 * 2018/10/14
 */
class ScreenPickerActivity : Activity() {
    val toast: ColorfulToast by lazy { ColorfulToast(this) }
    private lateinit var textViewList: List<ViewNode>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AccessibilityApi.isOpen()) {
            toast.showLong("无障碍未开启，无法获取屏幕内容")
            finish()
            return
        }

        textViewList = ScreenTextFinder(AccessibilityApi.accessibilityService!!)
                .findAll().toList()

        buildContent()
    }

    private val statusbarHeight: Int by lazy {
        var statusBarHeight1 = -1
        //获取status_bar_height资源的ID
        val resourceId = resources.getIdentifier("status_bar_height",
                "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = resources.getDimensionPixelSize(resourceId)
        }
        Vog.d(this, "状态栏高度 ---> $statusBarHeight1")
        statusBarHeight1
    }

    private fun buildContent() {
        val rootContent = RelativeLayout(this)
        rootContent.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        rootContent.setBackgroundColor(resources.getColor(android.R.color.transparent))

        textViewList.forEach { it ->
            //            Vog.d(this, "---> ${it.getBounds()} ${it.getText()}")
            val view = View(this).apply {
                setBackgroundResource(R.drawable.bg_screen_text_high_light)
                val rect = it.getBounds()

                setOnClickListener { _ ->
                    onItemClick.invoke(it)
                }
                setPadding(10, 5, 10, 5)
                layoutParams = RelativeLayout.LayoutParams(
                        (rect.right - rect.left) + 20, (rect.bottom - rect.top) + 20).also {
                    it.setMargins(rect.left - 10, rect.top - statusbarHeight - 10, 0, 0)
                }
            }
            rootContent.addView(view)
        }

        setContentView(rootContent)
    }

    override fun onStop() {
        super.onStop()
        onBackPressed()
        finish()
        onDestroy()
        Vog.d(this, "onStop ---> ")
    }

    override fun finish() {
        d?.dismiss()
        super.finish()
    }

    var d: MaterialDialog? = null
    private val onItemClick: (ViewNode) -> Unit = { node ->
        //        val checkedTextList = mutableListOf<CheckedTextView>()
        val text = node.getText() ?: "失败"
        val textView = TextView(this)
        textView.setPadding(60, 0, 60, 0)
        textView.setTextIsSelectable(true)
        textView.text = "$text\n\n可长按选择"
//        toast.showShort("分词中")
        d = MaterialDialog(this).title(text = "选择")
                .noAutoDismiss()
                .customView(view = textView/*R.layout.dialog_pick_text*/, scrollable = true)
                .positiveButton(text = "复制原文") {
                    SystemBridge.setClipText(text)
                    toast.showShort(R.string.text_copied)
                    it.dismiss()
                }
                .negativeButton(text = "快速搜索") {
                    //                    val st = getCheckedText(checkedTextList)
//                    val s = if (st.isEmpty()) text
//                    else st
                    val selT = textView.selectionStart.let { b ->
                        if (b < 0) text
                        else text.substring(b, textView.selectionEnd)
                    }
                    Vog.d(this, "selT ---> $selT")
                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
                    intent.putExtra(SearchManager.QUERY, selT)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    it.dismiss()
                }
//                .positiveButton(text = "复制全部") {
//                    //                    val st = getCheckedText(checkedTextList)
////                    if (st.isEmpty()) {
////                        toast.showShort(R.string.text_select_nothing)
////                        return@positiveButton
////                    }
//                    SystemBridge.setClipText(text)
//                    toast.showShort(R.string.text_copied)
//                    it.dismiss()
//                }
                .show {
                    //                                        thread {
//                        val wordList = BaiduAipHelper.lexer(node.getText() ?: "失败")
//                        if (wordList == null) {
//                            toast.showLong("分词失败")
//                        } else {
//                            runOnUiThread {
//                                val content = findViewById<WarpLinearLayout>(R.id.words_content)
//                                content.setOnTouchListener { v, event ->
//                                    Vog.d(this, " ---> $event")
//
//                                    when (event.action) {
//                                        ACTION_DOWN -> {
//                                            val item = findViewByPos(checkedTextList,
//                                                    event.rawX.toInt(), event.rawY.toInt())
//                                                ?: return@setOnTouchListener false
//                                            Vog.d(this, "buildView ---> ACTION_DOWN")
//                                            return@setOnTouchListener true
//                                        }
//                                        ACTION_UP -> {
//                                            Vog.d(this, "buildView ---> ACTION_UP")
//                                            return@setOnTouchListener true
//                                        }
//                                        ACTION_MOVE -> {
//                                            Vog.d(this, "buildView ---> ACTION_MOVE")
//                                            return@setOnTouchListener true
//                                        }
//                                        else -> {
//                                        }
//                                    }
//                                    return@setOnTouchListener false
//                                }
//
//                                wordList.withIndex().forEach { kv ->
//                                    val v = buildView(kv.index, kv.value)
//                                    content.addView(v)
//                                    checkedTextList.add(v)
//                                }
//                            }
//                        }
//                    }
                }
    }

    private fun buildView(index: Int, it: String): CheckedTextView {
        Vog.d(this, " ---> $it")
        val v = CheckedTextView(this@ScreenPickerActivity)
        v.setBackgroundResource(R.drawable.pickable_bg)
        v.text = it
        v.setPadding(20, 10, 20, 10)
        v.layoutParams = getPa()
        v.textSize = 15f
        v.setTextColor(Color.WHITE)
        v.isChecked = false
//        v.setOnClickListener { _ ->
//            v.toggle()
//        }
        v.tag = index
        return v
    }

    private fun findViewByPos(list: List<CheckedTextView>, x: Int, y: Int): CheckedTextView? {
        Vog.d(this, "findViewByPos ---> $x $y")
        list.forEach {
            val rect = Rect()
            it.getGlobalVisibleRect(rect)
            Vog.d(this, "findViewByPos ---> ${it.text} $rect")
            if (rect.contains(x, y))
                return it.also { _ ->
                    Vog.d(this, "findViewByPos --->  当前: ${it.text}")
                }
        }
        return null
    }


    private fun getPa(): LinearLayout.LayoutParams {
        val l = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        l.setMargins(10, 10, 10, 10)
        return l
    }

    private fun getCheckedText(list: List<CheckedTextView>): String {
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
}