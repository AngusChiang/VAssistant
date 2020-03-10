package cn.vove7.jarvis.view.custom

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import cn.vove7.slide_picker.SlideDelegate
import cn.vove7.slide_picker.toggleSelected
import kotlin.math.sqrt

/**
 * # SlideRelativeLayout
 *
 * @author Vove
 * 2020/1/3
 */
class SlideFLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val delegate by lazy {
        SlideDelegate { target, _, _ ->
            if (target != null) {
                hasResult = true
            }
            target?.toggleSelected()
        }
    }

    var onStartMove: (() -> Unit)? = null
    var onTouchUp: ((hasResult: Boolean) -> Unit)? = null

    private var hasResult = false
    private lateinit var downPoint: Point

    private var moved = false

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                hasResult = false
                moved = false
                downPoint = ev.point
            }
            MotionEvent.ACTION_MOVE -> {
                if (!moved) {
                    if (ev.point.distance(downPoint) > 20) {
                        moved = true
                        onStartMove?.invoke()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                onTouchUp?.invoke(hasResult)
            }
        }
        return delegate.onProcessTouchEvent(this, ev)
    }


    private val MotionEvent.point: Point get() = Point(rawX.toInt(), rawY.toInt())


    private fun Point.distance(that: Point): Double {
        val dx = x - that.x
        val dy = y - that.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }
}