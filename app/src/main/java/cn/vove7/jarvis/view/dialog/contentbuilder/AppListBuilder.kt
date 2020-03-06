package cn.vove7.jarvis.view.dialog.contentbuilder

import android.view.View
import cn.vove7.bottomdialog.builder.BindView
import cn.vove7.bottomdialog.builder.ListAdapterBuilder
import cn.vove7.bottomdialog.builder.OnItemClick
import cn.vove7.bottomdialog.util.ObservableList
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.utils.gone
import cn.vove7.jarvis.R
import cn.vove7.vtp.app.AppInfo
import kotlinx.android.synthetic.main.item_normal_icon_title.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * # AppListBuilder
 * 不完善 无法搜索，排序
 * @author Vove
 * 2019/6/30
 */
class AppListBuilder(
        autoDismiss: Boolean,
        private val scope: CoroutineScope,
        onItemClick: OnItemClick<AppInfo>
) : ListAdapterBuilder<AppInfo>(ObservableList(), autoDismiss, onItemClick) {

    override fun init(view: View) {
        super.init(view)
        loading = true
        scope.launch {
            delay(500)
            items.addAll(AdvanAppHelper.ALL_APP_LIST.values)
            backupList.addAll(items)
            loading = false
        }
    }

    private val backupList = mutableListOf<AppInfo>()

    override val bindView: BindView<AppInfo>
        get() = { view, item ->
            view.icon.gone()
            view.title.text = item.name
            view.sub_title.text = item.packageName
        }

    fun filter(text: String) {
        if (text.isBlank()) {
            if (items.size != backupList.size) {
                loading = true
                scope.launch {
                    delay(100)
                    items.clear()
                    items.addAll(backupList)
                    loading = false
                }
            }
            return
        }
        loading = true
        scope.launch {
            val tmp = backupList.filter {
                it.name?.contains(text, ignoreCase = true) == true
                        || it.packageName.contains(text, ignoreCase = true)
            }
            items.clear()
            items.addAll(tmp)
            loading = false
        }
    }

    override val itemView: (type: Int) -> Int = {
        R.layout.item_normal_icon_title
    }
}
