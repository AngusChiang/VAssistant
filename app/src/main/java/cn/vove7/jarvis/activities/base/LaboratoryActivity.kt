package cn.vove7.jarvis.activities.base

import android.os.Bundle
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.R
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.view.NumberPickerItem
import cn.vove7.jarvis.view.SwitchItem
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.custom.SettingsExpandableAdapter
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
        listOf(
                SettingGroupItem(R.color.google_blue, getString(R.string.text_open_ad_killer_service),
                        childItems = listOf(
                                SwitchItem(R.string.text_open, if (UserInfo.isVip()) null
                                else getString(R.string.summary_not_vip_remove_ad), R.string.key_open_ad_block,
                                        defaultValue = { true }) { holder, it ->
                                    when (it as Boolean) {
                                        true -> AdKillerService.bindServer()
                                        false -> AdKillerService.unBindServer()
                                    }
                                },
                                NumberPickerItem(R.string.text_time_wait_ad, "界面等待广告出现最长时间，单位秒",
                                        keyId = R.string.key_ad_wait_secs, range = Pair(10, 100),
                                        defaultValue = { 17 })
                        )
                )
        )
    }

}