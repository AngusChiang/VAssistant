package cn.vove7.jarvis.activities.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
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
        setSupportActionBar(toolbar)
//        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        view_pager.adapter = FragmentAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }


    abstract val fragments: Array<Fragment>

    abstract var titles: Array<String>

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Vog.d(this, "onOptionsItemSelected ${item?.itemId}")
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class FragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = titles[position]

    }
}