package cn.vove7.jarvis.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.CheckedTextView
import android.widget.RelativeLayout
import android.widget.TextView
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.view.finder.ScreenTextFinder

import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.dialog.WordSplitDialog
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithText
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import java.util.concurrent.CountDownLatch

/**
 * # ScreenPickerActivity
 *
 * @author Administrator
 * 2018/10/14
 */
class ScreenPickerActivity : Activity() {

    private val viewNodeList = mutableListOf<Model>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AccessibilityApi.isBaseServiceOn) {
            GlobalApp.toastWarning("无障碍未开启，无法获取屏幕内容")
            finish()
            return
        }

        if (!AppConfig.haveTextPickPermission()) {//免费次数
            GlobalApp.toastWarning("今日次数达到上限")
            finish()
            return
        }
        if (unSupportPage.contains(AccessibilityApi.accessibilityService?.currentScope)) {
            MainService.instance?.speak("不支持当前页")
            finish()
            return
        }

        runOnNewHandlerThread {
            ScreenTextFinder(AccessibilityApi.accessibilityService?.rootInWindow!!)
                    .findAll().forEach { viewNodeList.add(Model(it)) }
            Vog.d("onCreate ---> 提取数量 ${viewNodeList.size}")


            if (viewNodeList.isEmpty()) {
                GlobalApp.toastInfo("未提取到任何内容")
                finish()
            } else runOnUi {
                buildContent()
                if (intent.hasExtra("t")) {
                    translateAll()
                } else {
                    showTips()
                }
            }
        }
    }

    /**
     * 不支持的页面
     */
    private val unSupportPage = hashSetOf(
            ActionScope("com.tencent.mtt", "com.tencent.mtt.MainActivity")
    )

    private fun showTips() {
        val sp = SpHelper(this, "tutorials")
        if (sp.containsKey(Tutorials.screen_translate_tips) && !BuildConfig.DEBUG) {
            return
        }
        sp.set(Tutorials.screen_translate_tips, true)

        val textView = TextView(this).apply {
            text = "点按[音量下键]可以开启屏幕翻译"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.RED)
            setBackgroundColor(Color.WHITE)
            setPadding(10, 10, 10, 10)
        }
        val p = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        p.topMargin = 20
        textView.setOnClickListener {
            textView.visibility = View.GONE
        }
        rootContent.addView(textView, p)

        Handler().postDelayed({
            textView.visibility = View.GONE
        }, 5000)
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
        Vog.d("状态栏高度 ---> $statusBarHeight1")
        statusBarHeight1
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Vog.d("onKeyDown ---> $keyCode")
        if (!hasT && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            translateAll()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private var hasT = false
    private fun translateAll() {
        if (!AppConfig.haveTranslatePermission()) {
            return
        }

        hasT = true
        GlobalApp.toastInfo("开始翻译")
        val count = CountDownLatch(viewNodeList.size)
        viewNodeList.forEach {
            val text = it.text
            runOnCachePool {
                val r = BaiduAipHelper.translate(text, to = AppConfig.translateLang)
                if (r != null) {
                    val res = r.transResult
                    if (text == res) {//文本相同
                        count.countDown()
                        return@runOnCachePool
                    }
                    it.subText = res
                    runOnUi {
                        it.textView?.isChecked = true
                        it.textView?.text = res
                    }
                } else {
                    it.subText = "翻译失败"
                }
                count.countDown()
            }
            runOnCachePool {
                //监听
                count.await()
                runOnUi {
                    GlobalApp.toastSuccess("翻译完成")
                }
            }
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!hasT && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            return true
        return super.onKeyUp(keyCode, event)
    }

    private val rootContent by lazy { RelativeLayout(this) }

    private fun buildContent() {
        rootContent.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        rootContent.setBackgroundColor(resources.getColor(android.R.color.transparent))
        viewNodeList.forEach { model ->
            val it = model.viewNode
            //            Vog.d("---> ${it.getBounds()} ${it.getText()}")
            val view = CheckedTextView(this).apply {
                setBackgroundResource(R.drawable.bg_screen_text_high_light)
                val rect = it.getBounds()
                gravity = Gravity.TOP
                setTextColor(0xFFFFFF)
                textSize = 15f
                isVerticalScrollBarEnabled = true
                setOnClickListener {
                    onItemClick.invoke(model)
                }
                setPadding(10, 0, 10, 0)
                layoutParams = RelativeLayout.LayoutParams(
                        (rect.right - rect.left) + 10, (rect.bottom - rect.top) + 10).also {
                    it.setMargins(rect.left - 5, rect.top - statusbarHeight - 5, 0, 0)
                }
            }
            model.textView = view
            rootContent.addView(view)
        }
        setContentView(rootContent)
    }

    private fun showSplitWordDialog(s: String) {
        sd = WordSplitDialog(this, s)
        sd?.show()
    }

    override fun onStop() {
        super.onStop()
        d?.dismiss()
        sd?.dismiss()
        finish()
        Vog.d("onStop ---> ")
    }

    var sd: WordSplitDialog? = null

    var d: BottomDialogWithText? = null
    private val onItemClick: (Model) -> Unit = { model ->
        val text = model.text
        Vog.d(" ---> $text")
        d = BottomDialogWithText(this, "文字操作").apply d@{
            noAutoDismiss()
            positiveButton(text = "复制原文") {
                SystemBridge.setClipText(text)
                GlobalApp.toastInfo(R.string.text_copied)
            }
            neutralButton(text = "分词") {
                showSplitWordDialog(text)
                dismiss()
            }
            if (model.subText == null)
                negativeButton(text = "翻译") {
                    if (!AppConfig.haveTranslatePermission())
                        return@negativeButton
                    runOnCachePool {
                        textView.apply {
                            appendlnGreen("\n翻译中...")
                            val r = BaiduAipHelper.translate(text, to = AppConfig.translateLang)
                            if (r != null) {
                                model.subText = r.transResult
                                clear()
                                appendln(text)
                                appendlnRed("\n翻译结果：")
                                appendln(model.subText)
                                setCopyTranslationText(this@d, r.transResult)
                            } else appendlnRed("翻译失败")
                        }
                    }
                }
            textView.apply {
                setPadding(50, 20, 50, 20)
                appendln(text)
                model.subText?.also {
                    appendlnRed("\n翻译结果：")
                    appendln(it)
                    setCopyTranslationText(this@d,it)
                }
            }
            show()
        }
    }

    /**
     * 若翻译过 ，显示 复制原文
     */
    private fun setCopyTranslationText(bd:BottomDialogWithText, trans:String) {
        runOnUi {
            bd.negativeButton(text = "复制翻译") {
                SystemBridge.setClipText(trans)
                GlobalApp.toastInfo(R.string.text_copied)
            }
        }
    }

    class Model(
            val viewNode: ViewNode,
            var textView: CheckedTextView? = null,
            var subText: String? = null
    ) {
        val text: String get() = viewNode.getText() ?: viewNode.desc() ?: ""
    }

}