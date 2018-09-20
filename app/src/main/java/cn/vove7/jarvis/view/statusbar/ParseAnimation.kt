package cn.vove7.jarvis.view.statusbar

import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R

/**
 * # ParseAnimation
 *
 * @author 17719247306
 * 2018/9/2
 */
class ParseAnimation : StatusAnimation() {
    override var title: String = GlobalApp.getString(R.string.text_parsing)
    override var beginAniId: Int = R.drawable.parsing_animation

    override var failedAniId: Int = R.drawable.voice
}