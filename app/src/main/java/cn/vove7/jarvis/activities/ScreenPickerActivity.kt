package cn.vove7.jarvis.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.baiduaip.BaiduAipHelper
import cn.vove7.common.utils.LooperHelper
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.view.finder.ScreenTextFinder
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.jarvis.view.dialog.WordSplitDialog
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlin.concurrent.thread

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
                setPadding(10, 0, 10, 0)
                layoutParams = RelativeLayout.LayoutParams(
                        (rect.right - rect.left) + 20, (rect.bottom - rect.top) + 20).also {
                    it.setMargins(rect.left - 10, rect.top - statusbarHeight - 10, 0, 0)
                }
            }
            rootContent.addView(view)
        }
        setContentView(rootContent)
    }

    private fun showSplitWordDialog(s: String) {
        val p = ProgressDialog(this)
        thread {
            LooperHelper.prepareIfNeeded()
            val wordList = BaiduAipHelper.lexer(s)
            p.dismiss()
            if (wordList == null) {
                toast.showLong("分词失败")
            } else {
                runOnUi {
                    sd = WordSplitDialog(this, s, wordList).dialog
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        onBackPressed()
        finish()
        onDestroy()
        Vog.d(this, "onStop ---> ")
    }

    var sd: MaterialDialog? = null
    override fun finish() {
        d?.dismiss()
        sd?.dismiss()
        super.finish()
    }

    var d: MaterialDialog? = null
    private val onItemClick: (ViewNode) -> Unit = { node ->
        val text = node.getText() ?: "失败"
        val textView = TextView(this)
        textView.setPadding(60, 0, 60, 0)
        textView.setTextIsSelectable(true)
        textView.setTextColor(resources.getColor(R.color.primary_text))
        textView.text = text
//        toast.showShort("分词中")
        d = MaterialDialog(this).title(text = "选择")
                .noAutoDismiss()
                .customView(view = textView/*R.layout.dialog_pick_text*/, scrollable = true)
                .positiveButton(text = "复制原文") {
                    SystemBridge.setClipText(text)
                    toast.showShort(R.string.text_copied)
                    it.dismiss()
                }
                .negativeButton(text = "分享") {
                    val selT = textView.selectionStart.let { b ->
                        if (b == textView.selectionEnd) text
                        else text.substring(b, textView.selectionEnd)
                    }
                    Vog.d(this, "selT ---> $selT")
                    SystemBridge.shareText(selT)
                }
                .neutralButton(text = "分词") {
                    showSplitWordDialog(text)
                    it.dismiss()
                }
                .show {}
    }


}