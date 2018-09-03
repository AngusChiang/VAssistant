package cn.vove7.jarvis.activities

import android.support.v4.app.Fragment
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.GlobalInstListFragment
import cn.vove7.jarvis.fragments.InstAppListFragment

/**
 * 命令管理
 */
class InstManagerActivity : BaseActivityWithViewPager() {

    override var titleInit: String?
        get() = "命令管理"
        set(value) {}
    override var fragments: Array<Fragment> = arrayOf(
            GlobalInstListFragment(),
            InstAppListFragment()
    )
    override var titles: Array<String> = arrayOf("全局命令", "应用内命令")
}
