package cn.vove7.jarvis.view.floatwindows

import android.view.View
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.databinding.FloatPanelGoogleBinding
import cn.vove7.jarvis.view.SettingChildItem

/**
 * # GooglePanel
 *
 * @author Vove
 * 2019/11/11
 */
class GooglePanel : FloatyPanel<FloatPanelGoogleBinding>(-1, -2) {
    override val posY: Int get() = SystemBridge.screenHeight

    override fun onCreateView(view: View) {

    }

    override val settingItems: Array<SettingChildItem>
        get() = emptyArray()
}