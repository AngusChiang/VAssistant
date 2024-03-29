package cn.vove7.jarvis.view.floatwindows

import android.view.View
import android.widget.LinearLayout
import cn.vove7.bottomdialog.util.fadeIn
import cn.vove7.common.utils.fadeOut
import cn.vove7.jarvis.R
import cn.vove7.jarvis.databinding.FloatPanelOldBinding
import cn.vove7.jarvis.view.SettingChildItem

/**
 * # OldFloatPanel
 *
 * @author Vove
 * 2019/10/22
 */
class OldFloatPanel : FloatyPanel<FloatPanelOldBinding>(-1, -2) {

    override fun onCreateView(view: View) {
        animationBody.layoutParams = (animationBody.layoutParams as LinearLayout.LayoutParams).also {
            it.topMargin = statusbarHeight
        }
    }

    override val settingItems: Array<SettingChildItem>
        get() = emptyArray()
}