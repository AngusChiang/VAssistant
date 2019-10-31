package cn.vove7.jarvis.view.floatwindows

import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.chat.UrlItem

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
    fun showListResult(tite:String, items: List<UrlItem>)

    fun showListeningAni()

    fun showAndHideDelay(text: String, delay: Long = 1000) {
        showTextResult(text)
        hideDelay(delay)
    }

    fun showParseAni()

    fun showSettings() {
        GlobalApp.toastInfo("此样式无设置选项")
    }

}