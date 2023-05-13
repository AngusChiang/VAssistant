package cn.vove7.jarvis.view.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * # ListItemAnimationHelper
 *
 * @author Vove
 * 2018/9/11
 */
class ListItemAnimationHelper(val onlyFirstIn: Boolean, var dy: Float = 50f) {

    private var lastAnimatedPosition = 0
    var animationsLocked = false

    fun init() {
        lastAnimatedPosition = 0
    }

    fun fromB2T(view: View?, position: Int, fadeShow: Boolean = true, dyy: Float = dy, d: Long = 200) {
        start(view, position, 1, dyy, d, fadeShow)
    }

    fun fromT2B(view: View?, position: Int, fadeShow: Boolean = true, dyy: Float = dy, d: Long = 200) {
        start(view, position, -1, dyy, d, fadeShow)
    }

    fun hide(view: View?, end: (() -> Unit)? = null) {
        if (view == null) return
        view.alpha = 1f
        view.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        end?.invoke()
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        end?.invoke()
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }
                })
                .start()
    }

    private fun start(view: View?, position: Int, direction: Int, dyy: Float, d: Long, fadeShow: Boolean) {
        if (view == null) return
        if (animationsLocked)
            return

//        if (position > lastAnimatedPosition) {
        lastAnimatedPosition = position
        view.translationY = if (direction == 1) dyy else -dyy
        if (fadeShow)
            view.alpha = 0f//完全透明

        view.animate()
                .translationY(0.2f).alpha(1f)//设置最终效果为完全不透明，并且在原来的位置
                .setStartDelay((10 * position).toLong())//根据item的位置设置延迟时间，达到依次动画一个接一个进行的效果
                .setInterpolator(DecelerateInterpolator(0.8f))//设置动画效果为在动画开始的地方快然后慢
                .setDuration(d)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (onlyFirstIn)
                            animationsLocked = true//确保仅屏幕一开始能够显示的item项才开启动画，也就是说屏幕下方还没有显示的item项滑动时是没有动画效果
                    }
                }).start()
//        }
    }

}