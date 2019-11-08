package cn.vove7.jarvis.view.floatwindows

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.BindView
import cn.vove7.bottomdialog.builder.ListAdapterBuilder
import cn.vove7.bottomdialog.builder.title
import cn.vove7.bottomdialog.util.ObservableList
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.chat.UrlItem
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.tools.SettingItemHelper

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

    val settingItems: Array<SettingChildItem>

    fun showSettings(activity: Activity) {
        if (settingItems.isEmpty()) {
            GlobalApp.toastInfo("此样式无设置选项")
        } else {
            BottomDialog.builder(activity) {
                peekHeight = 900
                title("面板设置")
                content(SettingItemBuilder(settingItems.toMutableList()))
            }
        }
    }

}

class SettingItemBuilder(
        items: MutableList<SettingChildItem>
) : ListAdapterBuilder<SettingChildItem>(ObservableList(items), false, null) {

    override val adapter: RecyclerView.Adapter<*>
        get() = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VH = VH(SettingItemHelper(p0.context, items[p1]).fill().itemView)

            override fun getItemCount(): Int = items.size

            override fun getItemViewType(position: Int): Int = position

            override fun onBindViewHolder(p0: VH, p1: Int) {
            }

        }


    override val bindView: BindView<SettingChildItem> = { _, _ -> }

    override val itemView: (type: Int) -> Int = { 0 }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)

}
