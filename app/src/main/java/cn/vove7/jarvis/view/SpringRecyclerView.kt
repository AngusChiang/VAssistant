package cn.vove7.jarvis.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.vove7.jarvis.view.tools.SpringEffectHelper
import cn.vove7.jarvis.view.tools.TranslationXPropertyCompat
import cn.vove7.jarvis.view.tools.TranslationYPropertyCompat

class SpringRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val lm get() = layoutManager as LinearLayoutManager

    private val springHelper by lazy {
        SpringEffectHelper(
                this, ::isBottom, ::isTop,
                { super.onTouchEvent(it) },
                if (lm.orientation == VERTICAL) TranslationYPropertyCompat()
                else TranslationXPropertyCompat()
        )
    }

    private val isTop get() = if (lm.orientation == VERTICAL) !canScrollVertically(-1) else !canScrollHorizontally(-1)

    private val isBottom get() = if (lm.orientation == VERTICAL) !canScrollVertically(1) else !canScrollHorizontally(1)

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return springHelper.onTouch(e)
    }

}
