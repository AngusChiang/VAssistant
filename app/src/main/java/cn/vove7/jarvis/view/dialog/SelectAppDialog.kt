package cn.vove7.jarvis.view.dialog

import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.ToolbarHeader
import cn.vove7.bottomdialog.toolbar
import cn.vove7.common.utils.CoroutineExt
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.tools.SearchActionHelper
import cn.vove7.jarvis.view.dialog.contentbuilder.AppListBuilder
import cn.vove7.vtp.app.AppInfo

/**
 * # SelectAppDialog
 *
 * @author Administrator
 * 2018/12/20
 */
class SelectAppDialog {

    companion object {
        fun get(context: BaseActivity, onSel: (AppInfo) -> Unit): BottomDialog {
            val contentBuilder = AppListBuilder(true, context.lifecycleScope) { _, _, item, _ ->
                onSel(item)
            }
            val dialog = BottomDialog.builder(context, false) {
                toolbar {
                    title = "选择应用"
                }
                content(contentBuilder)
            }
            dialog.setOnShowListener {
                dialog.updateHeader<ToolbarHeader> {
                    toolbar.menu?.clear()
                    toolbar.inflateMenu(R.menu.menu_search)
                    val searchItem = toolbar.menu?.findItem(R.id.menu_search)!!
                    SearchActionHelper(searchItem) { text ->
                        contentBuilder.filter(text)
                    }
                }
            }
            return dialog
        }
    }
}