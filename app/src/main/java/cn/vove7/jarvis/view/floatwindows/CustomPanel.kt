package cn.vove7.jarvis.view.floatwindows

import android.widget.TextView
import cn.vove7.jarvis.R


class CustomPanel : FloatyPanel(-1, -1) {

    override fun layoutResId(): Int = R.layout.float_panel_custom

    override fun showTextResult(result: String) {
        super.showTextResult(result)
        f<TextView>(R.id.result_text)?.text = result
    }

}