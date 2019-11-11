package cn.vove7.jarvis.activities

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckedTextView
import android.widget.RelativeLayout
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.title
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.view.dialog.TextOperationDialog
import cn.vove7.jarvis.view.dialog.contentbuilder.MarkdownContentBuilder
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


    private val onItemClick: (Model) -> Unit = { model ->
        val text = model.item.text
        editDialog(text)
    }

    @Synchronized
    private fun copy(list: List<TextOcrItem>) {
        wordItems.clear()
        list.forEach { item ->
            wordItems.add(Model(item))
        }
    }

    private fun ocrWithScreenShot() {
        runOnNewHandlerThread {
            //异步
            try {
                val path = SystemBridge.screen2File()?.absolutePath
                if (path == null) {
                    GlobalApp.toastError("截图失败")
                    finish()
                    return@runOnNewHandlerThread
                }
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
        finish()
        System.gc()
    }


    private fun buildContent() {
        loading_layout.gone()
//        rootContent.layoutParams = RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        wordItems.forEach { model ->
            val item = model.item

            val h = item.height
            val t = item.top
            if (t + h <= statusbarHeight) {
                return@forEach
            }
            val top = t - statusbarHeight

            CheckedTextView(this).apply {
                setBackgroundResource(R.drawable.bg_screen_text_high_light_no_radius)
                Vog.d("buildContent ---> $item")
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
                val w = item.width
                val left = item.left
                val rotationAngle = item.rotationAngle
                Vog.d("buildContent ---> ${item.text} top:$top ,left:$left ,w:$w h:$h rotationAngle:$rotationAngle")
//                setPadding(10, 0, 10, 0)

                layoutParams = RelativeLayout.LayoutParams(w, h).also {
                    it.setMargins(left, top, 0, 0)
                }
                rotationX = 0.5f
                rotationY = 0.5f
                rotation = rotationAngle
                model.textView = this
                rootContent.addView(this)
            }

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
        BottomDialog.builder(this) {
            title("文字识别界面帮助")
            content(MarkdownContentBuilder()) {
                loadMarkdownFromAsset("files/ocr_help.md")
            }
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
        Vog.d("状态栏高度 ---> $statusBarHeight1")
        statusBarHeight1
    }

    override fun onDestroy() {
        super.onDestroy()
        d?.dismiss()
    }

    var d: BottomDialog? = null


    private fun editDialog(text: String) {
        d = TextOperationDialog(this, TextOperationDialog.TextModel(text)).bottomDialog
    }

    class Model(
            val item: TextOcrItem,
            var textView: CheckedTextView? = null
    )

}
