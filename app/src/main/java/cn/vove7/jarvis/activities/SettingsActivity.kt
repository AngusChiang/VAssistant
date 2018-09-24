package cn.vove7.jarvis.activities

import android.os.Bundle
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.view.CheckBoxItem
import cn.vove7.jarvis.view.NumberPickerItem
import cn.vove7.jarvis.view.SingleChoiceItem
import cn.vove7.jarvis.view.SwitchItem
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.custom.SettingsExpandableAdapter
import kotlinx.android.synthetic.main.activity_expandable_settings.*

/**
 *
 */
class SettingsActivity : ReturnableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        setContentView(R.layout.activity_expandable_settings)
        val adapter = SettingsExpandableAdapter(this, initData(), expand_list)
        expand_list.setAdapter(adapter)

        expand_list.post {
            expand_list.expandGroup(0)
            expand_list.expandGroup(1)
            expand_list.expandGroup(2)
            expand_list.expandGroup(3)
        }
    }

    private fun initData(): List<SettingGroupItem> = listOf(
            SettingGroupItem(R.color.google_green, "通知", childItems = listOf(
                    SwitchItem(R.string.text_show_toast_when_remove_ad,
                            keyId = R.string.key_show_toast_when_remove_ad, defaultValue = { true }),
                    SwitchItem(R.string.text_vibrate_reco_begin,
                            keyId = R.string.key_vibrate_reco_begin, defaultValue = { true })
            )),
            SettingGroupItem(R.color.google_yellow, "语音合成", childItems = listOf(
                    SwitchItem(R.string.text_play_voice_message, "关闭后以弹窗形式提醒",
                            keyId = R.string.key_audio_speak, defaultValue = { true }),
                    SingleChoiceItem(R.string.text_sound_model, "在线声音模型", R.string.key_voice_syn_model,
                            defaultValue = { getString(R.string.default_voice_model) }, entityArrId = R.array.voice_model_entities,
                            valueArrId = R.array.voice_model_values, callback = { h, i ->
                        AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_RELOAD_SYN_CONF)
                    }),
                    NumberPickerItem(R.string.text_speak_speed, keyId = R.string.key_voice_syn_speed,
                            defaultValue = { 5 }, range = Pair(1, 9), callback = { h, i ->
                        AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_RELOAD_SYN_CONF)
                    })
            )),
            SettingGroupItem(R.color.google_red, titleId = R.string.text_voice_control, childItems = listOf(
                    CheckBoxItem(R.string.text_voice_control_dialog, "使用语言命令控制对话框",
                            R.string.key_voice_control_dialog, defaultValue = { true })
            )),
            SettingGroupItem(R.color.google_red, titleId = R.string.text_wakeup, childItems = listOf(
                    SwitchItem(R.string.text_open_voice_wakeup, "以 \"你好小V\" 唤醒",
                            keyId = R.string.key_open_voice_wakeup, callback = { holder, it ->
                        when (it as Boolean) {
                            true -> {
                                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP)
                            }
                            false -> AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP)
                        }
                    }, defaultValue = { false }),
                    SwitchItem(R.string.text_long_press_volume_up, null,
                            keyId = R.string.key_long_press_volume_up_wake_up, defaultValue = { true })
            ))
    )

}
