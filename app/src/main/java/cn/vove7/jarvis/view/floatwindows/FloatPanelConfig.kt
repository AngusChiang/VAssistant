package cn.vove7.jarvis.view.floatwindows

import cn.vove7.common.R
import cn.vove7.common.utils.asColor
import cn.vove7.smartkey.AConfig
import cn.vove7.smartkey.android.noCacheKey
import cn.vove7.smartkey.annotation.Config

/**
 * # FloatPanelConfig
 *
 * @author Vove
 * 2019/11/8
 */
@Config("float_panel_config")
object FloatPanelConfig : AConfig() {

    val defaultPanelAnimation: Int by noCacheKey(0, R.string.key_default_fp_animation)
    val defaultPanelRadius: Int by noCacheKey(20, R.string.key_default_fp_radius)
    val defaultPanelColor: Int by noCacheKey("#0085f4".asColor, R.string.key_default_fp_color)
    val defaultTextColor: Int by noCacheKey("#ffffff".asColor, R.string.key_default_fp_text_color)


}