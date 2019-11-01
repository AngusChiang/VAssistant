package cn.vove7.jarvis.view.floatwindows

import android.view.View
import android.widget.LinearLayout
import cn.vove7.jarvis.R

/**
 * # OldFloatPanel
 *
 * @author Vove
 * 2019/10/22
 */
class OldFloatPanel : FloatyPanel(-1, -2) {

    override fun layoutResId(): Int = R.layout.float_panel_old

    override fun onCreateView(view: View) {
        animationBody.layoutParams = (animationBody.layoutParams as LinearLayout.LayoutParams).also {
            it.topMargin = statusbarHeight
        }
    }

}