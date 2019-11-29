package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.CheckedTextView
import android.widget.RelativeLayout
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.title
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.*
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.view.dialog.TextOperationDialog
import cn.vove7.jarvis.view.dialog.contentbuilder.MarkdownContentBuilder
import cn.vove7.vtp.asset.AssetHelper
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.activity_text_ocr.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * # TextOcrActivity
 *
 * @author 11324
 * 2019/3/11
 */
class TextOcrActivity : Activity() {
    private val wordItems = mutableListOf<Model>()

    companion object {
        fun start(act: Context, items: ArrayList<TextOcrItem>, bundle: Bundle? = null) {
            act.startActivity<TextOcrActivity> {
                if (bundle != null) {
                    putExtras(bundle)
                }
                putExtra("items", items)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

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

    override fun onBackPressed() {
        overridePendingTransition(0, 0)
        super.onBackPressed()
    }

    private val onItemClick: (Model) -> Unit = { model ->
        if (model.textView?.isChecked == true && wordItems.none { it.textView?.isChecked == true && it != model }) {
            editDialog(model.item.text, model.item.subText)
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
        GlobalScope.launch {
            //异步
            try {
                val path = SystemBridge.screen2File()?.absolutePath
                if (path == null) {
                    GlobalApp.toastError("截图失败")
                    finish()
                    return@launch
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
                text = item.text
                val w = item.width
                val left = item.left
                val rotationAngle = item.rotationAngle
                Vog.d("buildContent ---> ${item.text} top:$top ,left:$left ,w:$w h:$h rotationAngle:$rotationAngle")

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

        floatEditIcon.setOnClickListener {
            editCheckedText()
        }
        rootContent.setData(wordItems, onItemClick)
        rootContent.onStartMove = {
            floatEditIcon.gone()
        }
        rootContent.onTouchUp = {
            if (floatEditIcon.isOrWillBeHidden) {
                floatEditIcon.fadeIn(200)
            }
        }
        if (intent.hasExtra("t")) {
            translateAll()
        } else {
            Tutorials.showForView(this, Tutorials.screen_assistant_ocr, floatEditIcon, "", AssetHelper.getStrFromAsset(this, "files/ocr_help.md"))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Vog.d("onKeyDown ---> $keyCode")
        if (!hasT && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            translateAll()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!hasT && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            return true
        return super.onKeyUp(keyCode, event)
    }

    var hasT = false
    private fun translateAll() {
        AppConfig.haveTranslatePermission() ?: return

        hasT = true
        GlobalApp.toastInfo("开始翻译")
        val count = CountDownLatch(wordItems.size)
        wordItems.forEach {
            val text = it.item.text
            ThreadPool.runOnCachePool {
                val r = BaiduAipHelper.translate(text.toString(), to = AppConfig.translateLang)
                if (r != null) {
                    val res = r.transResult
                    if (text == res) {//文本相同
                        count.countDown()
                        return@runOnCachePool
                    }
                    it.item.subText = res
                    runOnUi {
                        it.textView?.isChecked = true
                        it.textView?.text = res
                    }
                } else {
                    it.item.subText = "翻译失败"
                }
                count.countDown()
            }
            ThreadPool.runOnCachePool {
                //监听
                count.await()
                if (isFinishing) return@runOnCachePool
                runOnUi {
                    GlobalApp.toastSuccess("翻译完成")
                }
            }
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

    private fun editDialog(text: CharSequence, sub: String? = null) {
        d = TextOperationDialog(this, TextOperationDialog.TextModel(text, sub)).bottomDialog
    }

    class Model(
            val item: TextOcrItem,
            var textView: CheckedTextView? = null
    ) {

        operator fun contains(p: Point): Boolean {
            return abs(point2LineDis(p, 0, 3) + point2LineDis(p, 1, 2) - item.width) < 5 &&
                    abs(point2LineDis(p, 0, 1) + point2LineDis(p, 2, 3) - item.height) < 5
        }

        /**
         * 点到直线距离
         * A = y2 - y1
         * B = x1 -x2
         * C = y1(x2-x1) - x1(y2-y1)
         */
        @Suppress("LocalVariableName")
        private fun point2LineDis(p: Point, i1: Int, i2: Int): Int {
            val p1 = item.points[i1]
            val p2 = item.points[i2]
            val A = p2.y - p1.y
            val B = p1.x - p2.x
            val C = p1.y * (p2.x - p1.x) - p1.x * (p2.y - p1.y)
            return abs(
                    (A * p.x + B * p.y + C) / sqrt((A * A + B * B).toDouble())
            ).toInt()
        }
    }

}
