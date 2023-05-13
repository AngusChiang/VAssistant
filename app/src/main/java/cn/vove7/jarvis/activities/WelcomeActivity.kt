package cn.vove7.jarvis.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.Fragment
import android.view.View
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.WelcomeFragment

/**
 * # WelcomeActivity
 *
 * @author Vove
 * 2018/8/29
 */
class WelcomeActivity : BaseActivityWithViewPager() {
    override var titles: Array<String> = arrayOf()
    override var fragments: Array<Fragment> = arrayOf()

    override fun beforeSetViewPager() {
        fragments = arrayOf(
                WelcomeFragment(R.layout.item_left_right_text)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        viewBinding.tabLayout.visibility = View.GONE
        viewBinding.appBar.visibility = View.GONE
        supportActionBar?.hide()
    }
}