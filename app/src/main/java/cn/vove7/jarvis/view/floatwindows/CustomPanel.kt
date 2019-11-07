package cn.vove7.jarvis.view.floatwindows

import android.widget.TextView
import cn.vove7.jarvis.R


class CustomPanel : FloatyPanel(-1, -1) {

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
        super.showTextResult(result)
        f<TextView>(R.id.result_text)?.text = result
    }

}