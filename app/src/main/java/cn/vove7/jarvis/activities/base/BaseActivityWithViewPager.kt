package cn.vove7.jarvis.activities.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.activity_base_view_pager.*

/**
 * # BaseActivityWithViewPager
 *
 * @author 17719
 * 2018/8/14
 */
abstract class BaseActivityWithViewPager : BaseActivity() {
    override val layoutRes: Int
        get() = R.layout.activity_base_view_pager
    lateinit var fragmentAdapter: FragmentAdapter

    val currentFragment: androidx.fragment.app.Fragment
        get() = fragments[view_pager.currentItem]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar = findViewById(R.id.toolbar)
        //设置ToolBar
        setSupportActionBar(toolbar)
        beforeSetViewPager()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        FragmentAdapter(supportFragmentManager).also {
            fragmentAdapter = it
            view_pager.adapter = it
        }
        if (fragments.size == 1)
            tab_layout.visibility = View.GONE
        else
            tab_layout.setupWithViewPager(view_pager)
        if (titleInit != null) {
            this.title = titleInit
        }

    }

    /**
     * 仅用来初始化
     * 其他时间使用无作用（无法设置标题
     */
    open var titleInit: String? = null

    /**
     * 可用来初始化Fragment
     */
    open fun beforeSetViewPager() {}

    abstract var fragments: Array<androidx.fragment.app.Fragment>

    abstract var titles: Array<String>

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Vog.d("${item?.title}")
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class FragmentAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = titles[position]

    }
}