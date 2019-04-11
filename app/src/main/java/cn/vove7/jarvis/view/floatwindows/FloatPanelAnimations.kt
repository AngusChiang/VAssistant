package cn.vove7.jarvis.view.floatwindows

import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.listener
import cn.vove7.common.utils.runInCatch
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppConfig

/**
 * # FloatPanelAnimations
 *
 * @author 11324
 * 2019/4/10
 */


fun FloatyPanel.showEnterAnimation() {
    runInCatch {
        val arr = GlobalApp.APP.resources.getStringArray(R.array.list_fp_animation)
        when (arr.indexOf(AppConfig.fpAnimation)) {
            1 -> {//揭露动画
                ViewAnimationUtils
                        .createCircularReveal(animationBody, screenWidth / 2, 0, 0f, screenWidth.toFloat())
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

}

fun FloatyPanel.showExitAnimation() {
    buildExitAnimation()
}

private fun FloatyPanel.buildExitAnimation() {
    val arr = context.resources.getStringArray(R.array.list_fp_animation)
    when (arr.indexOf(AppConfig.fpAnimation)) {
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

private fun FloatyPanel.startCircularAnimation() {
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
