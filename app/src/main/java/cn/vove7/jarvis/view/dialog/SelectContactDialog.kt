package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.os.Bundle
import cn.vove7.common.utils.CoroutineExt
import cn.vove7.common.helper.AdvanContactHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithList

/**
 * # SelectContactDialog
 *
 * @author Administrator
 * 2018/12/20
 */
class SelectContactDialog(context: Context,
                          private val onSel: (Pair<String, String>) -> Unit) :
        BottomDialogWithList<Pair<String, String>>(context, title = "选择联系人") {

    override fun onLoadData(pageIndex: Int) {
        CoroutineExt.launch {
            notifyLoadSuccess(AdvanContactHelper.getSimpleList(), true)
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


    override fun unification(data: Pair<String, String>): ListViewModel<Pair<String, String>>? {
        return ListViewModel(data.first, data.second, extra = data)
    }

    override var sortData: Boolean = true

    override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<Pair<String, String>>) {
        onSel.invoke(item.extra)
        dismiss()
    }
}