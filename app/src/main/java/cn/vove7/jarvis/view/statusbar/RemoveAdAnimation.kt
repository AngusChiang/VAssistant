package cn.vove7.jarvis.view.statusbar

import cn.vove7.jarvis.R

/**
 * # RemoveAdAnimation
 *
 * @author Administrator
 * 9/28/2018
 */
class RemoveAdAnimation : StatusAnimation() {
    override var title: String = "已为你关闭广告"
    override var beginAniId: Int = R.drawable.remove
    override var failedAniId: Int = R.drawable.remove
}