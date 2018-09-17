package cn.vove7.jarvis.activities

import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.MarkedAdFragment
import cn.vove7.jarvis.fragments.MarkedAppFragment
import cn.vove7.jarvis.fragments.MarkedContractFragment
import cn.vove7.jarvis.fragments.MarkedOpenFragment
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_sync,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_sync -> {//同步
                val p = view_pager.currentItem
                val f = fragments[p] as BaseMarkedFragment<*>
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

    override var fragments: Array<Fragment> = arrayOf(
            MarkedContractFragment(),
            MarkedAppFragment(),
            MarkedOpenFragment(),
            MarkedAdFragment()
    )
    override var titles: Array<String> = arrayOf("联系人", "应用", "打开", "广告")
}