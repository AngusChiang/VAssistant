package cn.vove7.jarvis.activities.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import cn.vove7.jarvis.R
import kotlinx.android.synthetic.main.activity_base_view_pager.*

/**
 * # BaseActivityWithViewPager
 *
 * @author 17719
 * 2018/8/14
 */
abstract class BaseActivityWithViewPager : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_view_pager)

        //设置ToolBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        setSupportActionBar(toolbar)
        view_pager.adapter = FragmentAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }


    abstract val fragments: Array<Fragment>

    abstract var titles: Array<String>

    inner class FragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = titles[position]

    }
}