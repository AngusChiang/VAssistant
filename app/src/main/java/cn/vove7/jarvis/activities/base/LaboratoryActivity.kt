package cn.vove7.jarvis.activities.base

import android.content.Intent
import android.os.Bundle
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.newDoc
import cn.vove7.common.utils.startActivityOnNewTask
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PluginManagerActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.plugins.VoiceWakeupStrategy
import cn.vove7.jarvis.services.MainService
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.custom.SettingGroupItem
import kotlinx.android.synthetic.main.activity_expandable_settings.*
import java.lang.Thread.sleep

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
                expandGroup(0, true)
                expandGroup(1, true)
            }
        }
    }

    private val groupItems: List<SettingGroupItem> by lazy {
        listOf(
                SettingGroupItem(R.color.indigo_700, "插件管理", childItems = listOf(
                        IntentItem(title = "插件管理", summary = "扩展功能") {
                            startActivityOnNewTask(Intent(this, PluginManagerActivity::class.java).also {
                                it.newDoc()
                            })
                        },
                        CheckBoxItem(title = "自动检查更新", keyId = R.string.key_auto_check_plugin_update,
                                defaultValue = AppConfig.autoCheckPluginUpdate)
                )),
                SettingGroupItem(R.color.google_blue, getString(R.string.text_open_ad_killer_service), childItems = listOf(
                        SwitchItem(R.string.text_open, summary = if (UserInfo.isVip()) null
                        else getString(R.string.summary_not_vip_remove_ad), keyId = R.string.key_open_ad_block,
                                defaultValue = { true }) { _, it ->
                            when (it) {
                                true -> AdKillerService.register()
                                false ->
                                    AdKillerService.unregister()
                            }
                            return@SwitchItem true
                        },
                        NumberPickerItem(R.string.text_time_wait_ad, "界面等待广告出现最长时间，单位秒",
                                keyId = R.string.key_ad_wait_secs, range = Pair(10, 100),
                                defaultValue = { 17 }),
                        CheckBoxItem(title = "智能识别广告", summary = "[应用切换]时识别未标记的广告页并清除\n有效时间1.5s\n可能会增加耗电",
                                keyId = R.string.key_smart_find_and_kill_ad, defaultValue = AppConfig.smartKillAd),
                        CheckBoxItem(R.string.text_show_toast_when_remove_ad, summary = getString(R.string.text_show_toast_when_remove_ad_summary)
                                , keyId = R.string.key_show_toast_when_remove_ad, defaultValue = true)
                )),
                SettingGroupItem(R.color.google_green, titleS = "聊天", childItems = listOf(
                        SwitchItem(title = "开启", summary = "指令匹配失败，调用聊天系统",
                                keyId = R.string.key_open_chat_system, defaultValue = { true }) { _, b ->
                            if (b) {
                                MainService.instance?.loadChatSystem()
                            }
                            return@SwitchItem true
                        },
                        SingleChoiceItem(title = "对话系统",
                                keyId = R.string.key_chat_system_type, entityArrId = R.array.list_chat_system,
                                defaultValue = { 0 }) { _, d ->
                            runOnPool {
                                sleep(800)//等待设置完成
                                MainService.instance?.loadChatSystem()
                                GlobalApp.toastInfo("对话系统切换完成")
                            }
                            return@SingleChoiceItem true
                        },
                        InputItem(title = "自定义参数", keyId = R.string.key_chat_str),
                        IntentItem(title = "自定义教程") {
                            SystemBridge.openUrl("https://vove.gitee.io/2019/01/24/custom_chat_system/")
                        }

                )),
                SettingGroupItem(R.color.google_red, titleS = "屏幕助手", childItems = listOf(
                        SwitchItem(title = "助手模式", summary = "设为默认语音辅助应用后\n通过唤醒用系统语音助手触发\n可捕捉屏幕内容进行快捷操作\n关闭后只能使快速唤醒", keyId = R.string.key_use_assist_service,
                                defaultValue = { AppConfig.useAssistService }),
                        InputItem(title = "文字识别参数设置", summary = "自定义文字识别key", keyId = R.string.key_text_ocr_key),
                        SingleChoiceItem(title = "长按HOME键操作",summary = "适用于一加",
                                keyId = R.string.key_home_fun, entityArrId = R.array.list_home_funs)

                )),
                SettingGroupItem(R.color.amber_A700, titleS = "结束词", childItems = listOf(
                        InputItem(title = "设置结束词", summary = "在指令结尾可以快速结束聆听\n注意根据效果来设置结束词\n不使用，置空即可",
                                keyId = R.string.key_finish_word)
                )),
                /*SettingGroupItem(R.color.teal_A700, titleS = "省电模式", childItems = listOf(
//                        CheckBoxItem(title = "去广告服务", defaultValue = { true },
//                                keyId = R.string.key_remove_ad_power_saving_mode,
//                                summary = "在系统发出低电量提醒后，自动关闭去广告服务\n充电后自动开启"),
                        CheckBoxItem(title = "无障碍服务", summary = "在系统发出低电量提醒后，自动关闭无障碍服务\n依赖无障碍服务的部分功将无法使用，基础功能仍能使用\n充电后自动恢复服务",
                                keyId = R.string.key_accessibility_service_power_saving_mode)

                )),*/
                SettingGroupItem(R.color.yellow_700, titleS = "语音唤醒", childItems = listOf(
                        SwitchItem(title = "自动释放麦克风", summary = "在已授予麦克风权限的其他App内自动关闭语音唤醒\n需要无障碍",/*设为系统应用后无效*/
                                keyId = R.string.key_fix_voice_micro, defaultValue = { AppConfig.fixVoiceMicro }) { _, b ->
                            if (b /* TODO && !AppConfig.IS_SYS_APP*/)
                                VoiceWakeupStrategy.register()
                            else
                                VoiceWakeupStrategy.unregister()
                            return@SwitchItem true
                        },
                        CheckBoxItem(title = "显示通知", summary = "关闭和打开时在状态栏显示通知",
                                keyId = R.string.key_close_wakeup_notification, defaultValue = true)

                ))
//                SettingGroupItem(R.color.yellow_700, titleS = "其他", childItems = listOf(
//                    IntentItem(title="无障碍黑名单",summary = "如果开启无障碍后，在某个应用内卡顿，请将它添加进来"){
//                        startActivityOnNewTask(Intent(this,AccServiceBlackListManagerActivity::class.java).also{
//                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
//                        })
//                    }
//                ))
        )
    }

}