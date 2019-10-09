package cn.vove7.jarvis.view

import android.support.design.widget.TextInputLayout
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.RootHelper
import cn.vove7.jarvis.R
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.log.Vog
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * # Utils
 *
 * @author Administrator
 * 2018/12/30
 */

/**
 * 检查TextInputLayout编辑框内容是否空
 * @receiver TextInputLayout
 * @param errMsgWhenEmpty String
 * @return String?
 */
fun TextInputLayout.checkEmpty(
        errMsgWhenEmpty: String = context.getString(R.string.text_not_empty), trim: Boolean = true): String? {
    val s = editText?.text.toString()
    if ((if (trim) s.trim() else s).isEmpty()) {
        error = errMsgWhenEmpty
        return null
    }
    error = ""
    return s

}