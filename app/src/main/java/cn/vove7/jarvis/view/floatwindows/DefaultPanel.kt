package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.AnimationDrawable
import android.view.ViewAnimationUtils
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.listener
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.SingleChoiceItem
import kotlinx.android.synthetic.main.float_panel_default.view.*

/**
 * # DefaultPanel
 *
 * @author Vove
 * 2019/10/22
 */
class DefaultPanel : FloatyPanel(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
) {
    override fun layoutResId(): Int = R.layout.float_panel_default

    override fun showListeningAni() {
        if (contentView?.listening_ani?.isShown == true) return
        contentView?.parse_ani?.gone()
        contentView?.listening_ani?.show()
    }

    override fun showParseAni() {
        if (contentView?.parse_ani?.isShown == true) return
        contentView?.parse_ani?.apply {
            (drawable as? AnimationDrawable)?.start()
            show()
        }
        contentView?.listening_ani?.gone()
    }

    override fun showEnterAnimation() = runInCatch {
        val width = contentView?.width ?: screenWidth
        when (AppConfig.fpAnimation) {
            1 -> {//揭露动画
                ViewAnimationUtils
                        .createCircularReveal(animationBody, width / 2, 0, 0f, width.toFloat())
                        .apply {
                            interpolator = AccelerateInterpolator()
                            duration = 400
                            start()
                        }
            }
            else -> {//默认
                animationBody.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pop_fade_in))
            }
        }
    }


    override fun showExitAnimation() = buildExitAnimation()


    private fun buildExitAnimation() {
        when (AppConfig.fpAnimation) {
            1 -> {//揭露动画
                startCircularAnimation()
            }
            else -> {
                AnimationUtils.loadAnimation(context, R.anim.pop_fade_out).apply {
                    listener {
                        onEnd { superRemove() }
                    }
                    runInCatch {
                        animationBody.startAnimation(this)
                    }
                }
            }
        }
    }

    private fun startCircularAnimation() {
        try {
            ViewAnimationUtils.createCircularReveal(animationBody, screenWidth / 2, 0,
                    screenWidth.toFloat(), 0f)
                    .apply {
                        duration = 300
                        interpolator = AccelerateInterpolator()
                        listener {
                            onEnd { superRemove() }
                        }
                        start()
                    }
        } catch (e: Exception) {
            e.printStackTrace()
            superRemove()
        }
    }
    override val settingItems: Array<SettingChildItem>
        get() = arrayOf(
                SingleChoiceItem(
                        title = "动画", defaultValue = 0,
                        entityArrId = R.array.list_fp_animation,
                        keyId = R.string.key_fp_animation
                )
        )

}