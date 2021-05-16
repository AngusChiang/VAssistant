package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.databinding.FloatPanelCustomBinding
import cn.vove7.jarvis.view.SettingChildItem


class CustomPanel : FloatyPanel<FloatPanelCustomBinding>(-1, -2) {

    override fun onCreateView(view: View) {
        animationBody.layoutParams = (animationBody.layoutParams as LinearLayout.LayoutParams).also {
            it.topMargin = statusbarHeight
        }
    }

    override fun showListeningAni() {
        val vb = viewBinding ?: return
        if (vb.listeningAni.isShown) return
        vb.parseAni.gone()
        vb.listeningAni.show()
    }

    override fun showParseAni() {
        val vb = viewBinding ?: return
        if (vb.parseAni.isShown) return
        vb.parseAni.apply {
            (drawable as? AnimationDrawable)?.start()
            show()
        }
        vb.listeningAni.gone()
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