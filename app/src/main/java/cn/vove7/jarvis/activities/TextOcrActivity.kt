package cn.vove7.jarvis.activities

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckedTextView
import android.widget.RelativeLayout
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.ThreadPool
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.view.dialog.TextEditorDialog
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithMarkdown
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithText
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.activity_text_ocr.*

/**
 * # TextOcrActivity
 *
 * @author 11324
 * 2019/3/11
 */
class TextOcrActivity : Activity() {
    private val wordItems = mutableListOf<Model>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.let {
            if (it.hasExtra("items")) {
                copy(it.getSerializableExtra("items") as List<TextOcrItem>)
                setContentView(R.layout.activity_text_ocr)
                buildContent()
            } else {//请求截屏
                ocrWithScreenShot()
            }
        } ?: ocrWithScreenShot()

    }


    var d: BottomDialogWithText? = null
    private val onItemClick: (Model) -> Unit = { model ->
        val text = model.item.text
        editDialog(text)
    }

    /**
     * 若翻译过 ，显示 复制原文
     */
    private fun setCopyTranslationText(bd: BottomDialogWithText, trans: String) {
        runOnUi {
            bd.negativeButton(text = "复制翻译") {
                SystemBridge.setClipText(trans)
                GlobalApp.toastInfo(R.string.text_copied)
            }
        }
    }

    @Synchronized
    private fun copy(list: List<TextOcrItem>) {
        wordItems.clear()
        list.forEach { item ->
            wordItems.add(Model(item))
        }
    }

    var p = 2 //倍率
    private fun ocrWithScreenShot() {
        p = 1
        runOnNewHandlerThread {
            //异步
            try {
                val path = SystemBridge.screen2File()!!.absolutePath
                runOnUi {
                    setContentView(R.layout.activity_text_ocr)
                }
                copy(BaiduAipHelper.ocr(path))
                runOnUi {
                    buildContent()
                }
            } catch (e: NullPointerException) {
                GlobalApp.toastError("截屏失败")
                finish()
            } catch (e: Exception) {
                GlobalApp.toastError(e.message!!)
                finish()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        System.gc()
    }


    private fun buildContent() {
        loading_layout.gone()
//        rootContent.layoutParams = RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        wordItems.forEach { model ->
            val item = model.item
            val view = CheckedTextView(this).apply {
                setBackgroundResource(R.drawable.bg_screen_text_high_light)
                Vog.d(this, "buildContent ---> $item")
                gravity = Gravity.TOP
                setTextColor(0xFFFFFF)
                textSize = 15f
                isVerticalScrollBarEnabled = true
                setOnClickListener {
                    (it as CheckedTextView).apply {
                    }.toggle()
                }
                setOnLongClickListener {
                    onItemClick.invoke(model)
                    true
                }
                text = item.text
                val h = item.height * p + 10
                val w = item.width * p + 10
                val top = item.top * p - statusbarHeight
                val left = item.left * p
                val rotationAngle = item.rotationAngle
                Vog.d(this, "buildContent ---> ${item.text} top:$top ,left:$left ,w:$w h:$h rotationAngle:$rotationAngle")
                setPadding(10, 0, 10, 0)

                layoutParams = RelativeLayout.LayoutParams(w, h).also {
                    it.setMargins(left, top, 0, 0)
                }
                rotationX = 0.5f
                rotationY = 0.5f
                rotation = rotationAngle
                model.textView = this
            }

            rootContent.addView(view)
        }

        helpIcon.setOnClickListener {
            showHelp()
        }

        floatEditIcon.setOnClickListener {
            editCheckedText()
        }

    }

    /**
     * 勾选的文字
     */
    private val checkedText
        get() = buildString {
            wordItems.forEach {
                if (it.textView?.isChecked == true) {
                    appendln(it.item.text)
                }
            }
        }.let {
            if (it.isEmpty()) buildString {
                wordItems.forEach { m ->
                    appendln(m.item.text)
                }
            } else it
        }

    private fun showHelp() {
        BottomDialogWithMarkdown(this, "文字识别界面帮助").apply {
            loadFromAsset("files/ocr_help.md")
            show()
        }
    }

    private fun editCheckedText() {
        editDialog(checkedText)
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

    private fun editDialog(text: String) {
        Vog.d(this, " ---> $text")
        d = BottomDialogWithText(this, "文字操作").apply d@{
            noAutoDismiss()
            positiveButton(text = "复制原文") {
                SystemBridge.setClipText(text)
                GlobalApp.toastInfo(R.string.text_copied)
            }
            neutralButton("编辑") {
                TextEditorDialog(this@TextOcrActivity, text)
                dismiss()
            }
            negativeButton(text = "翻译") {
                if (!AppConfig.haveTranslatePermission())
                    return@negativeButton
                ThreadPool.runOnCachePool {
                    textView.apply {
                        appendlnGreen("\n翻译中...")
                        val r = BaiduAipHelper.translate(text, to = AppConfig.translateLang)
                        if (r != null) {
                            clear()
                            appendln(text)
                            appendlnRed("\n翻译结果：")
                            appendln(r.transResult)
                            setCopyTranslationText(this@d, r.transResult)
                        } else appendlnRed("翻译失败")
                    }
                }
            }
            textView.apply {
                setPadding(50, 20, 50, 20)
                appendln(text)
            }
            show()
        }
    }

    class Model(
            val item: TextOcrItem,
            var textView: CheckedTextView? = null,
            var subText: String? = null
    )

}
