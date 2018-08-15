package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.InstListFragment

/**
 * 命令管理
 */
class InstManagerActivity : BaseActivityWithViewPager() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setSupportActionBar()
    }

    override val fragments: Array<Fragment>
        get() = arrayOf(
                InstListFragment.newInstance(InstListFragment.INST_TYPE_GLOBAL),
                InstListFragment.newInstance(InstListFragment.INST_TYPE_APP_INNER)
        )
    override var titles: Array<String> = arrayOf("全局命令", "应用内命令")

}
