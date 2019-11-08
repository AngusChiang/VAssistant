package cn.vove7.jarvis.view.dialog.contentbuilder

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import cn.vove7.bottomdialog.builder.BindView
import cn.vove7.bottomdialog.builder.ListAdapterBuilder
import cn.vove7.bottomdialog.util.ObservableList
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.tools.SettingItemHelper
import cn.vove7.smartkey.BaseConfig


/**
 * 设置item列表 for bottom sheet
 */
class SettingItemBuilder(
        items: MutableList<SettingChildItem>,
        val config: BaseConfig
) : ListAdapterBuilder<SettingChildItem>(ObservableList(items), false, null) {

    override val adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>
        get() = object : androidx.recyclerview.widget.RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VH =
                VH(SettingItemHelper(p0.context, items[p1], config).fill(p0).itemView)

            override fun getItemCount(): Int = items.size

            override fun getItemViewType(position: Int): Int = position

            override fun onBindViewHolder(p0: VH, p1: Int) {
            }

        }


    override val bindView: BindView<SettingChildItem> = { _, _ -> }

    override val itemView: (type: Int) -> Int = { 0 }

    class VH(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

}
