package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.ShortcutUtil
import cn.vove7.jarvis.utils.UriUtils
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
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

        expand_list?.post {
            expand_list?.apply {
                expandGroup(0)
                expandGroup(1)
                expandGroup(2)
                expandGroup(3)
            }
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
                    SwitchItem(R.string.text_open_voice_wakeup, summary = "以 \"你好小V\" 唤醒",
                            keyId = R.string.key_open_voice_wakeup, callback = { holder, it ->
                        when (it as Boolean) {
                            true -> {
                                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP)
                            }
                            false -> AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP)
                        }
                    }, defaultValue = { false }),
                    SwitchItem(R.string.text_long_press_volume_up, null,
                            keyId = R.string.key_long_press_volume_up_wake_up, summary = "需要无障碍模式开启",
                            defaultValue = { true }),
                    IntentItem(R.string.text_add_wakeup_shortcut_to_launcher, summary = "添加需要8.0+，" +
                            "7.1+可直接在桌面长按图标使用Shortcut快捷唤醒",
                            onClick = { _, _ ->
                                ShortcutUtil.addWakeUpPinShortcut()
                            }),
                    CheckBoxItem(R.string.text_auto_open_voice_wakeup_charging,
                            keyId = R.string.key_auto_open_voice_wakeup_charging,
                            callback = { _, b ->
                                if (PowerEventReceiver.isCharging && PowerEventReceiver.open) {//充电中生效
                                    if (b as Boolean) {//正在充电，开启
                                        AppBus.post(SpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP))
                                    } else {
                                        AppBus.post(SpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP))
                                    }
                                }
                            }),
                    IntentItem(R.string.text_customize_wakeup_words) { _, _ ->
                        MaterialDialog(this).title(R.string.text_customize_wakeup_words)
                                .customView(R.layout.dialog_customize_wakeup_words)
                                .show {
                                    findViewById<TextView>(R.id.wakeup_file_path).text = "当前文件路径：${AppConfig.wakeUpFilePath}"
                                    findViewById<View>(R.id.get_wakeup_file).setOnClickListener {
                                        SystemBridge.openUrl("https://vove.gitee.io/2018/10/11/GET_WAKEUP_FILE/")
                                        this.dismiss()
                                    }
                                    neutralButton(text = "恢复默认") {
                                        setPathAndReload(AppConfig.DEFAULT_WAKEUP_FILE)
                                    }
                                    findViewById<View>(R.id.sel_wakeup_file).setOnClickListener {
                                        val selIntent = Intent(Intent.ACTION_GET_CONTENT)
                                        selIntent.type = "*/*"
                                        selIntent.addCategory(Intent.CATEGORY_OPENABLE)
                                        try {
                                            startActivityForResult(selIntent, 1)
                                            this.dismiss()
                                        } catch (e: ActivityNotFoundException) {
                                            e.printStackTrace()
                                            toast.showShort(getString(R.string.text_cannot_open_file_manager))
                                        }
                                    }
                                }
                    })
            )
//           ,SettingGroupItem(R.color.lime_600, titleId = R.string.text_animation, childItems = listOf(
//                    CheckBoxItem(, "应用内动画",
//                            R.string.key_voice_control_dialog, defaultValue = { true })
//            )
            ,
            SettingGroupItem(R.color.lime_600, titleId = R.string.text_other, childItems = listOf(
                    CheckBoxItem(title = "用户体验计划", summary = "改善体验与完善功能",
                            keyId = R.string.key_user_exp_plan, defaultValue =
                    { true })
            ))
            //todo shortcut 管理
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    if (uri != null) {
                        try {
                            val path = UriUtils.getPathFromUri(this, uri)

                            if (path == null) {
                                toast.showShort("路径获取失败")
                            } else if (path.endsWith(".bin")) {
                                setPathAndReload(path)
                            } else {
                                toast.showShort("请选择.bin文件")
                            }
                        } catch (e: Exception) {
                            GlobalLog.err(e)
                            toast.showShort("设置失败")
                        }
                    } else {
                        toast.showShort(getString(R.string.text_open_failed))
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun setPathAndReload(path: String) {
        SpHelper(this).set(R.string.key_wakeup_file_path, path)
        AppConfig.reload()
        if (AppConfig.voiceWakeup) {
            toast.showShort("正在重载配置")
            Handler().postDelayed({
                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_STOP_WAKEUP)
                Thread.sleep(2000)
                AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_START_WAKEUP)
            }, 3000)
        } else
            toast.showShort("设置完成")

    }

}
