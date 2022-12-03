package cn.vove7.jarvis.activities

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.CoroutineExt.withMain
import cn.vove7.common.utils.fadeIn
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.startActivity
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.databinding.ActivityTextOcrBinding
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.view.dialog.TextOperationDialog
import cn.vove7.jarvis.view.dp
import cn.vove7.vtp.asset.AssetHelper
import cn.vove7.vtp.log.Vog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.*

/**
 * # TextOcrActivity
 *
 * @author 11324
 * 2019/3/11
 */
class TextOcrActivity : BaseActivity<ActivityTextOcrBinding>() {
    override val darkTheme: Int
        get() = R.style.TextOcr_Dark

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
        window.setWindowAnimations(R.style.fade)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        intent?.let {
            if (it.hasExtra("items")) {
                copy(it.getSerializableExtra("items") as List<TextOcrItem>)
                buildContent()
            } else {//请求截屏
                ocrWithScreenShot()
            }
        } ?: ocrWithScreenShot()

    }

    @Synchronized
    private fun copy(list: List<TextOcrItem>) {
        wordItems.clear()
        wordItems.addAll(list.map { Model(it) })
    }

    private fun ocrWithScreenShot() {
        lifecycleScope.launch {
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

    private var lastCheckedCount = 0
    private val onItemClick: (Model) -> Unit = { model ->
        if (lastCheckedCount == 0 && model.textView?.isSelected == true && wordItems.none { it.textView?.isSelected == true && it != model }) {
            editDialog(model.item.text, model.item.subText)
        }
    }

    private fun buildContent() {
        viewBinding.loadingLayout.gone()
        val framePx = 0f.dp.px
        wordItems.forEach { model ->
            val item = model.item

            val h = item.height
            val t = item.top
            if (t + h <= statusbarHeight) {
                return@forEach
            }
            val top = t - statusbarHeight

            TextView(this).apply {
                setBackgroundResource(R.drawable.bg_screen_text_high_light_no_radius)
                Vog.d("buildContent ---> $item")
                gravity = Gravity.CENTER
                val w = item.width
                val left = item.left
                val rotationAngle = item.rotationAngle
                Vog.d("buildContent ---> ${item.text} top:$top ,left:$left ,w:$w h:$h rotationAngle:$rotationAngle")

                layoutParams = RelativeLayout.LayoutParams(w + 2 * framePx, h + 2 * framePx).also {
                    it.setMargins(left - framePx / 2, top - framePx / 2, 0, 0)
                }
                if (rotationAngle != 0.0f) {
                    rotationX = 0.5f
                    rotationY = 0.5f
                    rotation = rotationAngle
                }
                model.textView = this
                viewBinding.rootContent.addView(this)
            }

        }

        viewBinding.floatEditIcon.setOnClickListener {
            editCheckedText()
        }
        viewBinding.floatEditIcon.setOnLongClickListener {
            val allStatus = wordItems.all { it.textView?.isSelected == false }
            wordItems.forEach {
                it.textView?.isSelected = allStatus
            }
            true
        }
        viewBinding.rootContent.onStartMove = {
            viewBinding.floatEditIcon.gone()
        }
        viewBinding.rootContent.onTouchUp = { hasResult ->
            if (hasResult) {//一个时 弹出编辑
                wordItems.find { it.textView?.isSelected ?: false }?.also {
                    onItemClick(it)
                }
            }
            lastCheckedCount = wordItems.count { it.textView?.isSelected == true }
            if (viewBinding.floatEditIcon.isOrWillBeHidden) {
                viewBinding.floatEditIcon.fadeIn(200)
            }
        }
        if (intent.hasExtra("t")) {
            translateAll()
        } else {
            Tutorials.showForView(this, Tutorials.screen_assistant_ocr, viewBinding.floatEditIcon, "", AssetHelper.getStrFromAsset(this, "files/ocr_help.md"))
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

    private var hasT = false
    private fun translateAll() {
        AppConfig.haveTranslatePermission() ?: return

        hasT = true
        GlobalApp.toastInfo("开始翻译")

        lifecycleScope.launch(Dispatchers.IO) {
            val defs = wordItems.map {
                val text = it.item.text
                async {
                    val r = BaiduAipHelper.translate(text.toString(), to = AppConfig.translateLang)
                    if (r != null) {
                        val res = r.transResult
                        it.item.subText = res
                        withMain {
                            it.textView?.isSelected = true
                        }
                    } else {
                        it.item.subText = "翻译失败"
                    }
                }
            }
            defs.joinAll()
            //监听
            if (isFinishing) return@launch
            withMain {
                GlobalApp.toastSuccess("翻译完成")
            }
        }

    }


    /**
     * 勾选的文字
     */
    private val checkedText
        get() = buildString {
            wordItems.forEach {
                if (it.textView?.isSelected == true) {
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
        val resourceId = resources!!.getIdentifier("status_bar_height",
                "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = resources!!.getDimensionPixelSize(resourceId)
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
            var textView: TextView? = null
    )

}
