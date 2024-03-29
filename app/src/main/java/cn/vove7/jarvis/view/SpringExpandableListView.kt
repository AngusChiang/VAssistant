package cn.vove7.jarvis.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ExpandableListView
import cn.vove7.jarvis.view.tools.SpringEffectHelper
import cn.vove7.jarvis.view.tools.TranslationYPropertyCompat


/**
 * # SpringExpandableListView
 *
 * Created on 2020/6/9
 * @author Vove
 */
class SpringExpandableListView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ExpandableListView(context, attrs, defStyleAttr) {

    private val springHelper = SpringEffectHelper(
            this, ::isBottom, ::isTop,
            { super.onTouchEvent(it) },
            TranslationYPropertyCompat()
    )

    private val isTop get() = firstVisiblePosition == 0 && getChildAt(0)?.top == top + paddingTop
    private val isBottom get() = lastVisiblePosition == count - 1 && getChildAt(childCount - 1).bottom <= bottom - paddingBottom

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return springHelper.onTouch(e)
    }

}