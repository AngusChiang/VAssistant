package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.jarvis.R
import cn.vove7.jarvis.plugins.AdKillerService
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
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        initData()
        setContentView(R.layout.activity_expandable_settings)
        val adapter = SettingsExpandableAdapter(this, groupItems, expand_list)
        expand_list.setAdapter(adapter)

        expand_list.post {
            expand_list.expandGroup(0)
            expand_list.expandGroup(1)
            expand_list.expandGroup(2)
            expand_list.expandGroup(3)
        }
    }

    lateinit var groupItems: List<SettingGroupItem>

    private fun initData() {
        groupItems = listOf(
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
                        SwitchItem(R.string.text_open_voice_wakeup, "以 \"你好小V\" 唤醒",
                                keyId = R.string.key_open_voice_wakeup, callback = { holder, it ->
                            when (it as Boolean) {
                                true -> {
                                    AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP)
                                    GlobalApp.toastShort("语音唤醒已开启")
                                }
                                false -> AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP)
                            }
                        }, defaultValue = { false }),
                        CheckBoxItem(R.string.text_voice_control_dialog, "使用语言命令控制对话框",
                                R.string.key_voice_control_dialog, defaultValue = { true })
                )),
                SettingGroupItem(R.color.google_blue, "去广告", childItems = listOf(
                        SwitchItem(R.string.text_open_ad_killer_service, null, R.string.key_open_ad_block,
                                defaultValue = { true }) { holder, it ->
                            when (it as Boolean) {
                                true -> AdKillerService.bindServer()
                                false -> AdKillerService.unBindServer()
                            }
                        },
                        NumberPickerItem(R.string.text_time_wait_ad, "界面等待广告出现最长时间，单位秒\n超时暂停",
                                keyId = R.string.key_ad_wait_secs, range = Pair(10, 100),
                                defaultValue = { 17 })
                ))
        )
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
