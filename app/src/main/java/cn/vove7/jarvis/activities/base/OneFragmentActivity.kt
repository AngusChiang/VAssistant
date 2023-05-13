package cn.vove7.jarvis.activities.base

import androidx.fragment.app.Fragment

/**
 * # OneFragmentActivity
 *
 * @author Vove
 * 2018/8/19
 */

abstract class OneFragmentActivity : BaseActivityWithViewPager() {
    override var fragments: Array<Fragment> = arrayOf()

    override var titles: Array<String> = arrayOf()
}