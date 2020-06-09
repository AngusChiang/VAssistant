package cn.vove7.jarvis.view

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.widget.ExpandableListView

/**
 * # SpringExpandableListView
 *
 * Created on 2020/6/9
 * @author Vove
 */
class SpringExpandableListView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ExpandableListView(context, attrs, defStyleAttr) {
    private var mMaxYOverscrollDistance = 0

    init {
        initBounceListView()
    }

    private fun initBounceListView() {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val density = metrics.density
        mMaxYOverscrollDistance = (density * MAX_Y_OVERSCROLL_DISTANCE).toInt()
    }

    override fun overScrollBy(
            deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int,
            scrollRangeX: Int, scrollRangeY: Int, maxOverScrollX: Int, maxOverScrollY: Int,
            isTouchEvent: Boolean
    ): Boolean {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent)
    }

    companion object {
        private const val MAX_Y_OVERSCROLL_DISTANCE = 50
    }

}