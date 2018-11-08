package cn.vove7.jarvis.view.statusbar

import cn.vove7.jarvis.R

/**
 * # AccessibilityStatusAnimation
 *
 * @author Administrator
 * 2018/11/8
 */
class AccessibilityStatusAnimation : StatusAnimation() {
    override val nId = 1234
    override var title: String = "无障碍服务"
    override var beginAniId: Int = R.drawable.ic_accessibility
    override var failedAniId: Int = R.drawable.ic_unaccessibility
}