package cn.vove7.jarvis.view.statusbar

import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R

/**
 * # ListeningAnimation
 *
 * @author Vove
 * 2018/9/2
 */
class ListeningAnimation : StatusAnimation() {
    override var title: String = GlobalApp.getString(R.string.text_listening)
    override var beginAniId: Int= R.drawable.listening_animation
    override var failedAniId: Int = R.drawable.ic_sentiment_dissatisfied_red_a400_18dp
}