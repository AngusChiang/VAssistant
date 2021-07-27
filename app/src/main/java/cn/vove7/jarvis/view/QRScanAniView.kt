package cn.vove7.jarvis.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.asActivity

class QRScanAniView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleObserver {

    private val iv = ImageView(context)

    private val animator: ValueAnimator by lazy {
        ValueAnimator.ofFloat(0f, 0f).apply {
            resetAnimator(this)
            repeatCount = -1
            interpolator = LinearInterpolator()
            addUpdateListener {
                val h = (it.animatedValue as Float)
                iv.translationY = h
            }
        }
    }

    private fun resetAnimator(ani: ValueAnimator) {
        val h = (height - iv.height).toFloat()
        ani.setFloatValues(0f, h)
        ani.duration = (h / 20).toLong() * 50
    }

    init {
        val lp = LayoutParams(-1, -2)
        iv.adjustViewBounds = true
        addView(iv, lp)
        iv.setImageResource(R.drawable.cmc)
        (context.asActivity as ComponentActivity).lifecycle.addObserver(this)
        post {
            animator.start()
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetAnimator(animator)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        animator.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        animator.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        animator.cancel()
    }

}