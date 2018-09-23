package cn.vove7.jarvis.view.statusbar

import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R

/**
 * # ExecuteAnimation
 *
 * @author 17719247306
 * 2018/9/4
 */
class ExecuteAnimation : StatusAnimation() {
    override var title: String = GlobalApp.getString(R.string.text_executing)
    override var beginAniId: Int = R.drawable.voice_animation

    override var failedAniId: Int = R.drawable.ic_sentiment_dissatisfied_red_a400_18dp

    var failed = false
    override fun onFailed() {
        failed = true
        super.onFailed()
    }

    override fun finish() {
        if (!failed)
            super.finish()
    }
}