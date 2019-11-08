package cn.vove7.jarvis.view.statusbar

import androidx.core.app.NotificationManagerCompat
import cn.vove7.jarvis.R

/**
 * # WakeupStatusAnimation
 *
 * @author Administrator
 * 2018/11/6
 */
class WakeupStatusAnimation : StatusAnimation() {
    override val importLevel: Int get() = NotificationManagerCompat.IMPORTANCE_MAX
    override val nId: Int = 527
    override val alert: Boolean get() = true
    override var title: String = "语音唤醒"
    override var beginAniId: Int = R.drawable.ic_hearing
    override var failedAniId: Int = R.drawable.ic_unhearing
}