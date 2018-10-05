package cn.vove7.jarvis.activities.base

import android.os.Bundle
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.view.NumberPickerItem
import cn.vove7.jarvis.view.SwitchItem
import cn.vove7.jarvis.view.custom.SettingGroupItem
import kotlinx.android.synthetic.main.activity_expandable_settings.*

/**
 * # LaboratoryActivity
 *
 * @author Administrator
 * 9/24/2018
 */
class LaboratoryActivity : ReturnableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_expandable_settings)
        val adapter = SettingsExpandableAdapter(this, groupItems, expand_list)
        expand_list.setAdapter(adapter)

        expand_list.post {
            expand_list.expandGroup(0)
        }
    }

    private val groupItems: List<SettingGroupItem> by lazy {
        listOf(SettingGroupItem(R.color.google_blue, getString(R.string.text_open_ad_killer_service), childItems = listOf(
                SwitchItem(R.string.text_open, summary = if (UserInfo.isVip()) null
                else getString(R.string.summary_not_vip_remove_ad), keyId = R.string.key_open_ad_block,
                        defaultValue = { true }) { _, it ->
                    when (it as Boolean) {
                        true -> MyAccessibilityService.registerEvent(AdKillerService)
                        false ->
                            MyAccessibilityService.unregisterEvent(AdKillerService)
                    }
                },
                NumberPickerItem(R.string.text_time_wait_ad, "界面等待广告出现最长时间，单位秒",
                        keyId = R.string.key_ad_wait_secs, range = Pair(10, 100),
                        defaultValue = { 17 }),
                SwitchItem(R.string.text_show_toast_when_remove_ad, summary = getString(R.string.text_show_toast_when_remove_ad_summary)
                        , keyId = R.string.key_show_toast_when_remove_ad, defaultValue = { true })
        )
        )
        )
    }

}