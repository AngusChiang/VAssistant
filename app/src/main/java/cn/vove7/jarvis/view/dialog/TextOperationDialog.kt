package cn.vove7.jarvis.view.dialog

import android.app.Activity
import android.widget.PopupMenu
import android.widget.TextView
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.ButtonsBuilder
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.builder.title
import cn.vove7.bottomdialog.builder.withCloseIcon
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.*
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.DataCollector
import cn.vove7.jarvis.tools.QRTools
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.dialog.contentbuilder.ImageContentBuilder
import cn.vove7.jarvis.view.dialog.contentbuilder.WrappedTextContentBuilder
import java.io.File

/**
 * # TextOperationDialog
 * 文本操作对话框
 *
 * 分词 复制 更多(分享,编辑,复制翻译)
 * @author Vove
 * 2019/6/7
 */
class TextOperationDialog(val activity: Activity, val textModel: TextModel) {

    val bottomDialog = BottomDialog.builder(activity) {

        awesomeHeader("文字操作")
        buttons {

            positiveButton(text = "复制") {
                SystemBridge.setClipText(textModel.text)
                GlobalApp.toastInfo(R.string.text_copied)
            }

            neutralButton(text = "分词") {
                DataCollector.buriedPoint("to_split_words")
                WordSplitDialog(this@TextOperationDialog.activity, textModel.text)
                it.dismiss()
            }
            negativeButton(text = "更多") {
                showMoreMenu()
            }
        }

        content(WrappedTextContentBuilder(""))
    }

    init {
        bottomDialog.updateContent<WrappedTextContentBuilder> {
            textView.apply {
                appendln(textModel.text)
                textModel.subText?.also {
                    appendlnRed("\n翻译结果：")
                    appendln(it)
                }
            }
        }
    }


    private fun translate() {
        DataCollector.buriedPoint("to_trans")
        AppConfig.haveTranslatePermission() ?: return
        ThreadPool.runOnCachePool {
            bottomDialog.updateContent<WrappedTextContentBuilder> {
                textView.apply {
                    appendlnGreen("\n翻译中...")
                    val r = BaiduAipHelper.translate(textModel.text, to = AppConfig.translateLang)
                    if (r != null) {
                        textModel.subText = r.transResult
                        set(text)
                        appendlnRed("\n翻译结果：")
                        appendln(textModel.subText)
                    } else appendlnRed("翻译失败")
                }
            }
        }
    }

    private fun showMoreMenu() {
        var b: TextView? = null
        bottomDialog.updateFooter<ButtonsBuilder> { b = buttonNegative }
        PopupMenu(activity, b).apply {
            menu.add("分享")
            menu.add("编辑")
            menu.add("搜索")
            if (textModel.subText != null) {
                menu.add("复制翻译")
            } else {
                menu.add("翻译")
            }
            menu.add("生成二维码")
            setOnMenuItemClickListener {
                when (it.title) {
                    "分享" -> SystemBridge.shareText(textModel.text)
                    "编辑" -> {
                        TextEditorDialog(activity, textModel.text) {
                            title(text = "编辑")
                            positiveButton(text = "复制") {
                                SystemBridge.setClipText(editorView.content())
                            }
                            negativeButton(text = "分享") {
                                SystemBridge.shareText(editorView.content())
                            }
                            neutralButton(text = "完成") {
                                TextOperationDialog(activity,
                                        TextModel(editorView.content()))
                                dismiss()
                            }
                        }
                        bottomDialog.dismiss()
                    }
                    "翻译" -> translate()
                    "复制翻译" -> {
                        SystemBridge.setClipText(textModel.subText)
                        GlobalApp.toastInfo(R.string.text_copied)
                    }
                    "搜索" -> {
                        DataCollector.buriedPoint("to_search")
                        SystemBridge.quickSearch(textModel.text)
                    }
                    "生成二维码" -> {
                        DataCollector.buriedPoint("to_gen_qr")
                        QRTools.encode(textModel.text) { path, e ->
                            if (path != null) {
                                bottomDialog.dismiss()
                                runOnUi {
                                    showQrDialog(textModel.text, path)
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
                positiveButton("分享") {
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
            val text: String,
            var subText: String? = null
    )
}