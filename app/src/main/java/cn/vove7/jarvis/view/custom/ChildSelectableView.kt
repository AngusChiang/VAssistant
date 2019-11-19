package cn.vove7.jarvis.view.custom

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import cn.vove7.jarvis.activities.TextOcrActivity

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

    private var move = false
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val p = event.point
                move = false
                val model = wordItems.find { p in it }
                if (model != null) {
                    lastSelModel = model
                    model.textView?.toggle()
                }
                onTouchDown?.invoke()
            }
            MotionEvent.ACTION_MOVE -> {
                if(!move){
                    onStartMove?.invoke()
                }
                move = true
                val p = event.point
                val model = wordItems.find { p in it }
                if (model != null) {
                    if (lastSelModel != model) {
                        model.textView?.toggle()
                        lastSelModel = model
                    }
                } else {
                    lastSelModel = null
                }
            }
            MotionEvent.ACTION_UP -> {
                val m = lastSelModel
                if (!move && m != null) {
                    onChildViewClick?.invoke(m)
                }
                lastSelModel = null
                onTouchUp?.invoke()
            }
        }
        return true
    }

}
