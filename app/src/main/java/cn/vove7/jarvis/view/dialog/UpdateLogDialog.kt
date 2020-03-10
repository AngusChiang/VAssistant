package cn.vove7.jarvis.view.dialog

import android.app.Activity
import androidx.core.content.ContextCompat
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.App
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.dialog.contentbuilder.MarkdownContentBuilder
import cn.vove7.jarvis.view.dialog.contentbuilder.markdownContent
import cn.vove7.jarvis.view.positiveButtonWithColor
import java.util.*

/**
 * # UpdateLogDialog
 *
 * @author Administrator
 * 2018/10/28
 */
class UpdateLogDialog(context: Activity, onDismiss: (() -> Unit)? = null) {

    init {
        val d = BottomDialog.builder(context) {
            awesomeHeader("更新日志")
            markdownContent {
                loadMarkdownFromAsset("files/update_log.md")
            }
            buttons {
                if (UserInfo.getVipEndDate() ?: Date(0) < Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, 7) }.time) {
                    neutralButton("支持一下".spanColor(ContextCompat.getColor(context, R.color.google_green))) {
                        UserInfoDialog.recharge(context)
                    }
                }
                positiveButtonWithColor("新用户必读") {
                    it.dismiss()
                    BottomDialog.builder(context) {
                        awesomeHeader(context.getString(R.string.using_help))
                        markdownContent {
                            loadMarkdownFromAsset("files/introduction.md")
                        }
                    }
                }
            }
        }
        d.setOnDismissListener {
            onDismiss?.invoke()
        }
    }

}