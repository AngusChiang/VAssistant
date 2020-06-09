package cn.vove7.jarvis.view.tools

import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.vove7.common.utils.findFirstParentWith
import cn.vove7.vtp.log.Vog
import kotlin.math.abs

/**
 * # SpringEffectHelper
 *
 * 使用示例: [cn.vove7.jarvis.view.SpringExpandableListView]
 *
 * @property target View
 * @property isBottom Function0<Boolean>
 * @property isTop Function0<Boolean>
 * @property superOnTouch Function1<MotionEvent, Boolean>
 * @property valueDelegate FloatPropertyCompat<View>  [TranslationYPropertyCompat]
 * @property maxOffset Float
 * @property startDragY Float
 * @property springAnim SpringAnimation
 * @constructor
 *
 * Created on 2020/6/9
 * @author Vove
 */
class SpringEffectHelper(
        private val target: View,
        private val isBottom: () -> Boolean,
        private val isTop: () -> Boolean,
        private val superOnTouch: (MotionEvent) -> Boolean,
        private val valueDelegate: FloatPropertyCompat<View>,
        private val maxOffset: Float = 50f
) {

    private var startDragY = 0f
    private val springAnim: SpringAnimation = SpringAnimation(
            target,
            valueDelegate,
            0f
    ).apply {
        //刚度，值越大回弹的速度越快，类似于劲度系数，默认值是 1500f
        spring.stiffness = 1000.0f
        //阻尼 默认0.5 值越小，回弹之后来回的次数越多
        spring.dampingRatio = 0.35f
        this.addEndListener { _, _, _, _ ->
            startDragY = 0f
            Vog.d("addEndListener")
        }
    }

    fun Float.bounce(): Float {
        val f = if (this < 0) -1 else 1
        val it = abs(this)
        return (if (it > maxOffset) {
            (it - maxOffset) * 0.3f + maxOffset
        } else it) * f
    }

    fun onTouch(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                val p = target.findFirstParentWith { it is SwipeRefreshLayout }
                if (p != null && p is SwipeRefreshLayout) {
                    if (p.isRefreshing) return false
                }
            }
            MotionEvent.ACTION_MOVE -> if (isTop()) {
                //顶部下拉
                if (startDragY == 0f) {
                    startDragY = e.rawY
                }
                when {
                    e.rawY - startDragY > 0 -> {
                        val p = target.findFirstParentWith { it is SwipeRefreshLayout }
                        if (p != null && (p as SwipeRefreshLayout).isEnabled) {
                            return superOnTouch(e)
                        }
                        valueDelegate.setValue(target, ((e.rawY - startDragY) / 3).bounce())
                        return true
                    }
                    isBottom() -> {
                        dealScrollUp(e)?.also { return it }
                    }
                    else -> {
                        springAnim.cancel()
                        valueDelegate.setValue(target, 0f)
                    }
                }
            } else if (isBottom()) {
                dealScrollUp(e)?.also { return it }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (valueDelegate.getValue(target) != 0f) {
                    springAnim.start()
                }
                startDragY = 0f
            }
        }
        return superOnTouch(e)
    }

    private fun dealScrollUp(e: MotionEvent): Boolean? {
        //底部上拉
        if (startDragY == 0f) {
            Vog.d("底部上拉 ${e.rawY} $startDragY")
            startDragY = e.rawY
        }
        if (e.rawY - startDragY < 0) {
            valueDelegate.setValue(target, ((e.rawY - startDragY) / 3).bounce())
            return true
        } else {
            springAnim.cancel()
            valueDelegate.setValue(target, 0f)
        }
        return null
    }
}

class TranslationYPropertyCompat : FloatPropertyCompat<View>("TranslationPropertyCompat") {

    override fun getValue(v: View) = v.translationY

    override fun setValue(v: View, value: Float) {
        v.translationY = value
    }
}

class TranslationXPropertyCompat : FloatPropertyCompat<View>("TranslationPropertyCompat") {

    override fun getValue(v: View) = v.translationX

    override fun setValue(v: View, value: Float) {
        v.translationX = value
    }
}
