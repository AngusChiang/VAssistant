package cn.vove7.jarvis.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import cn.vove7.jarvis.view.tools.SpringEffectHelper
import cn.vove7.jarvis.view.tools.TranslationYPropertyCompat
import cn.vove7.vtp.view.listview.WrapContentListView

/**
 * # SpringWrapContentListView
 *
 * Created on 2020/6/9
 * @author Vove
 */
class SpringWrapContentListView(context: Context, p_attrs: AttributeSet) : WrapContentListView(context, p_attrs) {

    private val springHelper = SpringEffectHelper(
            this, ::isBottom, ::isTop,
            { super.onTouchEvent(it) },
            TranslationYPropertyCompat()
    )

    private val isTop get() = true
    private val isBottom get() = true

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return springHelper.onTouch(e)
    }

}