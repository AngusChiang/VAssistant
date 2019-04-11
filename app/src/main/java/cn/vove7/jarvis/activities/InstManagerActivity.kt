package cn.vove7.jarvis.activities

import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.GlobalInstListFragment
import cn.vove7.jarvis.fragments.InstAppListFragment
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.tools.SearchActionHelper
import kotlinx.android.synthetic.main.activity_base_view_pager.*

/**
 * 命令管理
 */
class InstManagerActivity : BaseActivityWithViewPager() {

    override var titleInit: String? = "指令管理"

    override var fragments: Array<Fragment> = arrayOf(
            GlobalInstListFragment(),
            InstAppListFragment()
    )

    override fun onCreateOptionsMenu(m: Menu?): Boolean {
        toolbar.apply {
            inflateMenu(R.menu.menu_sync)

            SearchActionHelper(menu!!.findItem(R.id.menu_item_search)) { text ->
                (currentFragment as SimpleListFragment<*>).search(text)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_sync -> {//同步
                val p = view_pager.currentItem
                val f = fragments[p] as OnSyncInst
                f.onSync()
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override var titles: Array<String> = arrayOf("全局指令", "应用内指令")
}

interface OnSyncInst {
    fun onSync()
}
