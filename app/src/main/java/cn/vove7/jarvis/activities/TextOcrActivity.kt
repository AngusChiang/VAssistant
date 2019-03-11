package cn.vove7.jarvis.activities

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckedTextView
import android.widget.RelativeLayout
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.ThreadPool
import cn.vove7.common.utils.runOnUi
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithText
import cn.vove7.vtp.log.Vog

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
                buildContent()
            } else {//请求截屏
                ocrWithScreenShot()
            }
        } ?: ocrWithScreenShot()

    }


    var d: BottomDialogWithText? = null
    private val onItemClick: (Model) -> Unit = { model ->
        val text = model.item.text
        Vog.d(this, " ---> $text")
        d = BottomDialogWithText(this, "文字操作").apply d@{
            noAutoDismiss()
            positiveButton(text = "复制原文") {
                SystemBridge.setClipText(text)
                GlobalApp.toastShort(R.string.text_copied)
            }
            if (model.subText == null)
                negativeButton(text = "翻译") {
                    if (!AppConfig.haveTranslatePermission())
                        return@negativeButton
                    ThreadPool.runOnCachePool {
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
                    setCopyTranslationText(this@d, it)
                }
            }
            show()
        }
    }

    /**
     * 若翻译过 ，显示 复制原文
     */
    private fun setCopyTranslationText(bd: BottomDialogWithText, trans: String) {
        runOnUi {
            bd.negativeButton(text = "复制翻译") {
                SystemBridge.setClipText(trans)
                GlobalApp.toastShort(R.string.text_copied)
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

    private fun ocrWithScreenShot() {
        try {
            val path = SystemBridge.screen2File()!!.absolutePath
            copy(BaiduAipHelper.ocr(path))
            buildContent()
        } catch (e: NullPointerException) {
            GlobalApp.toastShort("截屏失败")
            finish()
        } catch (e: Exception) {
            GlobalApp.toastShort(e.message!!)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        System.gc()
    }

    private val rootContent by lazy { RelativeLayout(this) }

    private fun buildContent() {
        rootContent.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
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
                        GlobalApp.toastShort(text.toString())
                    }.toggle()
                }
                setOnLongClickListener {
                    onItemClick.invoke(model)
                    true
                }
                text = item.text
                val h = item.height * 2 + 10
                val w = item.width * 2 + 10
                val top = item.top * 2 - statusbarHeight
                val left = item.left * 2
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
//            model.textView = view
            rootContent.addView(view)
        }
        setContentView(rootContent)

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

    class Model(
            val item: TextOcrItem,
            var textView: CheckedTextView? = null,
            var subText: String? = null
    )

}