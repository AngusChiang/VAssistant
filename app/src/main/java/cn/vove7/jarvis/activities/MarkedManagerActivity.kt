package cn.vove7.jarvis.activities

import android.support.v4.app.Fragment
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.*

/**
 * # MarkedManagerActivity
 *
 * @author 17719247306
 * 2018/9/4
 */
class MarkedManagerActivity : BaseActivityWithViewPager() {

    override var titleInit: String?
        get() = "标记管理"
        set(_) {}
    override var fragments: Array<Fragment> = arrayOf(
            MarkedContractFragment(),
            MarkedAppFragment(),
            MarkedOpenFragment(),
            MarkedAdFragment()

    )
    override var titles: Array<String> = arrayOf("联系人", "应用", "打开", "广告")
}
