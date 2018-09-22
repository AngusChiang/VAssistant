package cn.vove7.jarvis.utils

import android.content.Context
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
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