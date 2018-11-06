package cn.vove7.jarvis.view.statusbar

import cn.vove7.jarvis.R

/**
 * # WakeupStatusAnimation
 *
 * @author Administrator
 * 2018/11/6
 */
class WakeupStatusAnimation : StatusAnimation() {
    override val nId: Int = 527
    override var title: String = "语音唤醒"
    override var beginAniId: Int = R.drawable.ic_hearing
    override var failedAniId: Int = R.drawable.ic_unhearing
}