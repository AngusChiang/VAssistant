package cn.vove7.jarvis.activities.base

import android.support.v4.app.Fragment

/**
 * # OneFragmentActivity
 *
 * @author 17719247306
 * 2018/8/19
 */

abstract class OneFragmentActivity : BaseActivityWithViewPager() {
    override var fragments: Array<Fragment> = arrayOf()

    override var titles: Array<String> = arrayOf()

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }
}