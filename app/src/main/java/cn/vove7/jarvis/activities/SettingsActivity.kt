package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.custom.SettingGroupItem
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
            SettingGroupItem(R.color.google_blue, "😄", childItems = listOf(
                    IntentItem(R.string.text_set_as_default_voice_assist, summary = "以快速唤醒",
                            onClick = { _, _ ->
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                                    } else {
                                        startActivity(Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"))
                                    }
                                } catch (e: Exception) {
                                    GlobalLog.err(e)
                                    Toast.makeText(this@SettingsActivity,
                                            "跳转失败", Toast.LENGTH_SHORT).show()
                                }
                            })

            )), SettingGroupItem(R.color.google_green, "通知", childItems = listOf(
            SwitchItem(R.string.text_vibrate_reco_begin,
                    keyId = R.string.key_vibrate_reco_begin, defaultValue = { true })
    )),
            SettingGroupItem(R.color.google_yellow, "语音合成", childItems = listOf(
                    SwitchItem(R.string.text_play_voice_message, summary = "关闭后以弹窗形式提醒",
                            keyId = R.string.key_audio_speak, defaultValue = { true }),
                    SingleChoiceItem(R.string.text_sound_model, summary = "在线声音模型", keyId = R.string.key_voice_syn_model,
                            defaultValue = { 0 }, entityArrId = R.array.voice_model_entities,
                            callback = { h, i ->
                                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_RELOAD_SYN_CONF)
                            }),
                    NumberPickerItem(R.string.text_speak_speed, keyId = R.string.key_voice_syn_speed,
                            defaultValue = { 5 }, range = Pair(1, 9), callback = { h, i ->
                        AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_RELOAD_SYN_CONF)
                    })
            )),
            SettingGroupItem(R.color.google_red, titleId = R.string.text_voice_control, childItems = listOf(
                    CheckBoxItem(R.string.text_voice_control_dialog, summary = "使用语言命令控制对话框",
                            keyId = R.string.key_voice_control_dialog, defaultValue = { true })
            )),
            SettingGroupItem(R.color.cyan_500, titleId = R.string.text_wakeup_way, childItems = listOf(
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
                            keyId = R.string.key_long_press_volume_up_wake_up, summary = "需要无障碍模式开启", defaultValue = { true })
            ))
//           ,SettingGroupItem(R.color.lime_600, titleId = R.string.text_animation, childItems = listOf(
//                    CheckBoxItem(, "应用内动画",
//                            R.string.key_voice_control_dialog, defaultValue = { true })
//            )
            ,
            SettingGroupItem(R.color.lime_600, titleId = R.string.text_other, childItems = listOf(
                    CheckBoxItem(title = "用户体验计划", summary = "改善体验与完善功能",
                            keyId = R.string.key_user_exp_plan, defaultValue = { true })
            ))
    )

}
