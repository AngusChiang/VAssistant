package cn.vove7.jarvis.activities

import android.support.v4.app.Fragment
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager

/**
 * # MarkedManageActivity
 *
 * @author 17719
 * 2018/8/14
 */
class MarkedManageActivity : BaseActivityWithViewPager() {
    override val fragments: Array<Fragment> = arrayOf(

    )
    override var titles: Array<String> = arrayOf("联系人", "应用")
}