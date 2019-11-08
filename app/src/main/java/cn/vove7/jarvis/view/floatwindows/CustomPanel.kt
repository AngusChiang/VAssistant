package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.SettingChildItem
import kotlinx.android.synthetic.main.float_panel_default.view.*


class CustomPanel : FloatyPanel(-1, -2) {

    override fun layoutResId(): Int = R.layout.float_panel_custom

    override fun onCreateView(view: View) {
        animationBody.layoutParams = (animationBody.layoutParams as LinearLayout.LayoutParams).also {
            it.topMargin = statusbarHeight
        }
    }

    override fun showListeningAni() {
        if (contentView?.listening_ani?.isShown == true) return
        contentView?.parse_ani?.gone()
        contentView?.listening_ani?.show()
    }

    override fun showParseAni() {
        if (contentView?.parse_ani?.isShown == true) return
        contentView?.parse_ani?.apply {
            (drawable as? AnimationDrawable)?.start()
            show()
        }
        contentView?.listening_ani?.gone()
    }

    override fun showTextResult(result: String) {
        runOnUi {
            showListeningAni()
            f<TextView>(R.id.result_text)?.apply {
                show()
                text = result
            }
        }
    }

    override val settingItems: Array<SettingChildItem>
        get() = emptyArray()
}