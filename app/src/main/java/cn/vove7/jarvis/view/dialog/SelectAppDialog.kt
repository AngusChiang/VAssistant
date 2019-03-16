package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.os.Bundle
import cn.vove7.common.utils.ThreadPool
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithList
import cn.vove7.vtp.app.AppInfo

/**
 * # SelectAppDialog
 *
 * @author Administrator
 * 2018/12/20
 */
class SelectAppDialog(
        context: Context,
        private val onSel: (AppInfo) -> Unit) :
        BottomDialogWithList<AppInfo>(context, title = "选择应用") {
    override var sortData: Boolean = true

    override fun onLoadData(pageIndex: Int) {
        ThreadPool.runOnCachePool {
            notifyLoadSuccess(AdvanAppHelper.ALL_APP_LIST.values, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.apply {
            inflateMenu(R.menu.menu_search)
            val searchItem = menu?.findItem(R.id.menu_search)!!
            searchItem.setIcon(R.drawable.ic_search_black_24dp)
            searchable(searchItem)
        }
    }

    override fun unification(data: AppInfo): ListViewModel<AppInfo>? {
        return ListViewModel(data.name, data.packageName, icon = data.icon,extra = data)
    }

    override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<AppInfo>) {
        onSel.invoke(item.extra)
        dismiss()
    }
}