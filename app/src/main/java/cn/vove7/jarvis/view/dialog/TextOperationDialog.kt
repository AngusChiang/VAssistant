package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.widget.PopupMenu
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.ThreadPool
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithText

/**
 * # TextOperationDialog
 * 文本操作对话框
 *
 * 分词 复制 更多(分享,编辑,复制翻译)
 * @author Vove
 * 2019/6/7
 */
class TextOperationDialog(val context: Context, val textModel: TextModel) {

    val d = BottomDialogWithText(context, "文字操作").apply d@{
        noAutoDismiss()
        positiveButton(text = "复制") {
            SystemBridge.setClipText(textModel.text)
            GlobalApp.toastInfo(R.string.text_copied)
        }
        neutralButton(text = "分词") {
            WordSplitDialog(context, textModel.text).show()
            dismiss()
        }
        negativeButton(text = "更多") {
            showMoreMenu()
        }

        textView.apply {
            setPadding(50, 20, 50, 20)
            appendln(textModel.text)
            textModel.subText?.also {
                appendlnRed("\n翻译结果：")
                appendln(it)
            }
        }
        show()
    }

    private fun translate() {
        AppConfig.haveTranslatePermission() ?: return
        ThreadPool.runOnCachePool {
            d.textView.apply {
                appendlnGreen("\n翻译中...")
                val r = BaiduAipHelper.translate(textModel.text, to = AppConfig.translateLang)
                if (r != null) {
                    textModel.subText = r.transResult
                    clear()
                    appendln(text)
                    appendlnRed("\n翻译结果：")
                    appendln(textModel.subText)
                } else appendlnRed("翻译失败")
            }
        }
    }

    private fun showMoreMenu() {
        PopupMenu(context, d.buttonNegative).apply {
            menu.add("分享")
            menu.add("编辑")
            menu.add("搜索")
            if (textModel.subText != null) {
                menu.add("复制翻译")
            } else {
                menu.add("翻译")
            }
            setOnMenuItemClickListener {
                when (it.title) {
                    "分享" -> SystemBridge.shareText(textModel.text)
                    "编辑" -> {
                        TextEditorDialog(context, textModel.text)
                        d.dismiss()
                    }
                    "翻译" -> translate()
                    "复制翻译" -> {
                        SystemBridge.setClipText(textModel.subText)
                        GlobalApp.toastInfo(R.string.text_copied)
                    }
                    "搜索" -> {
                        SystemBridge.quickSearch(textModel.text)
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