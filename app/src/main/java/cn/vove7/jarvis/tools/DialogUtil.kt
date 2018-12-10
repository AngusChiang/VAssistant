package cn.vove7.jarvis.tools

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.widget.AppCompatCheckBox
import android.view.Gravity
import android.widget.ScrollView
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.BooleanCallback
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.list.listItems

/**
 * # DialogUtil
 *
 * @author Administrator
 * 9/22/2018
 */
object DialogUtil {
    fun showSelApp(context: Context, back: (Pair<String, String>) -> Unit) {

        val list = arrayListOf<String>()
        AdvanAppHelper.APP_LIST.values.forEach {
            list.add(it.name + "\n" + it.packageName)
        }
        MaterialDialog(context)
                .title(R.string.text_select_application)
                .listItems(items = list, waitForPositiveButton = false) { _, i, s ->
                    val ss = s.split("\n")
                    back.invoke(Pair(ss[0], ss[1]))
                }
                .show()
    }

    fun dataDelAlert(context: Context, onPosClick: () -> Unit) {

        MaterialDialog(context)
                .title(R.string.text_confirm_2_del)
                .message(text = "若已分享，将同时删除云端记录")
                .positiveButton(R.string.text_confirm) { _ ->
                    onPosClick.invoke()
                }.negativeButton()
                .show()
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
        @StringRes res: Int = 0,
        text: String? = null,
        isCheckedDefault: Boolean = false,
        onToggle: BooleanCallback
): MaterialDialog {
    checkBoxPrompt(res, text, isCheckedDefault, onToggle)
    findViewById<AppCompatCheckBox>(R.id.md_checkbox_prompt).also {
        it.buttonDrawable = null
        it.isClickable = false
    }
    return this
}

fun MaterialDialog.noAutoScroll() {
    findViewById<ScrollView>(R.id.md_scrollview_content)?.also {
        Vog.d(this, "noAutoScroll ---> 0")
        it.isFocusable = true
        it.isFocusableInTouchMode = true
        it.requestFocus()
//        it.fullScroll(ScrollView.FOCUS_UP)
    }
}