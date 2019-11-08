package cn.vove7.jarvis.view.floatwindows

import android.app.Activity
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.title
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.chat.UrlItem
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.dialog.contentbuilder.SettingItemBuilder

/**
 * # IFloatyPanel
 *
 * @author Vove
 * 2019/10/22
 */
interface IFloatyPanel {
    fun hideImmediately()

    fun hideDelay(delay: Long = 800)
    fun show(text: String?)

    fun showUserWord(text: String?)
    fun showTextResult(result: String)
    fun showListResult(title: String, items: List<UrlItem>)

    fun showListeningAni()

    fun showAndHideDelay(text: String, delay: Long = 1000) {
        showTextResult(text)
        hideDelay(delay)
    }

    fun showParseAni()

    val settingItems: Array<SettingChildItem>

    fun showSettings(activity: Activity) {
        if (settingItems.isEmpty()) {
            GlobalApp.toastInfo("此样式无设置选项")
        } else {
            BottomDialog.builder(activity) {
                peekHeight = 600
                title("面板设置")
                content(SettingItemBuilder(settingItems.toMutableList(), FloatPanelConfig))
            }
        }
    }

}
