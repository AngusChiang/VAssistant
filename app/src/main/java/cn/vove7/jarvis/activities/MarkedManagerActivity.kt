package cn.vove7.jarvis.activities

import androidx.fragment.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.*
import cn.vove7.jarvis.fragments.base.OnSyncMarked
import cn.vove7.jarvis.tools.SearchActionHelper
import kotlinx.android.synthetic.main.activity_base_view_pager.*

/**
 * # MarkedManagerActivity
 *
 * @author 17719247306
 * 2018/9/4
 */
class MarkedManagerActivity : BaseActivityWithViewPager() {

    override var titleInit: String?
        get() = getString(R.string.text_mark_management)
        set(_) {}

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
                val f = fragments[p] as OnSyncMarked
                f.onSync(indexTypes[p])
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        val indexTypes = arrayOf(
                arrayOf(MarkedData.MARKED_TYPE_CONTACT),
                arrayOf(MarkedData.MARKED_TYPE_APP),
                arrayOf(MarkedData.MARKED_TYPE_SCRIPT_JS, MarkedData.MARKED_TYPE_SCRIPT_LUA),
                arrayOf()//ad
        )
    }

    override var fragments: Array<androidx.fragment.app.Fragment> = arrayOf(
            MarkedContractFragment(),
            MarkedAppFragment(),
            MarkedOpenFragment(),
            MarkedAdFragment()
    )
    override var titles: Array<String> = arrayOf("联系人", "应用", "功能", "广告")
}