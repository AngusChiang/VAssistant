package cn.vove7.jarvis.view.dialog

import android.app.Activity
import android.widget.PopupMenu
import android.widget.TextView
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.ButtonsBuilder
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.builder.title
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.ThreadPool
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.dialog.contentbuilder.WrappedTextContentBuilder

/**
 * # TextOperationDialog
 * 文本操作对话框
 *
 * 分词 复制 更多(分享,编辑,复制翻译)
 * @author Vove
 * 2019/6/7
 */
class TextOperationDialog(val context: Activity, val textModel: TextModel) {

    val bottomDialog
            = BottomDialog.builder(context) {

        title("文字操作")
        buttons {

            positiveButton(text = "复制") {
                SystemBridge.setClipText(textModel.text)
                GlobalApp.toastInfo(R.string.text_copied)
            }

            neutralButton(text = "分词") {
                WordSplitDialog(this@TextOperationDialog.context, textModel.text)
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
        PopupMenu(context, b).apply {
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
                        TextEditorDialog(context, textModel.text)
                        bottomDialog.dismiss()
                    }
                    "翻译" -> translate()
                    "复制翻译" -> {
                        SystemBridge.setClipText(textModel.subText)
                        GlobalApp.toastInfo(R.string.text_copied)
                    }
                    "搜索" -> {
                        SystemBridge.quickSearch(textModel.text)
                    }
                    "生成二维码" -> {
                        //TODO
                        GlobalApp.toastInfo(R.string.text_coming_soon)
                    }
                }
                true
            }
            show()
        }
    }

    class TextModel(
            val text: String,
            var subText: String? = null
    )
}