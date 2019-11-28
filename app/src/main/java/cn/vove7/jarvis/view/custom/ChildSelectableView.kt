package cn.vove7.jarvis.view.custom

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import cn.vove7.jarvis.activities.TextOcrActivity
import kotlin.math.sqrt

class ChildSelectableView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), View.OnTouchListener {

    init {
        setOnTouchListener(this)
    }

    private var wordItems = mutableListOf<TextOcrActivity.Model>()
    private var onChildViewClick: Function1<TextOcrActivity.Model, Unit>? = null

    var onTouchDown: (() -> Unit)? = null
    var onStartMove: (() -> Unit)? = null
    var onTouchUp: (() -> Unit)? = null

    @Synchronized
    fun setData(data: List<TextOcrActivity.Model>, onViewClick: Function1<TextOcrActivity.Model, Unit>) {
        wordItems.clear()
        wordItems.addAll(data)
        onChildViewClick = onViewClick
    }

    private val MotionEvent.point: Point get() = Point(rawX.toInt(), rawY.toInt())

    private var lastSelModel: TextOcrActivity.Model? = null

    private var moveToOther = false
    private var move = false
    private var downPoint: Point? = null

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val p = event.point
                downPoint = p
                moveToOther = false
                move = false
                val model = wordItems.find { p in it }
                if (model != null) {
                    lastSelModel = model
                    model.textView?.toggle()
                }
                onTouchDown?.invoke()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!move && downPoint!!.distance(event.point) > 20) {
                    onStartMove?.invoke()
                    move = true
                }
                val p = event.point
                val model = wordItems.find { p in it }
                if (model != null) {
                    if (lastSelModel != model) {
                        moveToOther = true
                        model.textView?.toggle()
                        lastSelModel = model
                    }
                } else {
                    lastSelModel = null
                }
            }
            MotionEvent.ACTION_UP -> {
                val m = lastSelModel
                val p = event.point
                val dis = p.distance(downPoint!!)
                if (!moveToOther && dis < 20 && m != null) {
                    onChildViewClick?.invoke(m)
                }
                lastSelModel = null
                onTouchUp?.invoke()
                downPoint = null
            }
        }
        return true
    }

    private fun Point.distance(that: Point): Double {
        val dx = x - that.x
        val dy = y - that.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }

}
