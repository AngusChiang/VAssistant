package cn.vove7.jarvis.view.statusbar

import android.content.Context
import cn.vove7.jarvis.R

/**
 * # StatusVoiceIconAnimation
 *
 * @author 17719247306
 * 2018/9/2
 */
class StatusVoiceIconAnimation(context: Context) : StatusAnimation(context) {
    override val title: String
        get() = "识别中"
    override val beginAniId: Int
        get() = R.drawable.voice_animation
    override val failedAniId: Int
        get() = R.drawable.voice
}