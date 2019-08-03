package cn.vove7.jarvis.view.dialog

import android.app.Activity
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.BottomDialogBuilder
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.builder.title
import cn.vove7.bottomdoalog.extension.awesomeHeader
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.view.dialog.contentbuilder.MarkdownContentBuilder
import cn.vove7.vtp.sharedpreference.SpHelper

/**
 * # AppUpdateDialog
 * APP升级对话框
 * @author Vove
 * 2019/6/23
 */
class AppUpdateDialog(val context: Activity, val verName: String, val updateLog: String) {

    init {
        BottomDialog.builder(context, action = getBuildAction(verName, updateLog))
    }

    companion object {
        fun getBuildAction(ver: String, log: String): BottomDialogBuilder.() -> Unit {
            return {
                cancelable(false)
                awesomeHeader("发现新版本: v$ver")
                content(MarkdownContentBuilder()) {
                    loadMarkdown(log)
                }
                buttons {
                    neutralButton("不再提醒此版本") {
                        val sp = SpHelper(context)
                        sp.set("no_update_ver_name", ver)
                        it.dismiss()
                    }
                    positiveButton("用酷安下载") {
                        AppConfig.openCoolapk(context)
                        it.dismiss()
                    }
                    negativeButton()
                }
            }
        }
    }
}