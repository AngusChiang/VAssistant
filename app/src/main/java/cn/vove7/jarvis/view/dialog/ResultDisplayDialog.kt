package cn.vove7.jarvis.view.dialog

import android.content.Context
import cn.vove7.common.utils.LooperHelper
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.chat.UrlItem
import cn.vove7.jarvis.view.dialog.base.BottomDialogWithList

/**
 * # ResultDisplayDialog
 *
 * @author 11324
 * 2019/3/11
 */
class ResultDisplayDialog(context: Context, title: String, val urlItems: List<UrlItem>)
    : BottomDialogWithList<UrlItem>(context, title) {
    override var sortData: Boolean = false

    override fun onLoadData(pageIndex: Int) {
        notifyLoadSuccess(urlItems, true)
    }

    override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<UrlItem>) {
        SystemBridge.openUrl(item.extra.url)
        dismiss()
    }
}