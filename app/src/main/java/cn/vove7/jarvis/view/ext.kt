package cn.vove7.jarvis.view

import androidx.core.content.ContextCompat
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.ButtonsBuilder
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.R

/**
 * # ext
 *
 * @author Vove
 * 2020/3/10
 */


fun ButtonsBuilder.positiveButtonWithColor(pt: String, onClick: (BottomDialog) -> Unit) {
    positiveButton(pt.spanColor(ContextCompat.getColor(GlobalApp.APP, R.color.google_green)), onClick)
}