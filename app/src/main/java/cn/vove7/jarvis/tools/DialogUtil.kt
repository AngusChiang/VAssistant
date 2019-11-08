package cn.vove7.jarvis.tools

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import androidx.appcompat.widget.AppCompatCheckBox
import android.widget.ScrollView
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt

/**
 * # DialogUtil
 *
 * @author Administrator
 * 9/22/2018
 */
object DialogUtil {

    fun dataDelAlert(context: Context, onPosClick: () -> Unit) {

        MaterialDialog(context)
                .title(R.string.text_confirm_2_del)
                .message(text = "若已分享，将同时删除云端记录")
                .positiveButton(R.string.text_confirm) {
                    onPosClick.invoke()
                }.negativeButton()
                .show()
    }

}

fun Dialog.setFloat() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        if (!cn.vove7.vtp.runtimepermission.PermissionUtils.canDrawOverlays(context)) {
            throw Exception("无悬浮窗权限")
        }
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        window?.setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
    } else {
        window?.setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
    }
}

/**
 * 隐藏CheckBox按钮 仅显示文字
 * @receiver MaterialDialog
 * @param res Int
 * @param text String?
 * @param isCheckedDefault Boolean
 * @param onToggle BooleanCallback
 * @return MaterialDialog
 */
@SuppressLint("CheckResult")
fun MaterialDialog.checkBoxText(
        text: String? = null
): MaterialDialog {
    checkBoxPrompt(text = text, isCheckedDefault = false) {}
    findViewById<AppCompatCheckBox>(R.id.md_checkbox_prompt).also {
        it.buttonDrawable = null
        it.isClickable = false
    }
    return this
}

fun MaterialDialog.noAutoScroll() {
    findViewById<ScrollView>(R.id.md_scrollview_content)?.also {
        Vog.d("noAutoScroll ---> 0")
        it.isFocusable = true
        it.isFocusableInTouchMode = true
        it.requestFocus()
//        it.fullScroll(ScrollView.FOCUS_UP)
    }
}