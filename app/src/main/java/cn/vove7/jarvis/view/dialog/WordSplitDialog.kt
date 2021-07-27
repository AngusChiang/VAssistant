package cn.vove7.jarvis.view.dialog

import androidx.appcompat.app.AppCompatActivity
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.builder.withCloseIcon
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.lifecycle.LifecycleScope
import cn.vove7.jarvis.view.dialog.contentbuilder.WordSplitBuilder
import cn.vove7.jarvis.view.dp
import cn.vove7.jarvis.view.positiveButtonWithColor

/**
 * # WordSplitDialog
 *  分词
 * @author Administrator
 * 2018/10/28
 */
class WordSplitDialog(
        context: AppCompatActivity,
        val rawWords: String
) {

    fun dismiss() {
        d.dismiss()
    }

    val d = BottomDialog.builder(context) {
        val builder = WordSplitBuilder(LifecycleScope(context.lifecycle), rawWords)
        peekHeight = 450.dp.px

        cancelable(false)
        withCloseIcon()
        awesomeHeader("分词")
        content(builder)

        buttons {
            positiveButtonWithColor("快速搜索") {
                val st = builder.getCheckedText()
                if (st.isEmpty()) {
                    GlobalApp.toastWarning(R.string.text_select_nothing)
                    return@positiveButtonWithColor
                }
                SystemBridge.quickSearch(st)
            }
            negativeButton(text = "复制") {
                val st = builder.getCheckedText()
                if (st.isEmpty()) {
                    GlobalApp.toastSuccess("已复制原文")
                    SystemBridge.setClipText(rawWords)
                    return@negativeButton
                }
                SystemBridge.setClipText(builder.getCheckedText())
                GlobalApp.toastSuccess("已复制选择内容")
            }
            neutralButton(text = "分享") {
                val st = builder.getCheckedText()
                if (st.isEmpty()) {
                    GlobalApp.toastWarning(R.string.text_select_nothing)
                    return@neutralButton
                }
                SystemBridge.shareText(st)
                it.dismiss()
            }
        }
    }

}
