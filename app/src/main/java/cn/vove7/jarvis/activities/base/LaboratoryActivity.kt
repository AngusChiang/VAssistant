package cn.vove7.jarvis.activities.base

import android.os.Bundle
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.custom.SettingGroupItem
import kotlinx.android.synthetic.main.activity_expandable_settings.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread

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
        val expandableListView = expand_list
        val adapter = SettingsExpandableAdapter(this, groupItems, expandableListView)
        expandableListView.setAdapter(adapter)

        expandableListView?.post {
            expandableListView.apply {
                expandGroup(0)
                expandGroup(1)
            }
        }
    }

    private val groupItems: List<SettingGroupItem> by lazy {
        listOf(
                SettingGroupItem(R.color.google_blue, getString(R.string.text_open_ad_killer_service), childItems = listOf(
                        SwitchItem(R.string.text_open, summary = if (UserInfo.isVip()) null
                        else getString(R.string.summary_not_vip_remove_ad), keyId = R.string.key_open_ad_block,
                                defaultValue = { true }) { _, it ->
                            when (it as Boolean) {
                                true -> MyAccessibilityService.registerEvent(AdKillerService)
                                false ->
                                    MyAccessibilityService.unregisterEvent(AdKillerService)
                            }
                            return@SwitchItem true
                        },
                        NumberPickerItem(R.string.text_time_wait_ad, "界面等待广告出现最长时间，单位秒",
                                keyId = R.string.key_ad_wait_secs, range = Pair(10, 100),
                                defaultValue = { 17 }),
                        SwitchItem(R.string.text_show_toast_when_remove_ad, summary = getString(R.string.text_show_toast_when_remove_ad_summary)
                                , keyId = R.string.key_show_toast_when_remove_ad, defaultValue = { true })
                )),
                SettingGroupItem(R.color.google_green, titleS = "聊天", childItems = listOf(
                        SwitchItem(title = "开启", summary = "指令匹配失败，调用聊天系统",
                                keyId = R.string.key_open_chat_system, defaultValue = { true }) { _, b ->
                            if (b as Boolean) {
                                MainService.instance?.loadChatSystem()
                            }
                            return@SwitchItem true
                        },
                        SingleChoiceItem(title = "对话系统", summary = "由于每日调用次数有限，图灵机器人仅高级用户可用",
                                keyId = R.string.key_chat_system_type, entityArrId = R.array.list_chat_system,
                                defaultValue = { 0 }) { _, d ->
                            if (!UserInfo.isVip() && (d as Pair<*, *>).first != 0) {
                                toast.showShort("设置无效")
                                return@SingleChoiceItem false
                            }
                            thread {
                                sleep(500)//等待设置完成
                                MainService.instance?.loadChatSystem(true)
                            }
                            return@SingleChoiceItem true
                        },
                        CheckBoxItem(title = "连续对话", summary = "开启后可连续对话",
                                keyId = R.string.key_continuous_dialogue)
                )),
                SettingGroupItem(R.color.google_red, titleS = "语音助手", childItems = listOf(
                        SwitchItem(title = "助手模式(暂未开放)", summary = "通过唤醒用系统语音助手触发，可捕捉屏幕内容\n关闭后只能使快速唤醒", keyId = R.string.key_use_assist_service,
                                defaultValue = { false }),
                        SwitchItem(title = "立即识别", summary = "开启自动识别",
                                keyId = R.string.key_reco_when_wakeup_assist, defaultValue = { false }
                        )
                )),
                SettingGroupItem(R.color.amber_A700,titleS = "结束词", childItems = listOf(
                        InputItem(title = "设置结束词", summary = "在指令结尾可以快速结束聆听\n注意根据效果来设置结束词\n不使用，置空即可",
                                keyId = R.string.key_finish_word)
                ))
        )
    }

}