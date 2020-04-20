package cn.vove7.jarvis.view.dialog

import android.widget.PopupMenu
import android.widget.TextView
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.*
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.utils.broadcastImageFile
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.tools.DataCollector
import cn.vove7.jarvis.tools.QRTools
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.dialog.contentbuilder.ImageContentBuilder
import cn.vove7.jarvis.view.dialog.contentbuilder.WrappedTextContentBuilder
import cn.vove7.jarvis.view.positiveButtonWithColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * # TextOperationDialog
 * 文本操作对话框
 *
 * 分词 复制 更多(分享,编辑,复制翻译)
 * @author Vove
 * 2019/6/7
 */
class TextOperationDialog(
        val activity: BaseActivity,
        val textModel: TextModel
) {

    val lifeCycleScope = activity.lifecycleScope

    //默认已换行
    private var wraped = true

    private val cv = WrappedTextContentBuilder(textModel.text)

    val bottomDialog = BottomDialog.builder(activity) {
        awesomeHeader("文字操作")
        peekHeightProportion = 0.6f
        content(cv)
        buttons {
            positiveButtonWithColor("复制") {
                SystemBridge.setClipText(getOpText())
                GlobalApp.toastInfo(R.string.text_copied)
            }

            neutralButton(text = "分词") {
                DataCollector.buriedPoint("to_split_words")
                WordSplitDialog(this@TextOperationDialog.activity, getOpText())
                it.dismiss()
            }
            negativeButton(text = "更多") {
                showMoreMenu()
            }
        }

    }

    //待操作文字
    private fun getOpText(): String = (bottomDialog.contentBuilder as WrappedTextContentBuilder).textView.text.toString()

    init {
        bottomDialog.updateContent<WrappedTextContentBuilder> {
            textView.apply {
                appendln(textModel.text)
            }
        }
    }


    private fun translate() {
        DataCollector.buriedPoint("to_trans")
        AppConfig.haveTranslatePermission() ?: return
        val dialog = BottomDialog.builder(activity) {
            title(round = true, title = "翻译")
            message(textModel.subText ?: "", true)
            buttons {
                positiveButton("复制") {
                    it.updateContent<MessageContentBuilder> {
                        SystemBridge.setClipText(text)
                        GlobalApp.toastInfo(R.string.text_copied)
                    }
                }
                negativeButton("关闭") { it.dismiss() }
            }
        }
        if (textModel.subText != null) {
            return
        }
        lifeCycleScope.launch(Dispatchers.IO) {
            val r = BaiduAipHelper.translate(getOpText(), to = AppConfig.translateLang)
            withContext(Dispatchers.Main) {
                dialog.updateContent<MessageContentBuilder> {
                    text = if (r != null) {
                        textModel.subText = r.transResult
                        r.transResult
                    } else {
                        "翻译失败"
                    }
                }
            }
        }
    }

    private fun showMoreMenu() {
        var b: TextView? = null
        bottomDialog.updateFooter<ButtonsBuilder> { b = buttonNegative }
        PopupMenu(activity, b).apply {
            if (wraped) {
                menu.add("取消换行")
            } else {
                menu.add("自动换行")
            }
            menu.add("分享")
            menu.add("搜索")
            menu.add("翻译")
            menu.add("生成二维码")
            setOnMenuItemClickListener {
                when (it.title) {
                    "取消换行" -> {
                        wraped = false
                        bottomDialog.updateContent<WrappedTextContentBuilder> {
                            textModel.text = getOpText()
                            text = textModel.text.lines().joinToString("")
                        }
                    }
                    "自动换行" -> {
                        wraped = true
                        bottomDialog.updateContent<WrappedTextContentBuilder> {
                            text = textModel.text.toString()
                        }
                    }
                    "分享" -> SystemBridge.shareText(getOpText())
                    "翻译" -> translate()
                    "搜索" -> {
                        DataCollector.buriedPoint("to_search")
                        SystemBridge.quickSearch(getOpText())
                    }
                    "生成二维码" -> {
                        DataCollector.buriedPoint("to_gen_qr")
                        QRTools.encode(getOpText()) { path, e ->
                            if (path != null) {
                                bottomDialog.dismiss()
                                runOnUi {
                                    showQrDialog(getOpText(), path)
                                }
                            } else {
                                GlobalApp.toastError("生成失败\n${e?.message}")
                            }
                        }
                    }
                }
                true
            }
            show()
        }
    }

    private fun showQrDialog(content: String, path: String) {
        BottomDialog.builder(activity) {
            title("二维码", true)
            withCloseIcon()
            content(ImageContentBuilder()) {
                init {
                    loadFile(File(path))
                }
            }
            buttons {
                positiveButtonWithColor("分享") {
                    it.dismiss()
                    SystemBridge.shareImage(path)
                }
                negativeButton("保存") {
                    val targetFile = File(StorageHelper.picturesPath, System.currentTimeMillis().toString() + ".jpg")
                    try {
                        File(path).copyTo(targetFile, true)
                        it.dismiss()
                        GlobalApp.toastInfo("已保存到：${targetFile.absolutePath}")
                        targetFile.broadcastImageFile()
                    } catch (e: Exception) {
                        GlobalApp.toastError("保存失败：${e.message}")
                    }
                }
                neutralButton("") {}
            }
        }
    }

    class TextModel(
            var text: CharSequence,
            var subText: String? = null
    )
}