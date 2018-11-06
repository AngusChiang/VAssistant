package cn.vove7.jarvis.view.statusbar

import cn.vove7.jarvis.R

/**
 * # MicroToggleAnimation
 *
 * @author Administrator
 * 2018/11/5
 */
class MicroToggleAnimation : StatusAnimation() {
    override val nId: Int = 1127
    override var title: String = "语音唤醒"
    override var beginAniId: Int = R.drawable.ic_unmute_micro
    override var failedAniId: Int = R.drawable.ic_mute_micro
}