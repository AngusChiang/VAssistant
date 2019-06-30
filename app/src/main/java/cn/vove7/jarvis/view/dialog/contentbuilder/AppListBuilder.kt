package cn.vove7.jarvis.view.dialog.contentbuilder

import cn.vove7.bottomdialog.builder.BindView
import cn.vove7.bottomdialog.builder.ListAdapterBuilder
import cn.vove7.bottomdialog.builder.OnItemClick
import cn.vove7.bottomdialog.util.ObservableList
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.jarvis.R
import cn.vove7.vtp.app.AppInfo
import kotlinx.android.synthetic.main.item_normal_icon_title.view.*

/**
 * # AppListBuilder
 * 不完善 无法搜索，排序
 * @author Vove
 * 2019/6/30
 */
class AppListBuilder(
        autoDismiss: Boolean,
        onItemClick: OnItemClick<AppInfo>
) : ListAdapterBuilder<AppInfo>(ObservableList(), autoDismiss, onItemClick) {

    init {
        loading = true
        items.addAll(AdvanAppHelper.ALL_APP_LIST.values)
        loading = false
    }

    override val bindView: BindView<AppInfo>
        get() = { view, item ->
            view.icon.setImageDrawable(item.icon)
            view.title.text = item.name
            view.sub_title.text = item.packageName
        }
    override val itemView: (type: Int) -> Int = {
        R.layout.item_normal_icon_title
    }


}