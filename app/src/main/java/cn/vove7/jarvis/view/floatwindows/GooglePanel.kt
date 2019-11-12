package cn.vove7.jarvis.view.floatwindows

import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.SettingChildItem

/**
 * # GooglePanel
 *
 * @author Vove
 * 2019/11/11
 */
class GooglePanel : FloatyPanel(-1, -2) {
    override val posY: Int get() = SystemBridge.screenHeight

    override fun layoutResId(): Int = R.layout.float_panel_google

    override val settingItems: Array<SettingChildItem>
        get() = emptyArray()
}