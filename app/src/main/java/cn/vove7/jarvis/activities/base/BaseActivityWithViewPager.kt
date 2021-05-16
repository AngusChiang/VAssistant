package cn.vove7.jarvis.activities.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import cn.vove7.jarvis.R
import cn.vove7.jarvis.databinding.ActivityBaseViewPagerBinding
import cn.vove7.vtp.log.Vog

/**
 * # BaseActivityWithViewPager
 *
 * @author 17719
 * 2018/8/14
 */
abstract class BaseActivityWithViewPager : BaseActivity<ActivityBaseViewPagerBinding>() {

    lateinit var fragmentAdapter: FragmentAdapter

    val currentFragment: androidx.fragment.app.Fragment
        get() = fragments[viewBinding.viewPager.currentItem]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar = findViewById(R.id.toolbar)
        //设置ToolBar
        setSupportActionBar(toolbar)
        beforeSetViewPager()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        FragmentAdapter(supportFragmentManager).also {
            fragmentAdapter = it
            viewBinding.viewPager.adapter = it
        }
        if (fragments.size == 1)
            viewBinding.tabLayout.visibility = View.GONE
        else
            viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)
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

    abstract val fragments: Array<androidx.fragment.app.Fragment>

    abstract var titles: Array<String>

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Vog.d("${item.title}")
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class FragmentAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = titles[position]

    }
}