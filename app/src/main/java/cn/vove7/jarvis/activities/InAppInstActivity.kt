package cn.vove7.jarvis.activities

import android.os.Bundle
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.fragments.InAppInstListFragment

class InAppInstActivity : OneFragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra("title")
    }

    override fun beforeSetViewPager() {
        val f = InAppInstListFragment.newInstance(
                intent.getStringExtra("pkg"), intent.getStringExtra("title"))
        fragments = arrayOf(f)
    }
}