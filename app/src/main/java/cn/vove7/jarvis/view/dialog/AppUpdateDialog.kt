package cn.vove7.jarvis.view.dialog

import android.content.Context
import cn.vove7.common.app.AppConfig
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt

/**
 * # AppUpdateDialog
 * APP升级对话框
 * @author Vove
 * 2019/6/23
 */
class AppUpdateDialog(val context: Context, val verName: String, val updateLog: String) {

    init {
        val sp = SpHelper(context)
        MaterialDialog(context).title(text = "发现新版本 v$verName")
                .message(text = updateLog)
                .positiveButton(text = "用酷安下载") {
                    AppConfig.openCoolapk(context)
                }
                .checkBoxPrompt(text = "不再提醒此版本") { b ->
                    if (b) {
                        sp.set("no_update_ver_name", verName)
                    } else sp.removeKey("no_update_ver_name")
                }
                .negativeButton()
                .cancelable(false)
                .show()
    }

}