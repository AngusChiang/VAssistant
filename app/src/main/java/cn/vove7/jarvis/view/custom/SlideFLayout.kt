package cn.vove7.jarvis.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import cn.vove7.slide_picker.SlideDelegate

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
            target?.toggle()
        }
    }

    var onStartMove: (() -> Unit)? = null
    var onTouchUp: ((hasResult: Boolean) -> Unit)? = null
    private var hasResult = false

    private var moved = false

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                hasResult = false
                moved = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!moved) {
                    moved = true
                    onStartMove?.invoke()
                }
            }
            MotionEvent.ACTION_UP -> {
                onTouchUp?.invoke(hasResult)
            }
        }
        return delegate.onProcessTouchEvent(this, ev)
    }
}